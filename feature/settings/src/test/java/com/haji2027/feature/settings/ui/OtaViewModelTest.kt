package com.haji2027.feature.settings.ui

import com.haji2027.feature.settings.ota.ContentSource
import com.haji2027.feature.settings.ota.DualLayerContentManager
import com.haji2027.feature.settings.ota.OtaDownloader
import com.haji2027.feature.settings.ota.OtaEngine
import com.haji2027.feature.settings.ota.OtaManifest
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class OtaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var tempDir: File
    private lateinit var fakeDownloader: FakeOtaDownloader
    private lateinit var otaEngine: OtaEngine
    private lateinit var dualLayerManager: DualLayerContentManager
    private lateinit var viewModel: OtaViewModel

    private val bundledJsonPayload = """
        {
          "version": "1.0.0",
          "revision_date": "2026-10-24",
          "approved_by": "Ustadz Pembimbing",
          "hajj_year": 1448,
          "hajj_type_primary": "TAMATTU",
          "days": []
        }
    """.trimIndent()

    private val newValidJsonPayload = """
        {
          "version": "1.1.0",
          "revision_date": "2026-10-25",
          "approved_by": "Komisi Fatwa",
          "hajj_year": 1448,
          "hajj_type_primary": "TAMATTU",
          "days": []
        }
    """.trimIndent()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        tempDir = Files.createTempDirectory("hajj_vm_ota_test").toFile()
        fakeDownloader = FakeOtaDownloader()
        otaEngine = OtaEngine(downloader = fakeDownloader, ioDispatcher = testDispatcher)

        dualLayerManager = DualLayerContentManager(
            contentDir = tempDir,
            bundledAssetProvider = { ByteArrayInputStream(bundledJsonPayload.toByteArray(Charsets.UTF_8)) }
        )

        viewModel = OtaViewModel(
            otaEngine = otaEngine,
            dualLayerManager = dualLayerManager,
            contentDir = tempDir,
            manifestUrl = "https://test.com/manifest.json",
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        tempDir.deleteRecursively()
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `testInitialState()`() {
        val state = viewModel.uiState.value
        assertEquals("1.0.0", state.currentVersion)
        assertEquals(ContentSource.BUNDLED_ASSET, state.contentSource)
        assertTrue(state.status is OtaStatus.Idle)
        assertNull(state.userNotification)
    }

    @Test
    fun `testCheckForUpdateSuccessWithNewVersion()`() = testScope.runTest {
        val manifestJson = """
            {
              "version": "1.1.0",
              "version_code": 110,
              "download_url": "https://test.com/content_v1.1.0.json",
              "sha256_checksum": "abcdef",
              "release_notes": "Revisi doa tawaf"
            }
        """.trimIndent()
        fakeDownloader.manifestResponse = Result.success(manifestJson)

        viewModel.checkForUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.status is OtaStatus.UpdateAvailable)
        val available = state.status as OtaStatus.UpdateAvailable
        assertEquals("1.1.0", available.manifest.version)
        assertEquals("Revisi doa tawaf", available.manifest.releaseNotes)
    }

    @Test
    fun `testCheckForUpdateAlreadyUpToDate()`() = testScope.runTest {
        val manifestJson = """
            {
              "version": "1.0.0",
              "version_code": 100,
              "download_url": "https://test.com/content_v1.0.0.json",
              "sha256_checksum": "abcdef",
              "release_notes": ""
            }
        """.trimIndent()
        fakeDownloader.manifestResponse = Result.success(manifestJson)

        viewModel.checkForUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.status is OtaStatus.AlreadyUpToDate)
    }

    @Test
    fun `testCheckForUpdateFailure()`() = testScope.runTest {
        fakeDownloader.manifestResponse = Result.failure(RuntimeException("Koneksi gagal"))

        viewModel.checkForUpdate()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.status is OtaStatus.Error)
        assertEquals("Koneksi gagal", (state.status as OtaStatus.Error).message)
    }

    @Test
    fun `testApplyUpdateSuccessAndRefreshesVersion()`() = testScope.runTest {
        val payloadBytes = newValidJsonPayload.toByteArray(Charsets.UTF_8)
        val validHash = sha256Hex(payloadBytes)
        fakeDownloader.contentResponse = Result.success(payloadBytes)

        val manifest = OtaManifest(
            version = "1.1.0",
            downloadUrl = "https://test.com/content_v1.1.0.json",
            sha256Checksum = validHash
        )

        viewModel.applyUpdate(manifest)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.status is OtaStatus.UpdateSuccess)
        assertEquals("1.1.0", state.currentVersion)
        assertEquals(ContentSource.OTA_OVERLAY, state.contentSource)
        assertNotNull(state.userNotification)
        assertTrue(state.userNotification?.contains("1.1.0") == true)
    }

    @Test
    fun `testApplyUpdateIntegrityError()`() = testScope.runTest {
        val payloadBytes = newValidJsonPayload.toByteArray(Charsets.UTF_8)
        fakeDownloader.contentResponse = Result.success(payloadBytes)

        val manifestWithWrongHash = OtaManifest(
            version = "1.1.0",
            downloadUrl = "https://test.com/content_v1.1.0.json",
            sha256Checksum = "WRONG_CHECKSUM_HASH_123"
        )

        viewModel.applyUpdate(manifestWithWrongHash)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.status is OtaStatus.Error)
        val err = state.status as OtaStatus.Error
        assertTrue(err.isIntegrityError)
        assertEquals("1.0.0", state.currentVersion) // Versi tetap bawaan (Layer 1)
        assertEquals(ContentSource.BUNDLED_ASSET, state.contentSource)
    }

    @Test
    fun `testRollbackToBundled()`() = testScope.runTest {
        // Simulasikan Layer 2 aktif terlebih dahulu
        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        activeFile.writeText(newValidJsonPayload)
        viewModel.refreshActiveContentInfo()

        assertEquals("1.1.0", viewModel.uiState.value.currentVersion)
        assertEquals(ContentSource.OTA_OVERLAY, viewModel.uiState.value.contentSource)

        viewModel.rollbackToBundled()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("1.0.0", state.currentVersion)
        assertEquals(ContentSource.BUNDLED_ASSET, state.contentSource)
        assertTrue(state.status is OtaStatus.Idle)
        assertTrue(state.userNotification?.contains("materi bawaan") == true)
        assertFalse(activeFile.exists())
    }

    @Test
    fun `testDismissNotification()`() {
        viewModel.rollbackToBundled()
        assertNotNull(viewModel.uiState.value.userNotification)

        viewModel.dismissNotification()
        assertNull(viewModel.uiState.value.userNotification)
    }
}

private class FakeOtaDownloader : OtaDownloader {
    var manifestResponse: Result<String> = Result.failure(IllegalStateException("No mock manifest"))
    var contentResponse: Result<ByteArray> = Result.failure(IllegalStateException("No mock content"))

    override suspend fun fetchManifest(url: String): Result<String> = manifestResponse
    override suspend fun downloadContent(url: String): Result<ByteArray> = contentResponse
}
