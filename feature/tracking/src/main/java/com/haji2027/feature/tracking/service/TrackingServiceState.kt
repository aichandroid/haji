package com.haji2027.feature.tracking.service

import com.haji2027.core.model.UserLocation
import com.haji2027.feature.tracking.domain.MovementMode

/**
 * Status reaktif dari Android Location Foreground Service untuk dipantau UI dan ViewModel.
 */
data class TrackingServiceStatus(
    val isRunning: Boolean = false,
    val movementMode: MovementMode = MovementMode.MOVING,
    val lastLocation: UserLocation? = null,
    val startedAtEpochMs: Long = 0L,
    val totalUpdatesCount: Long = 0L
)
