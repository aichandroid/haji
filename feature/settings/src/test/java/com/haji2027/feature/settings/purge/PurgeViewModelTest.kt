package com.haji2027.feature.settings.purge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurgeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakePurgeManager: FakeDataPurgeManager
    private lateinit var viewModel: PurgeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePurgeManager = FakeDataPurgeManager()
        viewModel = PurgeViewModel(
            dataPurgeManager = fakePurgeManager,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `testInitialStateIsIdle`() {
        val state = viewModel.uiState.value
        assertEquals(PurgeStep.Idle, state.step)
        assertNull(state.feedbackMessage)
    }

    @Test
    fun `testTwoStepConfirmationDialogTransitions`() {
        viewModel.requestPurgeStep1()
        assertEquals(PurgeStep.DialogStep1, viewModel.uiState.value.step)

        viewModel.proceedToStep2()
        assertEquals(PurgeStep.DialogStep2, viewModel.uiState.value.step)

        viewModel.cancelPurge()
        assertEquals(PurgeStep.Idle, viewModel.uiState.value.step)
    }

    @Test
    fun `testExecuteFinalPurgeSuccess`() = testScope.runTest {
        viewModel.requestPurgeStep1()
        viewModel.proceedToStep2()
        viewModel.executeFinalPurge()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.step is PurgeStep.Success)
        val success = state.step as PurgeStep.Success
        assertTrue(success.result.isDatabasePurged)
        assertTrue(success.result.isCloudPurged)
        assertNotNull(state.feedbackMessage)
        assertTrue(fakePurgeManager.purgeExecuted)
    }

    @Test
    fun `testExecuteFinalPurgeFailure`() = testScope.runTest {
        fakePurgeManager.shouldFail = true

        viewModel.requestPurgeStep1()
        viewModel.proceedToStep2()
        viewModel.executeFinalPurge()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.step is PurgeStep.Error)
        val error = state.step as PurgeStep.Error
        assertEquals("Purge failed intentionally", error.message)
    }

    @Test
    fun `testDismissFeedbackReturnsToIdle`() = testScope.runTest {
        viewModel.requestPurgeStep1()
        viewModel.proceedToStep2()
        viewModel.executeFinalPurge()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.step is PurgeStep.Success)

        viewModel.dismissFeedback()
        assertEquals(PurgeStep.Idle, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.feedbackMessage)
    }
}

private class FakeDataPurgeManager : DataPurgeManager {
    var shouldFail = false
    var purgeExecuted = false

    override suspend fun executePurgeAllUserData(): Result<PurgeResult> {
        purgeExecuted = true
        return if (shouldFail) {
            Result.failure(RuntimeException("Purge failed intentionally"))
        } else {
            Result.success(
                PurgeResult(
                    isDatabasePurged = true,
                    isCloudPurged = true,
                    isAuthPurged = true,
                    isCachePurged = true,
                    message = "Seluruh data Anda telah berhasil dihapus secara bersih (Right to be Forgotten)."
                )
            )
        }
    }
}
