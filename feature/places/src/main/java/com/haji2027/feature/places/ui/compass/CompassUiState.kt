package com.haji2027.feature.places.ui.compass

import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.CompassOrientation
import com.haji2027.feature.places.domain.GpsSample
import com.haji2027.feature.places.domain.NavigationTargetBearing

/**
 * State antarmuka untuk layar kompas panah navigasi spasial (SCR-04).
 */
data class CompassUiState(
    val targetPlace: SavedPlace? = null,
    val currentLocation: GpsSample? = null,
    val orientation: CompassOrientation? = null,
    val navigationBearing: NavigationTargetBearing? = null,
    val showCalibrationDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val isTargetDeleted: Boolean = false,
    val isWaitingForGps: Boolean = true,
    val errorMessage: String? = null
)
