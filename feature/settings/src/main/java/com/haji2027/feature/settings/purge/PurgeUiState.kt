package com.haji2027.feature.settings.purge

/**
 * Tahapan alur dialog konfirmasi 2-langkah (2-step confirmation) penghapusan data total (TC-PRIV-01).
 */
sealed interface PurgeStep {
    /** Kondisi diam normal */
    data object Idle : PurgeStep

    /** Langkah 1: Dialog peringatan konsekuensi penghapusan data */
    data object DialogStep1 : PurgeStep

    /** Langkah 2: Dialog konfirmasi final tindakan permanen/irreversibel */
    data object DialogStep2 : PurgeStep

    /** Sedang memproses pembersihan cloud, database SQLite, dan cache */
    data object Purging : PurgeStep

    /** Berhasil dihapus secara tuntas */
    data class Success(val result: PurgeResult) : PurgeStep

    /** Terjadi kesalahan saat pembersihan */
    data class Error(val message: String) : PurgeStep
}

/**
 * State tampilan UI untuk fitur Right to be Forgotten.
 */
data class PurgeUiState(
    val step: PurgeStep = PurgeStep.Idle,
    val feedbackMessage: String? = null
)
