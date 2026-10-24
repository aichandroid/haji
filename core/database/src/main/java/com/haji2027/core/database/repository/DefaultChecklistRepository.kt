package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.ChecklistDao
import com.haji2027.core.database.entity.ChecklistEntity
import com.haji2027.core.model.ChecklistProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi default ChecklistRepository menggunakan ChecklistDao Room.
 */
class DefaultChecklistRepository(
    private val checklistDao: ChecklistDao
) : ChecklistRepository {

    override fun observeAllChecklist(): Flow<List<ChecklistProgress>> {
        return checklistDao.getAllChecklistFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeCompletedStepIds(): Flow<Set<String>> {
        return checklistDao.getCompletedStepIdsFlow().map { it.toSet() }
    }

    override fun isStepCompleted(stepId: String): Flow<Boolean> {
        return checklistDao.getAllChecklistFlow().map { list ->
            list.any { it.stepId == stepId && it.isCompleted }
        }
    }

    override suspend fun getStepProgress(stepId: String): ChecklistProgress? {
        return checklistDao.getChecklistByStepId(stepId)?.toDomain()
    }

    override suspend fun setStepCompleted(stepId: String, isCompleted: Boolean, timestampMs: Long?) {
        val completedAt = if (isCompleted) (timestampMs ?: System.currentTimeMillis()) else null
        checklistDao.upsertChecklist(
            ChecklistEntity(
                stepId = stepId,
                isCompleted = isCompleted,
                completedAtEpochMs = completedAt
            )
        )
    }

    override suspend fun clearAll() {
        checklistDao.clearAllChecklist()
    }
}
