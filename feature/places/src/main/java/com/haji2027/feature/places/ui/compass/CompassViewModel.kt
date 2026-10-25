package com.haji2027.feature.places.ui.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.CompassAccuracy
import com.haji2027.feature.places.domain.CompassNavigationEngine
import com.haji2027.feature.places.domain.CompassOrientation
import com.haji2027.feature.places.domain.GpsSample
import com.haji2027.feature.places.domain.LocationSampleProvider
import com.haji2027.feature.places.domain.OrientationSensorProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk layar kompas navigasi spasial real-time (SCR-04).
 */
class CompassViewModel(
    private val targetPlaceId: String,
    private val placesRepository: PlacesRepository,
    private val navigationEngine: CompassNavigationEngine,
    private val locationProvider: LocationSampleProvider,
    private val sensorProvider: OrientationSensorProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompassUiState())
    val uiState: StateFlow<CompassUiState> = _uiState.asStateFlow()

    private val _targetPlace = MutableStateFlow<SavedPlace?>(null)
    private val _currentLocation = MutableStateFlow<GpsSample?>(null)
    private val _currentOrientation = MutableStateFlow<CompassOrientation?>(null)

    init {
        loadTargetPlace()
        startSensorsAndNavigation()
    }

    private fun loadTargetPlace() {
        viewModelScope.launch {
            val place = placesRepository.getPlaceById(targetPlaceId)
            _targetPlace.value = place
            _uiState.update { it.copy(targetPlace = place) }
        }
    }

    private fun startSensorsAndNavigation() {
        // Aliran GPS Lokasi Pengguna
        viewModelScope.launch {
            locationProvider.requestLocationUpdates(intervalMs = 1000L).collect { sample ->
                _currentLocation.value = sample
                _uiState.update { it.copy(currentLocation = sample, isWaitingForGps = false) }
            }
        }

        // Aliran Sensor Kompas Perangkat
        viewModelScope.launch {
            sensorProvider.observeOrientation().collect { orientation ->
                _currentOrientation.value = orientation
                _uiState.update {
                    it.copy(
                        orientation = orientation,
                        showCalibrationDialog = if (orientation.accuracy == CompassAccuracy.UNRELIABLE) true else it.showCalibrationDialog
                    )
                }
            }
        }

        // Fusi Data Navigasi Real-Time
        viewModelScope.launch {
            combine(
                _targetPlace,
                _currentLocation,
                _currentOrientation
            ) { target, loc, orientation ->
                Triple(target, loc, orientation)
            }.collect { (target, loc, orientation) ->
                if (target != null && loc != null && orientation != null) {
                    val bearing = navigationEngine.computeNavigation(
                        currentLat = loc.latitude,
                        currentLng = loc.longitude,
                        gpsAccuracyMeters = loc.accuracyMeters,
                        targetLat = target.latitude,
                        targetLng = target.longitude,
                        orientation = orientation
                    )
                    _uiState.update {
                        it.copy(
                            navigationBearing = bearing,
                            isWaitingForGps = false
                        )
                    }
                }
            }
        }
    }

    fun openCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = true) }
    }

    fun dismissCalibrationDialog() {
        _uiState.update { it.copy(showCalibrationDialog = false) }
    }

    fun openDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun confirmDeletePlace() {
        viewModelScope.launch {
            placesRepository.deletePlaceById(targetPlaceId)
            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = false,
                    isTargetDeleted = true
                )
            }
        }
    }
}
