package com.haji2027.core.database.repository

import com.haji2027.core.model.ChecklistProgress
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk mengelola progres checklist manasik jemaah.
 */
interface ChecklistRepository {
    fun observeAllChecklist(): Flow<List<ChecklistProgress>>
    fun observeCompletedStepIds(): Flow<Set<String>>
    fun isStepCompleted(stepId: String): Flow<Boolean>
    suspend fun getStepProgress(stepId: String): ChecklistProgress?
    suspend fun setStepCompleted(stepId: String, isCompleted: Boolean, timestampMs: Long? = null)
    suspend fun clearAll()
}
