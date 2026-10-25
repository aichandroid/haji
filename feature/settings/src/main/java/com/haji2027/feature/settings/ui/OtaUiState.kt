package com.haji2027.feature.settings.ui

import com.haji2027.feature.settings.ota.ContentSource
import com.haji2027.feature.settings.ota.OtaManifest

/**
 * State alur pembaruan konten OTA.
 */
sealed interface OtaStatus {
    data object Idle : OtaStatus
    data object Checking : OtaStatus
    data class UpdateAvailable(val manifest: OtaManifest) : OtaStatus
    data object DownloadingAndVerifying : OtaStatus
    data class UpdateSuccess(val version: String) : OtaStatus
    data object AlreadyUpToDate : OtaStatus
    data class Error(val message: String, val isIntegrityError: Boolean = false) : OtaStatus
}

/**
 * State tampilan UI pembaruan materi dan pengaturan OTA.
 */
data class OtaUiState(
    val currentVersion: String = "1.0.0",
    val contentSource: ContentSource = ContentSource.BUNDLED_ASSET,
    val status: OtaStatus = OtaStatus.Idle,
    val userNotification: String? = null
)
