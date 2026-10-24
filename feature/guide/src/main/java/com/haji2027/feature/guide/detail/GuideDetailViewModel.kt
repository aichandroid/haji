package com.haji2027.feature.guide.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.feature.guide.data.HajjGuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Layar Detail Kegiatan Manasik & Checklist Progres.
 */
class GuideDetailViewModel(
    private val dayId: String,
    private val guideRepository: HajjGuideRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuideDetailUiState())
    val uiState: StateFlow<GuideDetailUiState> = _uiState.asStateFlow()

    init {
        loadDayDetail()
        observeChecklist()
    }

    fun loadDayDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val day = guideRepository.getDayById(dayId)
                if (day != null) {
                    _uiState.update { it.copy(isLoading = false, day = day) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Data hari manasik tidak ditemukan.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    private fun observeChecklist() {
        checklistRepository.observeCompletedStepIds()
            .onEach { completedIds ->
                _uiState.update { it.copy(completedStepIds = completedIds) }
            }
            .catch {
                // Pertahankan status checklist yang ada jika terjadi gangguan
            }
            .launchIn(viewModelScope)
    }

    fun toggleStepCompleted(stepId: String, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            checklistRepository.setStepCompleted(stepId, !isCurrentlyCompleted)
        }
    }
}
