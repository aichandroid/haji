package com.haji2027.feature.settings.purge

import com.haji2027.core.database.dao.ChecklistDao
import com.haji2027.core.database.dao.LocationSyncQueueDao
import com.haji2027.core.database.dao.PlacesDao
import com.haji2027.core.database.entity.ChecklistEntity
import com.haji2027.core.database.entity.LocationSyncQueueEntity
import com.haji2027.core.database.entity.SavedPlaceEntity
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.auth.AuthState
import com.haji2027.core.network.auth.AuthUser
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.ClaimTokenResponse
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.GenerateTokenResponse
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.PurgeUserDataResponse
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.core.network.dto.RevokeAccessResponse
import com.haji2027.feature.settings.ota.OtaEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class DataPurgeManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var tempDir: File
    private lateinit var fakeChecklistDao: FakeChecklistDao
    private lateinit var fakePlacesDao: FakePlacesDao
    private lateinit var fakeQueueDao: FakeLocationSyncQueueDao
    private lateinit var fakeApiClient: FakeTrackingApiClient
    private lateinit var fakeAuthManager: FakeAuthManager
    private lateinit var purgeManager: DefaultDataPurgeManager

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("hajj_purge_test").toFile()
        fakeChecklistDao = FakeChecklistDao()
        fakePlacesDao = FakePlacesDao()
        fakeQueueDao = FakeLocationSyncQueueDao()
        fakeApiClient = FakeTrackingApiClient()
        fakeAuthManager = FakeAuthManager()

        // Siapkan data awal di dalam DAOs
        fakeChecklistDao.items["step_ihram"] = ChecklistEntity("step_ihram", true, 1000L)
        fakePlacesDao.items["place_hotel"] = SavedPlaceEntity("place_hotel", "Hotel Safwah", "HOTEL", 21.42, 39.82, 5f, "notes", 1000L)
        fakeQueueDao.items[1L] = LocationSyncQueueEntity(1L, "user_1", 21.42, 39.82, 5f, 1000L)

        // Siapkan berkas cache aktif
        File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE).writeText("""{"version":"1.1.0"}""")
        File(tempDir, OtaEngine.TEMP_CONTENT_FILE).writeText("""{"temp":true}""")

        purgeManager = DefaultDataPurgeManager(
            checklistDao = fakeChecklistDao,
            placesDao = fakePlacesDao,
            locationSyncQueueDao = fakeQueueDao,
            trackingApiClient = fakeApiClient,
            authManager = fakeAuthManager,
            contentDir = tempDir,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `testExecutePurgeAllUserDataSuccess_TC_PRIV_01`() = testScope.runTest {
        val result = purgeManager.executePurgeAllUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val purgeResult = result.getOrThrow()

        // Verifikasi seluruh flag bernilai true
        assertTrue("Database harus terhapus bersih", purgeResult.isDatabasePurged)
        assertTrue("Cloud data harus terhapus", purgeResult.isCloudPurged)
        assertTrue("Akun Firebase Auth harus terhapus", purgeResult.isAuthPurged)
        assertTrue("Cache konten OTA harus terhapus", purgeResult.isCachePurged)

        // Verifikasi basis data lokal SQLite benar-benar kosong
        assertEquals(0, fakeChecklistDao.items.size)
        assertEquals(0, fakePlacesDao.items.size)
        assertEquals(0, fakeQueueDao.items.size)

        // Verifikasi berkas di penyimpanan privat terhapus bersih
        assertFalse(File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE).exists())
        assertFalse(File(tempDir, OtaEngine.TEMP_CONTENT_FILE).exists())

        // Verifikasi interaksi API & Auth
        assertTrue(fakeApiClient.purgeUserDataCalled)
        assertTrue(fakeAuthManager.deleteAccountCalled)
        assertTrue(fakeAuthManager.signOutCalled)
    }

    @Test
    fun `testExecutePurgeAllUserDataGracefulWhenCloudFails`() = testScope.runTest {
        fakeApiClient.shouldFailPurge = true

        val result = purgeManager.executePurgeAllUserData()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val purgeResult = result.getOrThrow()

        // Verifikasi basis data lokal tetap dibersihkan meskipun cloud offline
        assertTrue(purgeResult.isDatabasePurged)
        assertFalse(purgeResult.isCloudPurged)
        assertEquals(0, fakeChecklistDao.items.size)
        assertEquals(0, fakePlacesDao.items.size)
        assertEquals(0, fakeQueueDao.items.size)
        assertFalse(File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE).exists())
    }
}

// -------------------------------------------------------------
// FAKES UNTUK PENGUJIAN UNIT PURGE DATA
// -------------------------------------------------------------

private class FakeChecklistDao : ChecklistDao {
    val items = mutableMapOf<String, ChecklistEntity>()

