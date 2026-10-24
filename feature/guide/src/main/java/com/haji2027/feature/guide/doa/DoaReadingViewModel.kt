package com.haji2027.feature.guide.doa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.feature.guide.data.HajjGuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Layar Bacaan Doa Multilingual (Arab, Latin, Terjemahan).
 */
class DoaReadingViewModel(
    private val readingId: String,
    private val guideRepository: HajjGuideRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoaReadingUiState())
    val uiState: StateFlow<DoaReadingUiState> = _uiState.asStateFlow()

    init {
        loadReading()
    }

    fun loadReading() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val reading = guideRepository.getReadingById(readingId)
                if (reading != null) {
                    _uiState.update { it.copy(isLoading = false, reading = reading) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Doa tidak ditemukan.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun toggleTextSize() {
        _uiState.update { current ->
            val nextScale = if (current.textSizeScale == TextSizeScale.NORMAL) {
                TextSizeScale.LARGE
            } else {
                TextSizeScale.NORMAL
            }
            current.copy(textSizeScale = nextScale)
        }
    }
}
