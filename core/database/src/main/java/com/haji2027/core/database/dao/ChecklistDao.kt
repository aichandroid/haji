package com.haji2027.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haji2027.core.database.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk checklist progres manasik.
 */
@Dao
interface ChecklistDao {

    @Query("SELECT * FROM checklist_progress")
    fun getAllChecklistFlow(): Flow<List<ChecklistEntity>>

    @Query("SELECT step_id FROM checklist_progress WHERE is_completed = 1")
    fun getCompletedStepIdsFlow(): Flow<List<String>>

    @Query("SELECT * FROM checklist_progress WHERE step_id = :stepId LIMIT 1")
    suspend fun getChecklistByStepId(stepId: String): ChecklistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChecklist(entity: ChecklistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChecklists(entities: List<ChecklistEntity>)

    @Query("DELETE FROM checklist_progress WHERE step_id = :stepId")
    suspend fun deleteChecklistByStepId(stepId: String): Int

    @Query("DELETE FROM checklist_progress")
    suspend fun clearAllChecklist()
}
