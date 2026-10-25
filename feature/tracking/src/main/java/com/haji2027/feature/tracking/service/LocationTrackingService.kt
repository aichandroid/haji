package com.haji2027.feature.tracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.haji2027.core.model.UserLocation
import com.haji2027.feature.tracking.domain.MovementClassifier
import com.haji2027.feature.tracking.domain.MovementMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Android Foreground Service bertipe lokasi (Android 14+ / API 34+).
 * Berjalan di latar belakang untuk mengirimkan koordinat pelacakan keluarga secara persisten
 * dengan interval adaptif (30 dtk saat bergerak, 5 menit saat istirahat) guna menghemat baterai.
 *
 * Selaras dengan FR-TRACK-004, FR-TRACK-005, NFR-BAT-001, dan Section 17.5 SYSTEM_DESIGN_DOCUMENT.md.
 */
class LocationTrackingService : Service() {

    companion object {
        const val ACTION_START_TRACKING = "com.haji2027.tracking.ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.haji2027.tracking.ACTION_STOP_TRACKING"
        const val EXTRA_USER_ID = "com.haji2027.tracking.EXTRA_USER_ID"

        private val _serviceStatus = MutableStateFlow(TrackingServiceStatus())
        val serviceStatus: StateFlow<TrackingServiceStatus> = _serviceStatus.asStateFlow()

        // Listener hook untuk sinkronisasi antrean telemetri (TASK-15)
        var locationListener: ((UserLocation) -> Unit)? = null

        fun start(context: Context, userId: String? = null) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
                if (userId != null) putExtra(EXTRA_USER_ID, userId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var notificationHelper: TrackingNotificationHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val movementClassifier = MovementClassifier()

    private var activeUserId: String = "anonymous_target"
    private var currentLocationCallback: LocationCallback? = null
    private var currentIntervalMs: Long = MovementClassifier.DEFAULT_MOVING_INTERVAL_MS

    override fun onCreate() {
        super.onCreate()
        notificationHelper = TrackingNotificationHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_TRACKING

        when (action) {
            ACTION_START_TRACKING -> {
                val userId = intent?.getStringExtra(EXTRA_USER_ID)
                if (!userId.isNullOrBlank()) {
                    activeUserId = userId
                }
                startLocationTracking()
                return START_STICKY
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
                return START_NOT_STICKY
            }
            else -> return START_NOT_STICKY
        }
    }

    private fun startLocationTracking() {
        val notification = notificationHelper.buildNotification(movementClassifier.getCurrentMode())

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            TrackingNotificationHelper.NOTIFICATION_ID,
            notification,
            serviceType
        )

        _serviceStatus.update {
            it.copy(
                isRunning = true,
                startedAtEpochMs = if (it.startedAtEpochMs == 0L) System.currentTimeMillis() else it.startedAtEpochMs
            )
        }

        requestAdaptiveLocationUpdates(MovementClassifier.DEFAULT_MOVING_INTERVAL_MS, MovementClassifier.DEFAULT_MIN_DISPLACEMENT_METERS)
    }

    private fun requestAdaptiveLocationUpdates(intervalMs: Long, minDisplacementMeters: Float) {
        removeLocationUpdates()

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(minDisplacementMeters)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                onNewLocationReceived(
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracy = location.accuracy,
                    speed = if (location.hasSpeed()) location.speed else null,
                    timestamp = location.time
                )
            }
        }

        currentLocationCallback = callback
        currentIntervalMs = intervalMs

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // Izin dicabut oleh pengguna saat runtime
            stopLocationTracking()
        }
    }

    internal fun onNewLocationReceived(
        lat: Double,
        lng: Double,
        accuracy: Float,
        speed: Float?,
        timestamp: Long
    ) {
        val now = if (timestamp > 0) timestamp else System.currentTimeMillis()

        // Evaluasi mode mobilitas adaptif
        val evaluation = movementClassifier.onLocationSample(
            latitude = lat,
            longitude = lng,
            speedMps = speed,
            timestampMs = now
        )

        val userLocation = UserLocation(
            userId = activeUserId,
            latitude = lat,
            longitude = lng,
            accuracyMeters = accuracy,
            updatedAtEpochMs = now
        )

        // Teruskan sampel ke listener telemetri (TASK-15 Offline Queue)
        locationListener?.invoke(userLocation)

        // Perbarui state publik
        _serviceStatus.update {
            it.copy(
                movementMode = evaluation.mode,
                lastLocation = userLocation,
                totalUpdatesCount = it.totalUpdatesCount + 1
            )
        }

        // Perbarui notifikasi status bar
        val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(now))
        notificationHelper.updateNotification(
            movementMode = evaluation.mode,
            lastUpdatedText = "Update: $timeStr"
        )

        // Jika interval yang direkomendasikan berbeda dari interval aktif saat ini, sesuaikan
        if (evaluation.intervalMs != currentIntervalMs) {
            requestAdaptiveLocationUpdates(
                intervalMs = evaluation.intervalMs,
                minDisplacementMeters = evaluation.minDistanceMeters
            )
        }
    }

    private fun removeLocationUpdates() {
        currentLocationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            currentLocationCallback = null
        }
    }

    private fun stopLocationTracking() {
        removeLocationUpdates()
        _serviceStatus.update {
            it.copy(
                isRunning = false,
                movementMode = MovementMode.MOVING
            )
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopLocationTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
