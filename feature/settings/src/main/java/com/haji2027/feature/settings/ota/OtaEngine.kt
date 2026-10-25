package com.haji2027.feature.settings.ota

import com.haji2027.core.common.parser.HajjContentParser
import com.haji2027.core.model.HajjContentPackage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

/**
 * Hasil pemeriksaan pembaruan konten OTA.
 */
sealed interface OtaCheckResult {
    data class UpdateAvailable(val manifest: OtaManifest) : OtaCheckResult
    data class UpToDate(val currentVersion: String) : OtaCheckResult
}

/**
 * Mesin utama manajemen pembaruan konten Over-The-Air (OTA) dan verifikasi integritas kriptografis SHA-256.
 * Sesuai dengan FR-DIST-002, TC-DIST-01, dan Alur Sequence Diagram Bagian 17.2 SYSTEM_DESIGN_DOCUMENT.md.
 */
class OtaEngine(
    private val downloader: OtaDownloader = DefaultOtaDownloader(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Memeriksa keberadaan manifest pembaruan materi di server statis HTTPS.
     */
    suspend fun checkForUpdate(
        manifestUrl: String,
        currentVersion: String
    ): Result<OtaCheckResult> = withContext(ioDispatcher) {
        runCatching {
            val manifestJson = downloader.fetchManifest(manifestUrl).getOrThrow()
            val manifest = json.decodeFromString<OtaManifest>(manifestJson)

            if (manifest.isNewerThan(currentVersion)) {
                OtaCheckResult.UpdateAvailable(manifest)
            } else {
                OtaCheckResult.UpToDate(currentVersion)
            }
        }
    }

    /**
     * Mengunduh materi baru, memverifikasi hash SHA-256, memvalidasi integritas JSON,
     * dan melakukan atomic swap berkas menjadi content_active.json.
     *
     * @param manifest Manifest dari rilis konten baru yang disetujui.
     * @param contentDir Direktori penyimpanan privat aplikasi (misal: context.filesDir/content).
     * @return [HajjContentPackage] yang berhasil diproses dan diaktifkan.
     */
    suspend fun downloadAndApplyUpdate(
        manifest: OtaManifest,
        contentDir: File
    ): Result<HajjContentPackage> = withContext(ioDispatcher) {
        runCatching {
            if (!contentDir.exists()) {
                contentDir.mkdirs()
            }

            // 1. Unduh payload bytes dari download_url
            val payloadBytes = downloader.downloadContent(manifest.downloadUrl).getOrThrow()

            // 2. Hitung hash SHA-256 lokal
            val computedHash = calculateSha256(payloadBytes)
            val expectedHash = manifest.sha256Checksum.trim()

            // 3. Verifikasi integritas hash kriptografis (TC-DIST-01)
            if (!computedHash.equals(expectedHash, ignoreCase = true)) {
                throw OtaIntegrityException(
                    "Verifikasi SHA-256 gagal! Diharapkan: $expectedHash, Didapatkan: $computedHash. Pembaruan dibatalkan demi keamanan."
                )
            }

            // 4. Verifikasi parsing struktur model domain JSON
            val jsonString = payloadBytes.toString(Charsets.UTF_8)
            val parseResult = HajjContentParser.parseJson(jsonString)
            val parsedPackage = parseResult.getOrElse { error ->
                throw OtaCorruptedJsonException(
                    "Berkas JSON unduhan tidak sesuai dengan skema materi manasik: ${error.message}",
                    error
                )
            }

            // 5. Atomic write & swap berkas:
            // Tulis ke content_temp.json terlebih dahulu
            val tempFile = File(contentDir, TEMP_CONTENT_FILE)
            val activeFile = File(contentDir, ACTIVE_CONTENT_FILE)

            try {
                tempFile.writeBytes(payloadBytes)

                // Atomic rename / move menggantikan content_active.json
                try {
                    Files.move(
                        tempFile.toPath(),
                        activeFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (e: Exception) {
                    // Fallback jika filesystem tidak mendukung ATOMIC_MOVE
                    if (activeFile.exists()) {
                        activeFile.delete()
                    }
                    if (!tempFile.renameTo(activeFile)) {
                        throw IOException("Gagal memindahkan berkas sementara ke berkas aktif")
                    }
                }
            } catch (writeErr: Exception) {
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                throw writeErr
            }

            parsedPackage
        }
    }

    /**
     * Menghapus Layer 2 (OTA Overlay Cache) dan mengembalikan aplikasi ke Layer 1 (Bundled Asset).
     */
    fun rollbackToBundled(contentDir: File): Boolean {
        val activeFile = File(contentDir, ACTIVE_CONTENT_FILE)
        val tempFile = File(contentDir, TEMP_CONTENT_FILE)

        if (tempFile.exists()) {
            tempFile.delete()
        }

        return if (activeFile.exists()) {
            activeFile.delete()
        } else {
            true
        }
    }

    /**
     * Memeriksa apakah berkas overlay OTA aktif ada dan dapat dibaca.
     */
    fun getActiveOverlayFile(contentDir: File): File? {
        val activeFile = File(contentDir, ACTIVE_CONTENT_FILE)
        return if (activeFile.exists() && activeFile.length() > 0) activeFile else null
    }

    /**
     * Menghitung string heksadesimal SHA-256 dari array byte.
     */
    fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val ACTIVE_CONTENT_FILE = "content_active.json"
        const val TEMP_CONTENT_FILE = "content_temp.json"
        const val DEFAULT_MANIFEST_URL = "https://unduh.panduanhaji2027.org/content/manifest.json"
    }
}

class OtaIntegrityException(message: String) : SecurityException(message)
class OtaCorruptedJsonException(message: String, cause: Throwable? = null) : IllegalArgumentException(message, cause)
