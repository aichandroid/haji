package com.haji2027.feature.guide.home

import com.haji2027.core.model.HajjDay
import com.haji2027.core.model.HijriDateModel

/**
 * State antarmuka untuk Beranda Utama ("Hari Ini").
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val hijriDate: HijriDateModel? = null,
    val hijriDateFormatted: String = "",
    val gregorianDateFormatted: String = "",
    val activeDay: HajjDay? = null,
    val isHajjPeakActive: Boolean = false,
    val totalStepsToday: Int = 0,
    val completedStepsToday: Int = 0,
    val progressPercentage: Int = 0,
    val isOfflineReady: Boolean = true,
    val appliedOffsetDays: Int = 0,
    val errorMessage: String? = null
) {
    val progressFraction: Float
        get() = if (totalStepsToday > 0) completedStepsToday.toFloat() / totalStepsToday.toFloat() else 0f
}
