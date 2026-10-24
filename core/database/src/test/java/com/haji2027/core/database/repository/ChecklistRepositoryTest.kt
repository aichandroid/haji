package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.ChecklistDao
import com.haji2027.core.database.entity.ChecklistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChecklistRepositoryTest {

    private lateinit var fakeDao: FakeChecklistDao
    private lateinit var repository: ChecklistRepository

    @Before
    fun setup() {
        fakeDao = FakeChecklistDao()
        repository = DefaultChecklistRepository(fakeDao)
    }

    @Test
    fun `setStepCompleted updates state and completed timestamp`() = runBlocking {
        val stepId = "step_wukuf_01"
        repository.setStepCompleted(stepId, true, timestampMs = 1715000000000L)

        val progress = repository.getStepProgress(stepId)
        assertNotNull(progress)
        assertTrue(progress!!.isCompleted)
        assertEquals(1715000000000L, progress.completedAtEpochMs)

        val completedIds = repository.observeCompletedStepIds().first()
        assertTrue(completedIds.contains(stepId))
    }

    @Test
    fun `setStepCompleted to false clears timestamp`() = runBlocking {
        val stepId = "step_tawaf_01"
        repository.setStepCompleted(stepId, true, timestampMs = 1715000000000L)
        repository.setStepCompleted(stepId, false)

        val progress = repository.getStepProgress(stepId)
        assertNotNull(progress)
        assertFalse(progress!!.isCompleted)
        assertNull(progress.completedAtEpochMs)

        val completedIds = repository.observeCompletedStepIds().first()
        assertFalse(completedIds.contains(stepId))
    }

    @Test
    fun `isStepCompleted returns correct reactive boolean`() = runBlocking {
        val stepId = "step_ihram_01"
        assertFalse(repository.isStepCompleted(stepId).first())

        repository.setStepCompleted(stepId, true)
        assertTrue(repository.isStepCompleted(stepId).first())
    }

    @Test
    fun `clearAll removes all checklist records`() = runBlocking {
        repository.setStepCompleted("step_1", true)
        repository.setStepCompleted("step_2", true)

        assertEquals(2, repository.observeAllChecklist().first().size)

        repository.clearAll()
        assertEquals(0, repository.observeAllChecklist().first().size)
    }

    private class FakeChecklistDao : ChecklistDao {
        private val state = MutableStateFlow<Map<String, ChecklistEntity>>(emptyMap())

        override fun getAllChecklistFlow(): Flow<List<ChecklistEntity>> {
            return state.map { it.values.toList() }
        }

        override fun getCompletedStepIdsFlow(): Flow<List<String>> {
            return state.map { map ->
                map.values.filter { it.isCompleted }.map { it.stepId }
            }
        }

        override suspend fun getChecklistByStepId(stepId: String): ChecklistEntity? {
            return state.value[stepId]
        }

        override suspend fun upsertChecklist(entity: ChecklistEntity) {
            val current = state.value.toMutableMap()
            current[entity.stepId] = entity
            state.value = current
        }

        override suspend fun upsertChecklists(entities: List<ChecklistEntity>) {
            val current = state.value.toMutableMap()
            entities.forEach { current[it.stepId] = it }
            state.value = current
        }

        override suspend fun deleteChecklistByStepId(stepId: String): Int {
            val current = state.value.toMutableMap()
            val removed = current.remove(stepId)
            state.value = current
            return if (removed != null) 1 else 0
        }

        override suspend fun clearAllChecklist() {
            state.value = emptyMap()
        }
    }
}
