package com.haji2027.feature.guide.home

import com.haji2027.core.common.calendar.HijriCalendarEngine
import com.haji2027.core.database.repository.ChecklistRepository
import com.haji2027.core.model.ChecklistProgress
import com.haji2027.feature.guide.data.AssetHajjGuideRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

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
    fun `viewModel loads active day matching 9 Zulhijah as Hari Arafah`() = testScope.runTest {
        // 20 Mei 2027 = 9 Zulhijah 1448 H
        val arafahDate = LocalDate.of(2027, 5, 20)

        val viewModel = HomeViewModel(
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository,
            currentDateProvider = { arafahDate }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("State should not be loading", state.isLoading)
        assertNotNull("Active day should not be null", state.activeDay)
        assertEquals("day_09_zulhijah", state.activeDay!!.dayId)
        assertTrue("Should be marked as hajj peak", state.isHajjPeakActive)
        assertTrue("Formatted hijri date should contain 9 Zulhijah", state.hijriDateFormatted.contains("9 Zulhijah"))
    }

    @Test
    fun `viewModel applies manual offset to advance from 8 to 9 Zulhijah`() = testScope.runTest {
        // 19 Mei 2027 = 8 Zulhijah 1448 H (Hari Tarwiyah)
        val tarwiyahDate = LocalDate.of(2027, 5, 19)

        val viewModel = HomeViewModel(
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository,
            currentDateProvider = { tarwiyahDate }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("day_08_zulhijah", viewModel.uiState.value.activeDay!!.dayId)

        // Terapkan manual offset +1 hari
        viewModel.setManualOffset(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(1, updatedState.appliedOffsetDays)
        assertEquals("day_09_zulhijah", updatedState.activeDay!!.dayId)
        assertTrue(updatedState.hijriDateFormatted.contains("9 Zulhijah"))
    }

    @Test
    fun `viewModel calculates step progress percentage accurately`() = testScope.runTest {
        val arafahDate = LocalDate.of(2027, 5, 20)

        val viewModel = HomeViewModel(
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository,
            currentDateProvider = { arafahDate }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val activeDay = viewModel.uiState.value.activeDay!!
        val allStepsInDay = activeDay.activities.flatMap { it.steps }
        val totalSteps = allStepsInDay.size
        assertTrue("Total steps should be > 0", totalSteps > 0)

        // Awalnya 0 langkah selesai
        assertEquals(0, viewModel.uiState.value.completedStepsToday)
        assertEquals(0, viewModel.uiState.value.progressPercentage)

        // Centang 1 langkah
        val firstStepId = allStepsInDay.first().stepId
        fakeChecklistRepository.setCompleted(firstStepId, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.completedStepsToday)
        val expectedPercent = (1 * 100) / totalSteps
        assertEquals(expectedPercent, viewModel.uiState.value.progressPercentage)
    }

    @Test
    fun `viewModel falls back gracefully when date is outside hajj season`() = testScope.runTest {
        // 1 Januari 2027 (Jauh sebelum musim haji)
        val nonHajjDate = LocalDate.of(2027, 1, 1)

        val viewModel = HomeViewModel(
            guideRepository = guideRepository,
            checklistRepository = fakeChecklistRepository,
            currentDateProvider = { nonHajjDate }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull("Should fallback to first day", state.activeDay)
        assertFalse("Should not be hajj peak", state.isHajjPeakActive)
    }

    private class FakeChecklistRepository : ChecklistRepository {
        private val completedIdsFlow = MutableStateFlow<Set<String>>(emptySet())

        fun setCompleted(stepId: String, isCompleted: Boolean) {
            val current = completedIdsFlow.value.toMutableSet()
            if (isCompleted) current.add(stepId) else current.remove(stepId)
            completedIdsFlow.value = current
        }

        override fun observeAllChecklist(): Flow<List<ChecklistProgress>> {
            return completedIdsFlow.map { set ->
                set.map { ChecklistProgress(it, true) }
            }
        }

        override fun observeCompletedStepIds(): Flow<Set<String>> = completedIdsFlow

        override fun isStepCompleted(stepId: String): Flow<Boolean> {
            return completedIdsFlow.map { it.contains(stepId) }
        }

        override suspend fun getStepProgress(stepId: String): ChecklistProgress? {
            return if (completedIdsFlow.value.contains(stepId)) {
                ChecklistProgress(stepId, true)
            } else null
        }

        override suspend fun setStepCompleted(stepId: String, isCompleted: Boolean, timestampMs: Long?) {
            setCompleted(stepId, isCompleted)
        }

        override suspend fun clearAll() {
            completedIdsFlow.value = emptySet()
        }
    }
}
