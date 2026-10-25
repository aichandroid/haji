package com.haji2027.feature.tracking.ui

/**
 * Tab navigasi layar fitur pelacakan keluarga.
 */
enum class TrackingTab {
    MAIN,
    SHARE_QR,
    SCAN_QR,
    CONSENT_LIST
}

/**
 * State layar SCR-05 (Bagikan Lokasi Saya / QR Code 5 Menit).
 */
sealed interface ShareQrState {
    data object Idle : ShareQrState
    data object Loading : ShareQrState
    data class Ready(
        val tokenId: String,
        val qrPayload: String,
        val remainingSeconds: Int,
        val expiresAtEpochMs: Long
    ) : ShareQrState
    data object Expired : ShareQrState
    data class Error(val message: String) : ShareQrState
}

/**
 * State alur pemindaian / klaim QR token pasangan pelacakan.
 */
sealed interface ClaimQrState {
    data object Idle : ClaimQrState
    data object Loading : ClaimQrState
    data class Success(
        val relationId: String,
        val targetUserId: String,
        val message: String
    ) : ClaimQrState
    data class Error(val message: String) : ClaimQrState
}

/**
 * Data entitas anggota keluarga yang memiliki izin memantau lokasi jemaah (SCR-06).
 */
data class TrackerConsentItem(
    val trackerUserId: String,
    val label: String,
    val connectedSinceFormatted: String,
    val isActive: Boolean = true
)

/**
 * State utama antarmuka fitur pelacakan keluarga Haji 2027.
 */
data class TrackingUiState(
    val currentTab: TrackingTab = TrackingTab.MAIN,
    val isServiceRunning: Boolean = false,
    val currentUserId: String? = null,
    val shareQrState: ShareQrState = ShareQrState.Idle,
    val claimQrState: ClaimQrState = ClaimQrState.Idle,
    val activeTrackers: List<TrackerConsentItem> = listOf(
        TrackerConsentItem(
            trackerUserId = "tracker_fatimah_01",
            label = "Fatimah (Anak Perempuan)",
            connectedSinceFormatted = "18 Mei 2027",
            isActive = true
        ),
        TrackerConsentItem(
            trackerUserId = "tracker_dahlan_02",
            label = "H. Ahmad Dahlan (Suami)",
            connectedSinceFormatted = "19 Mei 2027",
            isActive = true
        )
    ),
    val revokingTrackerId: String? = null,
    val userFeedbackMessage: String? = null
)
