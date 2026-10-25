package com.haji2027.feature.settings.ota

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Manifest pembaruan materi manasik over-the-air (OTA).
 * Disediakan oleh HTTPS static server untuk memberitahukan ketersediaan konten baru.
 */
@Serializable
data class OtaManifest(
    @SerialName("version") val version: String,
    @SerialName("version_code") val versionCode: Int = 1,
    @SerialName("download_url") val downloadUrl: String,
    @SerialName("sha256_checksum") val sha256Checksum: String,
    @SerialName("file_size_bytes") val fileSizeBytes: Long = 0L,
    @SerialName("release_notes") val releaseNotes: String = "",
    @SerialName("published_date") val publishedDate: String = "",
    @SerialName("min_app_version") val minAppVersion: String? = null
) {
    /**
     * Membandingkan versi manifest dengan versi aktif saat ini secara semver (major.minor.patch).
     * Mengembalikan true jika versi manifest lebih tinggi daripada versi saat ini.
     */
    fun isNewerThan(currentVersion: String): Boolean {
        return compareSemanticVersion(this.version, currentVersion) > 0
    }

    companion object {
        fun compareSemanticVersion(v1: String, v2: String): Int {
            val parts1 = v1.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
            val parts2 = v2.trim().removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

            val maxLen = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLen) {
                val num1 = parts1.getOrElse(i) { 0 }
                val num2 = parts2.getOrElse(i) { 0 }
                if (num1 != num2) {
                    return num1.compareTo(num2)
                }
            }
            return 0
        }
    }
}
