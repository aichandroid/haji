package com.haji2027.app.di

import android.content.Context
import android.hardware.SensorManager
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.haji2027.core.common.calendar.HijriCalendarEngine
import com.haji2027.core.database.AppDatabase
import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.core.database.repository.DefaultChecklistRepository
import com.haji2027.core.database.repository.DefaultGuideSearchRepository
import com.haji2027.core.database.repository.DefaultLocationQueueRepository
import com.haji2027.core.database.repository.DefaultPlacesRepository
import com.haji2027.core.database.repository.GuideSearchRepository
import com.haji2027.core.database.repository.LocationQueueRepository
import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.network.api.DefaultFirebaseFunctionsCaller
import com.haji2027.core.network.api.DefaultTrackingApiClient
import com.haji2027.core.network.api.FunctionsCaller
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.auth.DefaultFirebaseAuthManager
import com.haji2027.core.network.auth.DefaultFirebaseAuthProvider
import com.haji2027.feature.places.domain.AndroidOrientationSensorProvider
import com.haji2027.feature.places.domain.CompassNavigationEngine
import com.haji2027.feature.places.domain.FusedLocationSampleProvider
import com.haji2027.feature.places.domain.GpsAveragingEngine
import com.haji2027.feature.places.domain.LocationSampleProvider
import com.haji2027.feature.places.domain.OrientationSensorProvider
import com.haji2027.feature.settings.ota.DualLayerContentManager
import com.haji2027.feature.settings.ota.OtaEngine
import com.haji2027.feature.settings.purge.DataPurgeManager
import com.haji2027.feature.settings.purge.DefaultDataPurgeManager
import com.haji2027.feature.tracking.service.DefaultTrackingController
import com.haji2027.feature.tracking.service.TrackingController
import com.haji2027.feature.tracking.sync.AndroidNetworkConnectivityProvider
import com.haji2027.feature.tracking.sync.DefaultSyncManager
import com.haji2027.feature.tracking.sync.NetworkConnectivityProvider
import com.haji2027.feature.tracking.sync.SyncManager
import java.io.File

/**
 * Kontrak penyedia dependensi aplikasi (Dependency Injection / Service Locator).
 */
interface AppContainer {
    val database: AppDatabase
    val checklistRepository: ChecklistRepository
    val placesRepository: PlacesRepository
    val guideSearchRepository: GuideSearchRepository
    val locationQueueRepository: LocationQueueRepository
    val authManager: AuthManager
    val trackingApiClient: TrackingApiClient
    val trackingController: TrackingController
    val networkConnectivityProvider: NetworkConnectivityProvider
    val syncManager: SyncManager
    val dualLayerContentManager: DualLayerContentManager
    val otaEngine: OtaEngine
    val dataPurgeManager: DataPurgeManager
    val hijriCalendarEngine: HijriCalendarEngine
    val locationSampleProvider: LocationSampleProvider
    val gpsAveragingEngine: GpsAveragingEngine
    val orientationSensorProvider: OrientationSensorProvider
    val compassNavigationEngine: CompassNavigationEngine
    val guideRepository: com.haji2027.feature.guide.data.HajjGuideRepository
}

/**
 * Implementasi standar [AppContainer] yang menginisialisasi singleton secara lazy.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    override val checklistRepository: ChecklistRepository by lazy {
        DefaultChecklistRepository(database.checklistDao())
    }

    override val placesRepository: PlacesRepository by lazy {
        DefaultPlacesRepository(database.placesDao())
    }

    override val guideSearchRepository: GuideSearchRepository by lazy {
        DefaultGuideSearchRepository(database.guideSearchDao())
    }

    override val locationQueueRepository: LocationQueueRepository by lazy {
        DefaultLocationQueueRepository(database.locationSyncQueueDao())
    }

    override val authManager: AuthManager by lazy {
        DefaultFirebaseAuthManager(DefaultFirebaseAuthProvider())
    }

    private val functionsCaller: FunctionsCaller by lazy {
        DefaultFirebaseFunctionsCaller()
    }

    override val trackingApiClient: TrackingApiClient by lazy {
        DefaultTrackingApiClient(authManager, functionsCaller)
    }

    override val trackingController: TrackingController by lazy {
        DefaultTrackingController()
    }

    override val networkConnectivityProvider: NetworkConnectivityProvider by lazy {
        AndroidNetworkConnectivityProvider(context)
    }

    override val syncManager: SyncManager by lazy {
        DefaultSyncManager(
            queueRepository = locationQueueRepository,
            apiClient = trackingApiClient,
            networkProvider = networkConnectivityProvider
        )
    }

    private val contentDir: File by lazy {
        File(context.filesDir, "content")
    }

    override val dualLayerContentManager: DualLayerContentManager by lazy {
        DualLayerContentManager(contentDir) {
            context.assets.open("content_v1.0.0.json")
        }
    }

    override val otaEngine: OtaEngine by lazy {
        OtaEngine()
    }

    override val dataPurgeManager: DataPurgeManager by lazy {
        DefaultDataPurgeManager(
            checklistDao = database.checklistDao(),
            placesDao = database.placesDao(),
            locationSyncQueueDao = database.locationSyncQueueDao(),
            trackingApiClient = trackingApiClient,
            authManager = authManager,
            contentDir = contentDir
        )
    }

    override val hijriCalendarEngine: HijriCalendarEngine by lazy {
        HijriCalendarEngine()
    }

    override val locationSampleProvider: LocationSampleProvider by lazy {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        FusedLocationSampleProvider(fusedClient)
    }

    override val gpsAveragingEngine: GpsAveragingEngine by lazy {
        GpsAveragingEngine(locationSampleProvider)
    }

    override val orientationSensorProvider: OrientationSensorProvider by lazy {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        AndroidOrientationSensorProvider(sensorManager)
    }

    override val compassNavigationEngine: CompassNavigationEngine by lazy {
        CompassNavigationEngine()
    }

    override val guideRepository: com.haji2027.feature.guide.data.HajjGuideRepository by lazy {
        com.haji2027.feature.guide.data.AssetHajjGuideRepository {
            val otaContent = dualLayerContentManager.loadActiveContent().getOrNull()
            if (otaContent != null && otaContent.localFile != null && otaContent.localFile.exists()) {
                otaContent.localFile.readText(Charsets.UTF_8)
            } else {
                context.assets.open("content_v1.0.0.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        }
    }
}
