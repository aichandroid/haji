package com.haji2027.feature.settings.ota

import com.haji2027.core.common.parser.HajjContentParser
import com.haji2027.core.model.HajjContentPackage
import java.io.File
import java.io.InputStream

/**
 * Sumber materi manasik aktif sesuai arsitektur Dual-Layer (ADR-003).
 */
enum class ContentSource {
    BUNDLED_ASSET,
    OTA_OVERLAY
}

/**
 * Representasi paket materi yang berhasil dimuat beserta informasi lapisannya.
 */
data class LoadedContent(
    val packageData: HajjContentPackage,
    val source: ContentSource,
    val version: String,
    val localFile: File? = null
)

/**
 * Pengelola resolusi materi panduan dua lapis (Dual-Layer Content Architecture).
 * Memprioritaskan Layer 2 (OTA Overlay Cache) jika ada dan valid,
 * dan selalu menyediakan jaminan fallback ke Layer 1 (Bundled Assets) jika terjadi kendala.
 */
class DualLayerContentManager(
    private val contentDir: File,
    private val bundledAssetProvider: () -> InputStream
) {

    /**
     * Memuat materi manasik aktif.
     * Mencoba membaca dari Layer 2 (OTA) terlebih dahulu. Jika tidak ada atau korup,
     * secara otomatis beralih (*graceful fallback*) ke Layer 1 (Bundled Asset).
     */
    fun loadActiveContent(): Result<LoadedContent> {
        val activeFile = File(contentDir, OtaEngine.ACTIVE_CONTENT_FILE)

        // 1. Coba Layer 2: OTA Overlay Cache
        if (activeFile.exists() && activeFile.length() > 0) {
            val fileParseResult = runCatching {
                val jsonString = activeFile.readText(Charsets.UTF_8)
                HajjContentParser.parseJson(jsonString).getOrThrow()
            }

            if (fileParseResult.isSuccess) {
                val pkg = fileParseResult.getOrThrow()
                return Result.success(
                    LoadedContent(
                        packageData = pkg,
                        source = ContentSource.OTA_OVERLAY,
                        version = pkg.version,
                        localFile = activeFile
                    )
                )
            } else {
                // Berkas Layer 2 korup, hapus demi kesehatan sistem
                activeFile.delete()
            }
        }

        // 2. Fallback ke Layer 1: Bundled Asset bawaan APK
        return runCatching {
            val assetStream = bundledAssetProvider()
            val pkg = HajjContentParser.parseJson(assetStream).getOrThrow()
            LoadedContent(
                packageData = pkg,
                source = ContentSource.BUNDLED_ASSET,
                version = pkg.version,
                localFile = null
            )
        }
    }

    /**
     * Mendapatkan nomor versi materi aktif saat ini.
     */
    fun getActiveVersion(): String {
        return loadActiveContent().getOrNull()?.version ?: "1.0.0"
    }

    /**
     * Mengembalikan status sumber materi aktif saat ini.
     */
    fun getActiveSource(): ContentSource {
        return loadActiveContent().getOrNull()?.source ?: ContentSource.BUNDLED_ASSET
    }

    /**
     * Menghapus pembaruan Layer 2 dan kembali menggunakan materi bawaan APK.
     */
    fun rollbackToBundled(): Boolean {
        val activeFile = File(contentDir, OtaEngine.ACTIVE_CONTENT_FILE)
        val tempFile = File(contentDir, OtaEngine.TEMP_CONTENT_FILE)
        if (tempFile.exists()) tempFile.delete()
        return if (activeFile.exists()) activeFile.delete() else true
    }
}
