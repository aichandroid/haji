package com.haji2027.feature.guide.doa

import com.haji2027.feature.guide.data.AssetHajjGuideRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DoaReadingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var guideRepository: AssetHajjGuideRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val assetFile = File("../../app/src/main/assets/content_v1.0.0.json")
            .takeIf { it.exists() }
            ?: File("app/src/main/assets/content_v1.0.0.json")

        guideRepository = AssetHajjGuideRepository {
            assetFile.readText()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel loads valid doa reading with verified sources`() = testScope.runTest {
        val viewModel = DoaReadingViewModel(
            readingId = "read_tahlil_arafah",
            guideRepository = guideRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.reading)
        assertEquals("read_tahlil_arafah", state.reading!!.readingId)
        assertFalse(state.reading!!.arabicText.isBlank())
        assertFalse(state.reading!!.latinTransliteration.isBlank())
        assertNotNull(state.reading!!.sourceRef)
        assertTrue(
            "Source ref must cite authentic sources",
            state.reading!!.sourceRef!!.contains("Muslim.or.id") || state.reading!!.sourceRef!!.contains("Almanhaj.or.id")
        )
    }

    @Test
    fun `viewModel toggles text size scale between normal and large`() = testScope.runTest {
        val viewModel = DoaReadingViewModel(
            readingId = "read_tahlil_arafah",
            guideRepository = guideRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TextSizeScale.NORMAL, viewModel.uiState.value.textSizeScale)

        viewModel.toggleTextSize()
        assertEquals(TextSizeScale.LARGE, viewModel.uiState.value.textSizeScale)

        viewModel.toggleTextSize()
        assertEquals(TextSizeScale.NORMAL, viewModel.uiState.value.textSizeScale)
    }

    @Test
    fun `viewModel sets error message when readingId is not found`() = testScope.runTest {
        val viewModel = DoaReadingViewModel(
            readingId = "read_non_existent",
            guideRepository = guideRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }
}
