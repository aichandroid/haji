package com.haji2027.feature.guide.detail

import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.core.model.ChecklistProgress
import com.haji2027.feature.guide.data.AssetHajjGuideRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class GuideDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var guideRepository: AssetHajjGuideRepository
    private lateinit var fakeChecklistRepository: FakeChecklistRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val assetFile = File("../../app/src/main/assets/content_v1.0.0.json")
            .takeIf { it.exists() }
            ?: File("app/src/main/assets/content_v1.0.0.json")

        guideRepository = AssetHajjGuideRepository {
            assetFile.readText()
        }

        fakeChecklistRepository = FakeChecklistRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel loads valid day details and activities`() = testScope.runTest {
        val viewModel = GuideDetailViewModel(
            dayId = "day_09_zulhijah",
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.day)
        assertEquals("day_09_zulhijah", state.day!!.dayId)
        assertTrue(state.totalSteps > 0)
        assertEquals(0, state.completedSteps)
        assertEquals(0, state.progressPercentage)
    }

    @Test
    fun `viewModel toggles step completion and updates progress reactively`() = testScope.runTest {
        val viewModel = GuideDetailViewModel(
            dayId = "day_09_zulhijah",
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val firstStep = viewModel.uiState.value.day!!.activities.first().steps.first()
        val stepId = firstStep.stepId

        // Awalnya belum selesai
        assertFalse(viewModel.uiState.value.completedStepIds.contains(stepId))

        // Toggle menjadi selesai
        viewModel.toggleStepCompleted(stepId, false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.completedStepIds.contains(stepId))
        assertEquals(1, viewModel.uiState.value.completedSteps)

        // Toggle kembali menjadi belum selesai
        viewModel.toggleStepCompleted(stepId, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.completedStepIds.contains(stepId))
        assertEquals(0, viewModel.uiState.value.completedSteps)
    }

    @Test
    fun `viewModel sets error when dayId is not found`() = testScope.runTest {
        val viewModel = GuideDetailViewModel(
            dayId = "day_non_existent",
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    private class FakeChecklistRepository : ChecklistRepository {
        private val completedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

        override fun observeAllChecklist(): Flow<List<ChecklistProgress>> {
            return completedIdsFlow.map { set -> set.map { ChecklistProgress(it, true) } }
        }

        override fun observeCompletedStepIds(): Flow<Set<String>> = completedIdsFlow

        override fun isStepCompleted(stepId: String): Flow<Boolean> {
            return completedIdsFlow.map { it.contains(stepId) }
        }

        override suspend fun getStepProgress(stepId: String): ChecklistProgress? {
            return if (completedIdsFlow.value.contains(stepId)) ChecklistProgress(stepId, true) else null
        }

        override suspend fun setStepCompleted(stepId: String, isCompleted: Boolean, timestampMs: Long?) {
            val current = completedIdsFlow.value.toMutableSet()
            if (isCompleted) current.add(stepId) else current.remove(stepId)
            completedIdsFlow.value = current
        }

        override suspend fun clearAll() {
            completedIdsFlow.value = emptySet()
        }
    }
}
