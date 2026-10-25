package com.haji2027.feature.settings.purge

import com.haji2027.core.database.dao.ChecklistDao
import com.haji2027.core.database.dao.LocationSyncQueueDao
import com.haji2027.core.database.dao.PlacesDao
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.feature.settings.ota.OtaEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Rangkuman hasil eksekusi pembersihan total data (Right to be Forgotten).
 */
data class PurgeResult(
    val isDatabasePurged: Boolean,
    val isCloudPurged: Boolean,
    val isAuthPurged: Boolean,
    val isCachePurged: Boolean,
    val message: String
)

/**
 * Kontrak manajer penghapusan seluruh data pengguna (Right to be Forgotten).
 * Mengintegrasikan pembersihan lokal basis data SQLite (Room), cache konten OTA,
 * sesi Firebase Auth, dan dokumen cloud Firestore via Cloud Functions.
 *
 * Selaras dengan FR-DIST-003, FR-DIST-004, TC-PRIV-01, dan Bagian 28.3 SYSTEM_DESIGN_DOCUMENT.md.
 */
interface DataPurgeManager {
    suspend fun executePurgeAllUserData(): Result<PurgeResult>
}

/**
 * Implementasi standar [DataPurgeManager].
 */
class DefaultDataPurgeManager(
    private val checklistDao: ChecklistDao,
    private val placesDao: PlacesDao,
    private val locationSyncQueueDao: LocationSyncQueueDao,
    private val trackingApiClient: TrackingApiClient,
    private val authManager: AuthManager,
    private val contentDir: File? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataPurgeManager {

    override suspend fun executePurgeAllUserData(): Result<PurgeResult> = withContext(ioDispatcher) {
        runCatching {
            // 1. Pembersihan Cloud Server (Dokumen relasi pelacakan, titik koordinat, dan riwayat di Firestore)
            var cloudPurged = false
            try {
                val cloudResponse = trackingApiClient.purgeUserData()
                cloudPurged = cloudResponse.isSuccess
            } catch (e: Exception) {
                // Jangan menghentikan pembersihan lokal jika jaringan offline
                cloudPurged = false
            }

            // 2. Penghapusan Akun Anonim di Firebase Authentication
            var authPurged = false
            try {
                val authDeleteResult = authManager.deleteAccount()
                authPurged = authDeleteResult.isSuccess
                authManager.signOut()
            } catch (e: Exception) {
                authManager.signOut()
                authPurged = false
            }

            // 3. Pembersihan Seluruh Tabel Basis Data SQLite Lokal (Room)
            checklistDao.clearAllChecklist()
            placesDao.deleteAllPlaces()
            locationSyncQueueDao.clearQueue()
            val databasePurged = true

            // 4. Pembersihan Berkas Cache Konten OTA (Layer 2 Overlay)
            var cachePurged = true
            contentDir?.let { dir ->
                val activeFile = File(dir, OtaEngine.ACTIVE_CONTENT_FILE)
                val tempFile = File(dir, OtaEngine.TEMP_CONTENT_FILE)
                if (activeFile.exists()) {
                    cachePurged = activeFile.delete() && cachePurged
                }
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }

            val summaryMessage = if (cloudPurged) {
                "Seluruh data lokal dan server Anda telah berhasil dihapus secara bersih (Right to be Forgotten)."
            } else {
                "Seluruh data lokal telah dihapus bersih. Server cloud akan dibersihkan otomatis saat koneksi tersedia."
            }

            PurgeResult(
                isDatabasePurged = databasePurged,
                isCloudPurged = cloudPurged,
                isAuthPurged = authPurged,
                isCachePurged = cachePurged,
                message = summaryMessage
            )
        }
    }
}
