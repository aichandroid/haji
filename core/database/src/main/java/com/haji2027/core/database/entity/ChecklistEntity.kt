package com.haji2027.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haji2027.core.model.ChecklistProgress

/**
 * Entitas Room untuk tabel `checklist_progress`.
 * Menyimpan status penyelesaian langkah-langkah manasik secara persisten dan offline.
 */
@Entity(tableName = "checklist_progress")
data class ChecklistEntity(
    @PrimaryKey
    @ColumnInfo(name = "step_id")
    val stepId: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "completed_at_epoch_ms")
    val completedAtEpochMs: Long? = null
) {
    fun toDomain(): ChecklistProgress = ChecklistProgress(
        stepId = stepId,
        isCompleted = isCompleted,
        completedAtEpochMs = completedAtEpochMs
    )

    companion object {
        fun fromDomain(domain: ChecklistProgress): ChecklistEntity = ChecklistEntity(
            stepId = domain.stepId,
            isCompleted = domain.isCompleted,
            completedAtEpochMs = domain.completedAtEpochMs
        )
    }
}
