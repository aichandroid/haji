package com.haji2027.feature.tracking.sync

import com.haji2027.core.database.entity.LocationSyncQueueEntity
import com.haji2027.core.database.repository.LocationQueueRepository
import com.haji2027.core.model.UserLocation
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.ClaimTokenResponse
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.GenerateTokenResponse
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.PurgeUserDataResponse
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.core.network.dto.RevokeAccessResponse
import com.haji2027.feature.tracking.service.LocationTrackingService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeQueueRepository: FakeLocationQueueRepository
    private lateinit var fakeApiClient: FakeTrackingApiClient
    private lateinit var fakeNetworkProvider: FakeNetworkConnectivityProvider
    private lateinit var syncManager: DefaultSyncManager

    @Before
    fun setup() {
        fakeQueueRepository = FakeLocationQueueRepository()
        fakeApiClient = FakeTrackingApiClient()
        fakeNetworkProvider = FakeNetworkConnectivityProvider(initialOnline = false)

        syncManager = DefaultSyncManager(
            queueRepository = fakeQueueRepository,
            apiClient = fakeApiClient,
            networkProvider = fakeNetworkProvider,
            scope = testScope
        )
    }

    @Test
    fun `enqueueLocation when offline stores in SQLite queue and updates syncState to Offline`() = testScope.runTest {
        val loc = UserLocation("usr_ayah_123", 21.4225, 39.8262, 5.0f, 1000L)

        syncManager.enqueueLocation(loc)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeQueueRepository.getPendingCount())
        val state = syncManager.syncState.value
        assertTrue(state is SyncState.Offline)
        assertEquals(1, (state as SyncState.Offline).queuedCount)
        assertEquals(0, fakeApiClient.sentLocations.size)
    }

    @Test
    fun `enqueueLocation when online automatically triggers transmission and empties queue`() = testScope.runTest {
        fakeNetworkProvider.setOnline(true)
        testDispatcher.scheduler.advanceUntilIdle()

        val loc = UserLocation("usr_ayah_123", 21.4225, 39.8262, 5.0f, 2000L)

        syncManager.enqueueLocation(loc)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, fakeQueueRepository.getPendingCount())
        assertEquals(1, fakeApiClient.sentLocations.size)
        assertEquals(21.4225, fakeApiClient.sentLocations.first().latitude, 0.0001)

        val state = syncManager.syncState.value
        assertTrue(state is SyncState.Success)
        assertEquals(1, (state as SyncState.Success).syncedCount)
    }

    @Test
    fun `syncPendingLocations with multiple points sends only the latest sample (Data Minimization)`() = testScope.runTest {
        // Simulasikan 3 titik tertahan saat offline
        fakeQueueRepository.enqueueLocation(UserLocation("u1", 21.4200, 39.8200, 5f, 1000L))
        fakeQueueRepository.enqueueLocation(UserLocation("u1", 21.4210, 39.8210, 4f, 2000L))
        fakeQueueRepository.enqueueLocation(UserLocation("u1", 21.4220, 39.8220, 3f, 3000L))

        assertEquals(3, fakeQueueRepository.getPendingCount())

        // Koneksi internet kembali aktif
        fakeNetworkProvider.setOnline(true)

        val result = syncManager.syncPendingLocations()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow()) // 3 item antrean dibersihkan

        // Hanya 1 titik (titik ke-3 paling mutakhir t=3000L) yang dikirim ke server
        assertEquals(1, fakeApiClient.sentLocations.size)
        val sent = fakeApiClient.sentLocations.first()
        assertEquals(21.4220, sent.latitude, 0.0001)
        assertEquals(3000L, sent.timestampEpochMs)

        // Seluruh antrean lama di SQLite harus kosong
        assertEquals(0, fakeQueueRepository.getPendingCount())
    }

    @Test
    fun `reconnecting to network automatically triggers sync of pending items`() = testScope.runTest {
        // Enqueue saat offline
        syncManager.enqueueLocation(UserLocation("u1", 21.4200, 39.8200, 5f, 1000L))
        syncManager.enqueueLocation(UserLocation("u1", 21.4250, 39.8250, 4f, 2000L))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, fakeQueueRepository.getPendingCount())
        assertEquals(0, fakeApiClient.sentLocations.size)

        // Internet kembali online (menghasilkan emit pada observeConnectivity)
        fakeNetworkProvider.setOnline(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Harus tersinkronisasi otomatis
        assertEquals(0, fakeQueueRepository.getPendingCount())
        assertEquals(1, fakeApiClient.sentLocations.size)
        assertEquals(2000L, fakeApiClient.sentLocations.first().timestampEpochMs)
    }

    @Test
    fun `startListeningToTrackingService links LocationTrackingService listener`() = testScope.runTest {
        syncManager.startListeningToTrackingService()
        assertNotNull(LocationTrackingService.locationListener)

        val sampleLoc = UserLocation("u1", 21.42, 39.82, 4f, 5000L)
        LocationTrackingService.locationListener?.invoke(sampleLoc)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeQueueRepository.getPendingCount())

        syncManager.stopListeningToTrackingService()
        assertNull(LocationTrackingService.locationListener)
    }

    private class FakeLocationQueueRepository : LocationQueueRepository {
        private val queue = mutableListOf<LocationSyncQueueEntity>()
        private var nextId = 1L
        private val _flow = MutableStateFlow<List<LocationSyncQueueEntity>>(emptyList())

        override fun observePendingQueue(): Flow<List<LocationSyncQueueEntity>> = _flow

        override suspend fun enqueueLocation(location: UserLocation): Long {
            val id = nextId++
            queue.add(LocationSyncQueueEntity.fromDomainModel(location).copy(queueId = id))
            _flow.value = queue.toList()
            return id
        }

        override suspend fun getPendingItems(): List<LocationSyncQueueEntity> = queue.toList()

        override suspend fun getLatestPendingLocation(): UserLocation? =
            queue.maxByOrNull { it.timestampEpochMs }?.toDomainModel()

        override suspend fun getPendingCount(): Int = queue.size

        override suspend fun removeItems(queueIds: List<Long>) {
            queue.removeAll { it.queueId in queueIds }
            _flow.value = queue.toList()
        }

        override suspend fun clearAll() {
            queue.clear()
            _flow.value = emptyList()
        }
    }

    private class FakeTrackingApiClient : TrackingApiClient {
        val sentLocations = mutableListOf<LocationPointDto>()
        var shouldFail: Boolean = false

        override suspend fun generatePairingToken(request: GenerateTokenRequest): Result<GenerateTokenResponse> =
            throw UnsupportedOperationException()

        override suspend fun claimPairingToken(request: ClaimTokenRequest): Result<ClaimTokenResponse> =
            throw UnsupportedOperationException()

        override suspend fun revokeTrackingAccess(request: RevokeAccessRequest): Result<RevokeAccessResponse> =
            throw UnsupportedOperationException()

        override suspend fun purgeUserData(): Result<PurgeUserDataResponse> =
            throw UnsupportedOperationException()

        override suspend fun syncLocations(locations: List<LocationPointDto>): Result<Unit> {
            if (shouldFail) {
                return Result.failure(IllegalStateException("Server error (503)"))
            }
            sentLocations.addAll(locations)
            return Result.success(Unit)
        }
    }

    private class FakeNetworkConnectivityProvider(initialOnline: Boolean = true) : NetworkConnectivityProvider {
        private val _flow = MutableStateFlow(initialOnline)
        private var online = initialOnline

        fun setOnline(isOnline: Boolean) {
            online = isOnline
            _flow.value = isOnline
        }

        override fun isConnected(): Boolean = online
        override fun observeConnectivity(): Flow<Boolean> = _flow
    }
}
