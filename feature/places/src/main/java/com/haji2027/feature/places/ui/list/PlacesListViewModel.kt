package com.haji2027.feature.places.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.AveragedLocation
import com.haji2027.feature.places.domain.GpsAveragingEngine
import com.haji2027.feature.places.domain.GpsSample
import com.haji2027.feature.places.domain.GpsSamplingError
import com.haji2027.feature.places.domain.GpsSamplingState
import com.haji2027.feature.places.domain.LocationSampleProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel untuk mengelola daftar tempat tersimpan dan proses penandaan tempat baru.
 */
class PlacesListViewModel(
    private val placesRepository: PlacesRepository,
    private val averagingEngine: GpsAveragingEngine,
    private val locationProvider: LocationSampleProvider? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacesListUiState(isLoading = true))
    val uiState: StateFlow<PlacesListUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<PlaceCategory?>(null)
    private var samplingJob: Job? = null

    init {
        loadPlaces()
        observeLiveLocation()
    }

    private fun loadPlaces() {
        viewModelScope.launch {
            combine(
                placesRepository.observeAllPlaces(),
                _selectedCategory
            ) { allPlaces, category ->
                if (category == null) {
                    allPlaces
                } else {
                    allPlaces.filter { it.category == category }
                }
            }.collect { filteredPlaces ->
                _uiState.update {
                    it.copy(
                        places = filteredPlaces,
                        selectedCategory = _selectedCategory.value,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeLiveLocation() {
        locationProvider?.let { provider ->
            viewModelScope.launch {
                provider.requestLocationUpdates(intervalMs = 2000L).collect { sample ->
                    _uiState.update { it.copy(currentGpsLocation = sample) }
                }
            }
        }
    }

    fun setCategoryFilter(category: PlaceCategory?) {
        _selectedCategory.value = category
    }

    fun openAddPlaceDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                sampledLocation = null,
                samplingErrorMessage = null,
                samplingProgress = 0
            )
        }
        startGpsSampling()
    }

    fun closeAddPlaceDialog() {
        samplingJob?.cancel()
        _uiState.update {
            it.copy(
                showAddDialog = false,
                isSamplingGps = false,
                sampledLocation = null,
                samplingErrorMessage = null
            )
        }
    }

    fun retrySampling() {
        startGpsSampling()
    }

    private fun startGpsSampling() {
        samplingJob?.cancel()
        samplingJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSamplingGps = true,
                    samplingProgress = 0,
                    sampledLocation = null,
                    samplingErrorMessage = null
                )
            }

            averagingEngine.startSampling().collect { state ->
                when (state) {
                    is GpsSamplingState.Idle -> Unit
                    is GpsSamplingState.Sampling -> {
                        _uiState.update {
                            it.copy(
                                isSamplingGps = true,
                                samplingProgress = state.collectedSamples,
                                samplingTarget = state.targetSamples,
                                latestSamplingAccuracy = state.latestAccuracy
                            )
                        }
                    }
                    is GpsSamplingState.Success -> {
                        _uiState.update {
                            it.copy(
                                isSamplingGps = false,
                                sampledLocation = state.location,
                                samplingErrorMessage = null
                            )
                        }
                    }
                    is GpsSamplingState.Error -> {
                        val errorText = when (state.reason) {
                            GpsSamplingError.TIMEOUT_INSUFFICIENT_SAMPLES -> "Sinyal GPS lemah atau terhalang gedung."
                            GpsSamplingError.POOR_ACCURACY_ALL_FILTERED -> "Akurasi GPS rendah (> 25m)."
                            GpsSamplingError.PERMISSION_DENIED -> "Izin lokasi tidak diberikan."
                            GpsSamplingError.GPS_DISABLED -> "Layanan GPS perangkat belum aktif."
                        }
                        _uiState.update {
                            it.copy(
                                isSamplingGps = false,
                                samplingErrorMessage = errorText
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveNewPlace(
        name: String,
        category: PlaceCategory,
        notes: String?,
        location: AveragedLocation
    ) {
        viewModelScope.launch {
            val newPlace = SavedPlace(
                placeId = "place_${UUID.randomUUID()}",
                name = name,
                category = category,
                latitude = location.latitude,
                longitude = location.longitude,
                accuracyMeters = location.accuracyMeters,
                notes = notes,
                createdAtEpochMs = System.currentTimeMillis()
            )
            placesRepository.savePlace(newPlace)
            closeAddPlaceDialog()
        }
    }

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            placesRepository.deletePlaceById(placeId)
        }
    }
}
