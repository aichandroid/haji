package com.haji2027.feature.tracking.sync

import com.haji2027.core.model.UserLocation
import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak manajer sinkronisasi antrean telemetri lokasi offline.
 * Menghubungkan antrean SQLite lokal (:core:database) dengan endpoint Cloud Functions (:core:network).
 *
 * Selaras dengan FR-TRACK-006 dan Bagian 17.6 SYSTEM_DESIGN_DOCUMENT.md.
 */
interface SyncManager {
    /** Aliran state proses sinkronisasi reaktif */
    val syncState: StateFlow<SyncState>

    /**
     * Memasukkan sampel lokasi ke antrean lokal (FIFO maks 50) dan mencoba sinkronisasi instan jika online.
     */
    suspend fun enqueueLocation(location: UserLocation): Long

    /**
     * Menyinkronkan seluruh antrean lokasi yang tertahan saat jaringan online pulih.
     * Menerapkan prinsip data minimization: hanya mengirim sampel paling mutakhir dan membersihkan antrean lama.
     */
    suspend fun syncPendingLocations(): Result<Int>

    /**
     * Menautkan manajer sinkronisasi dengan event [LocationTrackingService.locationListener].
     */
    fun startListeningToTrackingService()

    /**
     * Melepaskan tautan listener saat layanan pelacakan berhenti.
     */
    fun stopListeningToTrackingService()
}
