package com.haji2027.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Status penyelesaian langkah manasik haji.
 */
@Serializable
data class ChecklistProgress(
    @SerialName("step_id") val stepId: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("completed_at_epoch_ms") val completedAtEpochMs: Long? = null
)
