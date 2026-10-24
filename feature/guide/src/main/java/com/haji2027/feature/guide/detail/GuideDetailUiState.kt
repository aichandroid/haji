package com.haji2027.feature.guide.detail

import com.haji2027.core.model.HajjDay

/**
 * State antarmuka untuk Layar SCR-02: Detail Kegiatan Manasik & Checklist.
 */
data class GuideDetailUiState(
    val isLoading: Boolean = true,
    val day: HajjDay? = null,
    val completedStepIds: Set<String> = emptySet(),
    val errorMessage: String? = null
) {
    val totalSteps: Int
        get() = day?.activities?.sumOf { it.steps.size } ?: 0

    val completedSteps: Int
        get() = day?.activities?.sumOf { activity ->
            activity.steps.count { completedStepIds.contains(it.stepId) }
        } ?: 0

    val progressFraction: Float
        get() = if (totalSteps > 0) completedSteps.toFloat() / totalSteps.toFloat() else 0f

    val progressPercentage: Int
        get() = if (totalSteps > 0) (completedSteps * 100) / totalSteps else 0
}
