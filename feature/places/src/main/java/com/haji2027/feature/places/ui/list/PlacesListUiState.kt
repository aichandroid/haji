package com.haji2027.feature.places.ui.list

import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.AveragedLocation
import com.haji2027.feature.places.domain.GpsSample

/**
 * State antarmuka untuk layar daftar tempat tersimpan.
 */
data class PlacesListUiState(
    val places: List<SavedPlace> = emptyList(),
    val selectedCategory: PlaceCategory? = null,
    val currentGpsLocation: GpsSample? = null,
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val isSamplingGps: Boolean = false,
    val samplingProgress: Int = 0,
    val samplingTarget: Int = 5,
    val latestSamplingAccuracy: Float? = null,
    val sampledLocation: AveragedLocation? = null,
    val samplingErrorMessage: String? = null
)
