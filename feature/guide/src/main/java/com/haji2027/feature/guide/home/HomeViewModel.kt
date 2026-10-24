package com.haji2027.feature.guide.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.core.common.calendar.HijriCalendarEngine
import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.core.model.HajjDay
import com.haji2027.feature.guide.data.HajjGuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * ViewModel untuk Beranda Utama ("Hari Ini").
 * Menghubungkan perhitungan kalender Hijriah Umm al-Qura, konten manasik berversi,
 * dan status progres checklist persisten dari Room Database.
 */
class HomeViewModel(
    private val guideRepository: HajjGuideRepository,
    private val checklistRepository: ChecklistRepository,
    private val calendarEngine: HijriCalendarEngine = HijriCalendarEngine,
    private val currentDateProvider: () -> LocalDate = { LocalDate.now(HijriCalendarEngine.SAUDI_ZONE) }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentOffsetDays: Int = 0
    private var allDays: List<HajjDay> = emptyList()
    private var completedStepIds: Set<String> = emptySet()

    init {
        loadData()
        observeChecklist()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                allDays = guideRepository.getAllDays()
                refreshDateAndProgress()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat data panduan manasik: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun observeChecklist() {
        checklistRepository.observeCompletedStepIds()
            .onEach { completedIds ->
                completedStepIds = completedIds
                refreshDateAndProgress()
            }
            .catch {
                // Pertahankan data jika terjadi error saat membaca checklist
            }
            .launchIn(viewModelScope)
    }

    /**
     * Mengatur penyesuaian manual tanggal Hijriah (-3 s.d. +3 hari).
     */
    fun setManualOffset(offsetDays: Int) {
        currentOffsetDays = calendarEngine.clampOffset(offsetDays)
        refreshDateAndProgress()
    }

    /**
     * Memperbarui kalkulasi tanggal dan progres manasik hari ini.
     */
    fun refreshDateAndProgress() {
        val today = currentDateProvider()
        val hijriDate = calendarEngine.calculateHijriDate(today, currentOffsetDays)
        val gregorianFormatted = formatGregorianDate(today)

        // Cari hari manasik yang cocok dengan tanggal Hijriah saat ini
        val matchedDay = allDays.find {
            it.hijriDay == hijriDate.dayOfMonth && it.hijriMonth == hijriDate.monthNumber
        } ?: allDays.firstOrNull() // Fallback ke hari pertama (Persiapan/Umrah) jika di luar musim haji

        // Hitung progres langkah manasik hari ini
        val totalSteps = matchedDay?.activities?.sumOf { it.steps.size } ?: 0
        val completedSteps = matchedDay?.activities?.sumOf { activity ->
            activity.steps.count { step -> completedStepIds.contains(step.stepId) }
        } ?: 0

        val progressPercent = if (totalSteps > 0) {
            (completedSteps * 100) / totalSteps
        } else {
            0
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                hijriDate = hijriDate,
                hijriDateFormatted = hijriDate.formattedDate,
                gregorianDateFormatted = gregorianFormatted,
                activeDay = matchedDay,
                isHajjPeakActive = hijriDate.isHajjSeason,
                totalStepsToday = totalSteps,
                completedStepsToday = completedSteps,
                progressPercentage = progressPercent,
                appliedOffsetDays = currentOffsetDays,
                isOfflineReady = true,
                errorMessage = null
            )
        }
    }

    private fun formatGregorianDate(date: LocalDate): String {
        val localeId = Locale("id", "ID")
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId)
        return date.format(formatter)
    }
}