    override fun getAllChecklistFlow(): Flow<List<ChecklistEntity>> = flowOf(items.values.toList())
    override fun getCompletedStepIdsFlow(): Flow<List<String>> = flowOf(items.filter { it.value.isCompleted }.keys.toList())
    override suspend fun getChecklistByStepId(stepId: String): ChecklistEntity? = items[stepId]
    override suspend fun upsertChecklist(entity: ChecklistEntity) { items[entity.stepId] = entity }
    override suspend fun upsertChecklists(entities: List<ChecklistEntity>) { entities.forEach { items[it.stepId] = it } }
    override suspend fun deleteChecklistByStepId(stepId: String): Int = if (items.remove(stepId) != null) 1 else 0
    override suspend fun clearAllChecklist() { items.clear() }
}

private class FakePlacesDao : PlacesDao {
    val items = mutableMapOf<String, SavedPlaceEntity>()

    override fun getAllPlacesFlow(): Flow<List<SavedPlaceEntity>> = flowOf(items.values.toList())
    override suspend fun getPlaceById(placeId: String): SavedPlaceEntity? = items[placeId]
    override fun getPlaceByIdFlow(placeId: String): Flow<SavedPlaceEntity?> = flowOf(items[placeId])
    override fun getPlacesByCategoryFlow(category: String): Flow<List<SavedPlaceEntity>> = flowOf(items.values.filter { it.category == category })
    override suspend fun insertPlace(place: SavedPlaceEntity) { items[place.placeId] = place }
    override suspend fun insertPlaces(places: List<SavedPlaceEntity>) { places.forEach { items[it.placeId] = it } }
    override suspend fun updatePlace(place: SavedPlaceEntity) { items[place.placeId] = place }
    override suspend fun deletePlace(place: SavedPlaceEntity) { items.remove(place.placeId) }
    override suspend fun deletePlaceById(placeId: String): Int = if (items.remove(placeId) != null) 1 else 0
    override suspend fun deleteAllPlaces() { items.clear() }
    override suspend fun getPlacesCount(): Int = items.size
}

private class FakeLocationSyncQueueDao : LocationSyncQueueDao {
    val items = mutableMapOf<Long, LocationSyncQueueEntity>()

    override suspend fun insert(entity: LocationSyncQueueEntity): Long {
        items[entity.queueId] = entity
        return entity.queueId
    }
    override fun observeQueue(): Flow<List<LocationSyncQueueEntity>> = flowOf(items.values.toList())
    override suspend fun getAllItems(): List<LocationSyncQueueEntity> = items.values.toList()
    override suspend fun getLatestItem(): LocationSyncQueueEntity? = items.values.lastOrNull()
    override suspend fun getQueueCount(): Int = items.size
    override suspend fun deleteById(queueId: Long) { items.remove(queueId) }
    override suspend fun deleteBatch(queueIds: List<Long>) { queueIds.forEach { items.remove(it) } }
    override suspend fun trimQueueToSize(maxItems: Int) {}
    override suspend fun clearQueue() { items.clear() }
}

private class FakeTrackingApiClient : TrackingApiClient {
    var shouldFailPurge = false
    var purgeUserDataCalled = false

    override suspend fun generatePairingToken(request: GenerateTokenRequest): Result<GenerateTokenResponse> {
        return Result.failure(NotImplementedError())
    }
    override suspend fun claimPairingToken(request: ClaimTokenRequest): Result<ClaimTokenResponse> {
        return Result.failure(NotImplementedError())
    }
    override suspend fun revokeTrackingAccess(request: RevokeAccessRequest): Result<RevokeAccessResponse> {
        return Result.failure(NotImplementedError())
    }
    override suspend fun purgeUserData(): Result<PurgeUserDataResponse> {
        purgeUserDataCalled = true
        return if (shouldFailPurge) {
            Result.failure(RuntimeException("Cloud unreachable"))
        } else {
            Result.success(PurgeUserDataResponse("SUCCESS", "usr_123", "Purged"))
        }
    }
    override suspend fun syncLocations(locations: List<LocationPointDto>): Result<Unit> = Result.success(Unit)
}

private class FakeAuthManager : AuthManager {
    var deleteAccountCalled = false
    var signOutCalled = false

    private val user = AuthUser("usr_123", true)
    override val authState: StateFlow<AuthState> = MutableStateFlow(AuthState.Authenticated(user))

    override suspend fun ensureAuthenticated(): Result<AuthUser> = Result.success(user)
    override suspend fun signInAnonymously(): Result<AuthUser> = Result.success(user)
    override suspend fun getCurrentUserId(): String? = user.uid
    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = Result.success("token")
    override suspend fun signOut() { signOutCalled = true }
    override suspend fun deleteAccount(): Result<Unit> {
        deleteAccountCalled = true
        return Result.success(Unit)
    }
}
