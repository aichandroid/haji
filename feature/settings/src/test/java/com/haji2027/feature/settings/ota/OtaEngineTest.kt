package com.haji2027.feature.settings.ota

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
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
class OtaEngineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var tempDir: File
    private lateinit var fakeDownloader: FakeOtaDownloader
    private lateinit var otaEngine: OtaEngine

    private val validJsonPayload = """
        {
          "version": "1.1.0",
          "revision_date": "2026-10-25",
          "approved_by": "Komisi Fatwa",
          "hajj_year": 1448,
          "hajj_type_primary": "TAMATTU",
          "days": []
        }
    """.trimIndent()

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

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("hajj_ota_test").toFile()
        fakeDownloader = FakeOtaDownloader()
        otaEngine = OtaEngine(downloader = fakeDownloader, ioDispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `testCheckForUpdateWhenManifestHasHigherVersionReturnsUpdateAvailable`() = testScope.runTest {
        val manifestJson = """
            {
              "version": "1.1.0",
              "version_code": 110,
              "download_url": "https://test.com/content_v1.1.0.json",
              "sha256_checksum": "abcdef123456",
              "release_notes": "Perbaikan dalil tawaf"
            }
        """.trimIndent()
        fakeDownloader.manifestResponse = Result.success(manifestJson)

        val result = otaEngine.checkForUpdate("https://test.com/manifest.json", "1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow()
        assertTrue(checkResult is OtaCheckResult.UpdateAvailable)
        val available = checkResult as OtaCheckResult.UpdateAvailable
        assertEquals("1.1.0", available.manifest.version)
        assertEquals("Perbaikan dalil tawaf", available.manifest.releaseNotes)
    }

    @Test
    fun `testCheckForUpdateWhenManifestHasSameOrLowerVersionReturnsUpToDate`() = testScope.runTest {
        val manifestJson = """
            {
              "version": "1.0.0",
              "version_code": 100,
              "download_url": "https://test.com/content_v1.0.0.json",
              "sha256_checksum": "11223344",
              "release_notes": ""
            }
        """.trimIndent()
        fakeDownloader.manifestResponse = Result.success(manifestJson)

        val result = otaEngine.checkForUpdate("https://test.com/manifest.json", "1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow()
        assertTrue(checkResult is OtaCheckResult.UpToDate)
    }

    @Test
    fun `testDownloadAndApplyUpdateSuccessWithValidSha256Checksum`() = testScope.runTest {
        val payloadBytes = validJsonPayload.toByteArray(Charsets.UTF_8)
        val expectedSha256 = sha256Hex(payloadBytes)
        fakeDownloader.contentResponse = Result.success(payloadBytes)

        val manifest = OtaManifest(
            version = "1.1.0",
            downloadUrl = "https://test.com/content_v1.1.0.json",
            sha256Checksum = expectedSha256,
            releaseNotes = "Catatan pembaruan"
        )

        val result = otaEngine.downloadAndApplyUpdate(manifest, tempDir)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        val pkg = result.getOrThrow()
        assertEquals("1.1.0", pkg.version)

        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        assertTrue(activeFile.exists())
        assertEquals(validJsonPayload, activeFile.readText())

        val tempFile = File(tempDir, OtaEngine.TEMP_CONTENT_FILE)
        assertFalse(tempFile.exists())
    }

    @Test
    fun `testIntegrityCheckRejectsCorruptPayloadWithSha256Mismatch_TC_DIST_01`() = testScope.runTest {
        // Simulasi bit korup / MITM attack pada unduhan OTA (TC-DIST-01)
        val corruptedPayloadBytes = (validJsonPayload + " /* TAMPERED BITS */").toByteArray(Charsets.UTF_8)
        val expectedOriginalSha256 = sha256Hex(validJsonPayload.toByteArray(Charsets.UTF_8))
        fakeDownloader.contentResponse = Result.success(corruptedPayloadBytes)

        val manifest = OtaManifest(
            version = "1.1.0",
            downloadUrl = "https://test.com/content_v1.1.0.json",
            sha256Checksum = expectedOriginalSha256,
            releaseNotes = "Pembaruan penting"
        )

        val result = otaEngine.downloadAndApplyUpdate(manifest, tempDir)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is OtaIntegrityException)
        assertTrue(exception?.message?.contains("Verifikasi SHA-256 gagal") == true)

        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        assertFalse("Berkas aktif tidak boleh dibuat saat hash tidak cocok", activeFile.exists())

        val tempFile = File(tempDir, OtaEngine.TEMP_CONTENT_FILE)
        assertFalse("Berkas temp harus dibersihkan", tempFile.exists())
    }

    @Test
    fun `testIntegrityCheckRejectsMalformedJsonEvenIfSha256Matches`() = testScope.runTest {
        val malformedJson = "{ broken json [}"
        val malformedBytes = malformedJson.toByteArray(Charsets.UTF_8)
        val correctHashForBrokenPayload = sha256Hex(malformedBytes)
        fakeDownloader.contentResponse = Result.success(malformedBytes)

        val manifest = OtaManifest(
            version = "1.2.0",
            downloadUrl = "https://test.com/content_v1.2.0.json",
            sha256Checksum = correctHashForBrokenPayload
        )

        val result = otaEngine.downloadAndApplyUpdate(manifest, tempDir)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is OtaCorruptedJsonException)

        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        assertFalse(activeFile.exists())
    }

    @Test
    fun `testRollbackToBundledDeletesOverlayFile`() {
        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        activeFile.writeText(validJsonPayload)
        assertTrue(activeFile.exists())

        val rollbackSuccess = otaEngine.rollbackToBundled(tempDir)
        assertTrue(rollbackSuccess)
        assertFalse(activeFile.exists())
    }

    @Test
    fun `testDualLayerFallbackWhenActiveFileMissingOrCorrupted`() {
        val dualLayer = DualLayerContentManager(
            contentDir = tempDir,
            bundledAssetProvider = { ByteArrayInputStream(bundledJsonPayload.toByteArray(Charsets.UTF_8)) }
        )

        // 1. Saat belum ada Layer 2, gunakan Layer 1 (BUNDLED_ASSET)
        val initialLoaded = dualLayer.loadActiveContent().getOrNull()
        assertNotNull(initialLoaded)
        assertEquals(ContentSource.BUNDLED_ASSET, initialLoaded?.source)
        assertEquals("1.0.0", initialLoaded?.version)
        assertNull(initialLoaded?.localFile)

        // 2. Saat Layer 2 dibuat, prioritaskan OTA_OVERLAY
        val activeFile = File(tempDir, OtaEngine.ACTIVE_CONTENT_FILE)
        activeFile.writeText(validJsonPayload)

        val overlayLoaded = dualLayer.loadActiveContent().getOrNull()
        assertNotNull(overlayLoaded)
        assertEquals(ContentSource.OTA_OVERLAY, overlayLoaded?.source)
        assertEquals("1.1.0", overlayLoaded?.version)
        assertNotNull(overlayLoaded?.localFile)

        // 3. Saat Layer 2 korup, fallback otomatis ke Layer 1 dan hapus berkas korup
        activeFile.writeText("corrupted data on disk")
        val fallbackLoaded = dualLayer.loadActiveContent().getOrNull()
        assertNotNull(fallbackLoaded)
        assertEquals(ContentSource.BUNDLED_ASSET, fallbackLoaded?.source)
        assertEquals("1.0.0", fallbackLoaded?.version)
        assertFalse("Berkas korup harus otomatis dibersihkan", activeFile.exists())
    }
}

private class FakeOtaDownloader : OtaDownloader {
    var manifestResponse: Result<String> = Result.failure(IllegalStateException("No mock manifest"))
    var contentResponse: Result<ByteArray> = Result.failure(IllegalStateException("No mock content"))

    override suspend fun fetchManifest(url: String): Result<String> = manifestResponse
    override suspend fun downloadContent(url: String): Result<ByteArray> = contentResponse
}
