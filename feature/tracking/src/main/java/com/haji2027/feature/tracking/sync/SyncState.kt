package com.haji2027.feature.tracking.sync

/**
 * State mesin sinkronisasi telemetri lokasi offline.
 */
sealed interface SyncState {
    /** Tidak ada aktivitas sinkronisasi yang sedang berjalan */
    data object Idle : SyncState

    /** Sedang mengunggah titik koordinat ke server */
    data object Syncing : SyncState

    /** Berhasil menyinkronkan koordinat ke Cloud Firestore */
    data class Success(val syncedCount: Int, val timestampMs: Long = System.currentTimeMillis()) : SyncState

    /** Jaringan terputus; data tertahan di antrean SQLite lokal */
    data class Offline(val queuedCount: Int) : SyncState

    /** Terjadi kegagalan saat transmisi ke server */
    data class Error(val message: String, val queuedCount: Int) : SyncState
}
