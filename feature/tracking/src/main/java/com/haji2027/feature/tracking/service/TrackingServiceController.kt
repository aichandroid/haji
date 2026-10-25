package com.haji2027.feature.tracking.service

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Antarmuka pengendali layanan pelacakan latar belakang untuk lapisan UI dan ViewModel.
 */
interface TrackingController {
    val status: StateFlow<TrackingServiceStatus>
    fun startTracking(context: Context, userId: String)
    fun stopTracking(context: Context)
}

/**
 * Implementasi standar pengendali delegasi ke [LocationTrackingService].
 */
class DefaultTrackingController : TrackingController {

    override val status: StateFlow<TrackingServiceStatus> = LocationTrackingService.serviceStatus

    override fun startTracking(context: Context, userId: String) {
        LocationTrackingService.start(context, userId)
    }

    override fun stopTracking(context: Context) {
        LocationTrackingService.stop(context)
    }
}
