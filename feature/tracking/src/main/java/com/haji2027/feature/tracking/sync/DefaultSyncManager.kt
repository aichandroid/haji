package com.haji2027.feature.tracking.sync

import com.haji2027.core.database.repository.LocationQueueRepository
import com.haji2027.core.model.UserLocation
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.dto.ApiErrorCode
import com.haji2027.core.network.dto.ApiException
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.feature.tracking.service.LocationTrackingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementasi standar [SyncManager] untuk sinkronisasi telemetri lokasi offline.
 * Menerapkan prinsip Data Minimization: saat sinyal pulih, hanya mengirimkan 1 sampel mutakhir
 * dan membersihkan antrean lama di SQLite untuk menghemat baterai & kuota jemaah (FR-TRACK-006).
 */
class DefaultSyncManager(
    private val queueRepository: LocationQueueRepository,
    private val apiClient: TrackingApiClient,
    private val networkProvider: NetworkConnectivityProvider,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : SyncManager {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val syncMutex = Mutex()

    init {
        // Pantau pemulihan konektivitas internet secara otomatis
        scope.launch {
            networkProvider.observeConnectivity().collect { isOnline ->
                if (isOnline && queueRepository.getPendingCount() > 0) {
                    syncPendingLocations()
                }
            }
        }
    }

    override suspend fun enqueueLocation(location: UserLocation): Long {
        val id = queueRepository.enqueueLocation(location)

        if (networkProvider.isConnected()) {
            scope.launch {
                syncPendingLocations()
            }
        } else {
            val pendingCount = queueRepository.getPendingCount()
            _syncState.value = SyncState.Offline(pendingCount)
        }

        return id
    }

    override suspend fun syncPendingLocations(): Result<Int> = syncMutex.withLock {
        if (!networkProvider.isConnected()) {
            val pendingCount = queueRepository.getPendingCount()
            _syncState.value = SyncState.Offline(pendingCount)
            return Result.failure(
                ApiException(
                    ApiErrorCode.NETWORK_UNAVAILABLE,
                    "Koneksi internet tidak tersedia; koordinat tersimpan aman di antrean lokal."
                )
            )
        }

        val pendingItems = queueRepository.getPendingItems()
        if (pendingItems.isEmpty()) {
            _syncState.value = SyncState.Idle
            return Result.success(0)
        }

        _syncState.value = SyncState.Syncing

        // Data Minimization (Section 17.6): Ambil sampel paling mutakhir
        val latestLocation = queueRepository.getLatestPendingLocation()
        if (latestLocation == null) {
            _syncState.value = SyncState.Idle
            return Result.success(0)
        }

        val pointDto = LocationPointDto(
            userId = latestLocation.userId,
            latitude = latestLocation.latitude,
            longitude = latestLocation.longitude,
            accuracyMeters = latestLocation.accuracyMeters,
            timestampEpochMs = latestLocation.updatedAtEpochMs
        )

        val sendResult = apiClient.syncLocations(listOf(pointDto))

        return if (sendResult.isSuccess) {
            val clearedCount = pendingItems.size
            // Hapus seluruh antrean lokal yang telah kedaluwarsa setelah berhasil terkirim
            queueRepository.clearAll()
            _syncState.value = SyncState.Success(clearedCount)
            Result.success(clearedCount)
        } else {
            val exception = sendResult.exceptionOrNull()
            val pendingCount = queueRepository.getPendingCount()
            _syncState.value = SyncState.Error(
                message = exception?.message ?: "Gagal mengirimkan koordinat ke server",
                queuedCount = pendingCount
            )
            Result.failure(exception ?: Exception("Kegagalan transmisi lokasi"))
        }
    }

    override fun startListeningToTrackingService() {
        LocationTrackingService.locationListener = { userLocation ->
            scope.launch {
                enqueueLocation(userLocation)
            }
        }
    }

    override fun stopListeningToTrackingService() {
        LocationTrackingService.locationListener = null
    }
}
