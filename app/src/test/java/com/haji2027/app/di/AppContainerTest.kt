package com.haji2027.app.di

import com.haji2027.core.common.calendar.HijriCalendarEngine
import com.haji2027.core.database.AppDatabase
import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.core.database.repository.GuideSearchRepository
import com.haji2027.core.database.repository.LocationQueueRepository
import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.feature.guide.data.HajjGuideRepository
import com.haji2027.feature.places.domain.CompassNavigationEngine
import com.haji2027.feature.places.domain.GpsAveragingEngine
import com.haji2027.feature.places.domain.LocationSampleProvider
import com.haji2027.feature.places.domain.OrientationSensorProvider
import com.haji2027.feature.settings.ota.DualLayerContentManager
import com.haji2027.feature.settings.ota.OtaEngine
import com.haji2027.feature.settings.purge.DataPurgeManager
import com.haji2027.feature.tracking.service.TrackingController
import com.haji2027.feature.tracking.sync.NetworkConnectivityProvider
import com.haji2027.feature.tracking.sync.SyncManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files

/**
 * Pengujian unit untuk memverifikasi kontrak antarmuka [AppContainer]
 * dan ketersediaan seluruh dependensi inti aplikasi tanpa dependensi mocking eksternal.
 */
class AppContainerTest {

    @Test
    fun `testAppContainerInterfaceContractSatisfaction`() {
        val tempDir = Files.createTempDirectory("container_test").toFile()
        try {
            val stubContainer = StubAppContainer(tempDir)

            assertNotNull(stubContainer.checklistRepository)
            assertNotNull(stubContainer.placesRepository)
            assertNotNull(stubContainer.guideSearchRepository)
            assertNotNull(stubContainer.locationQueueRepository)
            assertNotNull(stubContainer.authManager)
            assertNotNull(stubContainer.trackingApiClient)
            assertNotNull(stubContainer.trackingController)
            assertNotNull(stubContainer.networkConnectivityProvider)
            assertNotNull(stubContainer.syncManager)
            assertNotNull(stubContainer.dualLayerContentManager)
            assertNotNull(stubContainer.otaEngine)
            assertNotNull(stubContainer.dataPurgeManager)
            assertNotNull(stubContainer.hijriCalendarEngine)
            assertNotNull(stubContainer.locationSampleProvider)
            assertNotNull(stubContainer.gpsAveragingEngine)
            assertNotNull(stubContainer.orientationSensorProvider)
            assertNotNull(stubContainer.compassNavigationEngine)
            assertNotNull(stubContainer.guideRepository)

            // Verifikasi resolusi kalender Hijriah
            val date = stubContainer.hijriCalendarEngine.getCurrentHijriDate(0)
            assertNotNull(date)
            assertEquals("Zulhijah", stubContainer.hijriCalendarEngine.getMonthName(12))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}

private class StubAppContainer(tempDir: File) : AppContainer {
    override val database: AppDatabase get() = throw UnsupportedOperationException()
    override val checklistRepository: ChecklistRepository = object : ChecklistRepository {
        override fun getAllProgress() = kotlinx.coroutines.flow.emptyFlow<List<com.haji2027.core.model.ChecklistProgress>>()
        override fun getCompletedStepIds() = kotlinx.coroutines.flow.emptyFlow<List<String>>()
        override suspend fun isStepCompleted(stepId: String) = false
        override suspend fun setStepCompleted(stepId: String, isCompleted: Boolean) {}
        override suspend fun toggleStep(stepId: String): Boolean = true
        override suspend fun clearAll() {}
    }
    override val placesRepository: PlacesRepository = object : PlacesRepository {
        override fun getAllPlaces() = kotlinx.coroutines.flow.emptyFlow<List<com.haji2027.core.model.SavedPlace>>()
        override fun getPlacesByCategory(category: com.haji2027.core.model.PlaceCategory) = kotlinx.coroutines.flow.emptyFlow<List<com.haji2027.core.model.SavedPlace>>()
        override fun getPlaceById(placeId: String) = kotlinx.coroutines.flow.emptyFlow<com.haji2027.core.model.SavedPlace?>()
        override suspend fun savePlace(place: com.haji2027.core.model.SavedPlace) {}
        override suspend fun updatePlace(place: com.haji2027.core.model.SavedPlace) {}
        override suspend fun deletePlace(placeId: String) {}
        override suspend fun getPlacesCount() = 0
    }
    override val guideSearchRepository: GuideSearchRepository = object : GuideSearchRepository {
        override fun search(query: String) = kotlinx.coroutines.flow.emptyFlow<List<com.haji2027.core.database.entity.GuideSearchEntity>>()
        override suspend fun indexAll(hajjPackage: com.haji2027.core.model.HajjContentPackage) {}
        override suspend fun clearIndex() {}
    }
    override val locationQueueRepository: LocationQueueRepository = object : LocationQueueRepository {
        override suspend fun enqueueLocation(location: com.haji2027.core.model.UserLocation) = 1L
        override fun observeQueue() = kotlinx.coroutines.flow.emptyFlow<List<com.haji2027.core.database.entity.LocationSyncQueueEntity>>()
        override suspend fun getAllPendingLocations() = emptyList<com.haji2027.core.database.entity.LocationSyncQueueEntity>()
        override suspend fun getLatestPendingLocation() = null
        override suspend fun getPendingCount() = 0
        override suspend fun deleteLocation(queueId: Long) {}
        override suspend fun deleteBatch(queueIds: List<Long>) {}
        override suspend fun clearQueue() {}
    }
    override val authManager: AuthManager = object : AuthManager {
        override val authState = kotlinx.coroutines.flow.MutableStateFlow<com.haji2027.core.network.auth.AuthState>(com.haji2027.core.network.auth.AuthState.Unauthenticated)
        override suspend fun ensureAuthenticated() = Result.success(com.haji2027.core.network.auth.AuthUser("uid", true))
        override suspend fun signInAnonymously() = Result.success(com.haji2027.core.network.auth.AuthUser("uid", true))
        override suspend fun getCurrentUserId() = "uid"
        override suspend fun getIdToken(forceRefresh: Boolean) = Result.success("token")
        override suspend fun signOut() {}
        override suspend fun deleteAccount() = Result.success(Unit)
    }
    override val trackingApiClient: TrackingApiClient = object : TrackingApiClient {
        override suspend fun generatePairingToken(request: com.haji2027.core.network.dto.GenerateTokenRequest) = Result.failure<com.haji2027.core.network.dto.GenerateTokenResponse>(NotImplementedError())
        override suspend fun claimPairingToken(request: com.haji2027.core.network.dto.ClaimTokenRequest) = Result.failure<com.haji2027.core.network.dto.ClaimTokenResponse>(NotImplementedError())
        override suspend fun revokeTrackingAccess(request: com.haji2027.core.network.dto.RevokeAccessRequest) = Result.failure<com.haji2027.core.network.dto.RevokeAccessResponse>(NotImplementedError())
        override suspend fun purgeUserData() = Result.success(com.haji2027.core.network.dto.PurgeUserDataResponse("SUCCESS", "uid", "msg"))
        override suspend fun syncLocations(locations: List<com.haji2027.core.network.dto.LocationPointDto>) = Result.success(Unit)
    }
    override val trackingController: TrackingController = object : TrackingController {
        override val status = kotlinx.coroutines.flow.MutableStateFlow(com.haji2027.feature.tracking.service.TrackingServiceStatus())
        override fun startTracking(context: android.content.Context?, userId: String) {}
        override fun stopTracking(context: android.content.Context?) {}
    }
    override val networkConnectivityProvider: NetworkConnectivityProvider = object : NetworkConnectivityProvider {
        override fun isConnected() = true
        override fun observeConnectivity() = kotlinx.coroutines.flow.flowOf(true)
    }
    override val syncManager: SyncManager = object : SyncManager {
        override val syncState = kotlinx.coroutines.flow.MutableStateFlow<com.haji2027.feature.tracking.sync.SyncState>(com.haji2027.feature.tracking.sync.SyncState.Idle)
        override suspend fun enqueueLocation(location: com.haji2027.core.model.UserLocation) = 1L
        override suspend fun syncPendingLocations() = Result.success(0)
        override fun startListeningToTrackingService() {}
        override fun stopListeningToTrackingService() {}
    }
    override val dualLayerContentManager: DualLayerContentManager = DualLayerContentManager(tempDir) {
        ByteArrayInputStream("""{"version":"1.0.0","revision_date":"","approved_by":"","hajj_year":1448,"days":[]}""".toByteArray())
    }
    override val otaEngine: OtaEngine = OtaEngine()
    override val dataPurgeManager: DataPurgeManager = object : DataPurgeManager {
        override suspend fun executePurgeAllUserData() = Result.success(com.haji2027.feature.settings.purge.PurgeResult(true, true, true, true, "OK"))
    }
    override val hijriCalendarEngine: HijriCalendarEngine = HijriCalendarEngine()
    override val locationSampleProvider: LocationSampleProvider = object : LocationSampleProvider {
        override fun requestLocationUpdates(intervalMs: Long) = kotlinx.coroutines.flow.emptyFlow<com.haji2027.feature.places.domain.GpsSample>()
    }
    override val gpsAveragingEngine: GpsAveragingEngine = GpsAveragingEngine(locationSampleProvider)
    override val orientationSensorProvider: OrientationSensorProvider = object : OrientationSensorProvider {
        override fun observeOrientation(declinationDegrees: Float) = kotlinx.coroutines.flow.emptyFlow<com.haji2027.feature.places.domain.CompassOrientation>()
    }
    override val compassNavigationEngine: CompassNavigationEngine = CompassNavigationEngine()
    override val guideRepository: HajjGuideRepository = object : HajjGuideRepository {
        override suspend fun getContentPackage() = com.haji2027.core.model.HajjContentPackage("1.0.0", "", "", 1448)
        override suspend fun getAllDays() = emptyList<com.haji2027.core.model.HajjDay>()
        override suspend fun getDayById(dayId: String) = null
        override suspend fun getDayByHijri(hijriDay: Int, hijriMonth: Int) = null
        override suspend fun getReadingById(readingId: String) = null
    }
}
