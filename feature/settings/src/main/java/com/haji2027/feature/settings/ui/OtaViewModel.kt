package com.haji2027.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.feature.settings.ota.DualLayerContentManager
import com.haji2027.feature.settings.ota.OtaCheckResult
import com.haji2027.feature.settings.ota.OtaEngine
import com.haji2027.feature.settings.ota.OtaIntegrityException
import com.haji2027.feature.settings.ota.OtaManifest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel pengelola antarmuka pembaruan materi manasik OTA dan verifikasi integritas hash SHA-256.
 */
class OtaViewModel(
    private val otaEngine: OtaEngine,
    private val dualLayerManager: DualLayerContentManager,
    private val contentDir: File,
    private val manifestUrl: String = OtaEngine.DEFAULT_MANIFEST_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtaUiState())
    val uiState: StateFlow<OtaUiState> = _uiState.asStateFlow()

    init {
        refreshActiveContentInfo()
    }

    /**
     * Memperbarui nomor versi dan sumber materi yang sedang aktif di UI.
     */
    fun refreshActiveContentInfo() {
        val version = dualLayerManager.getActiveVersion()
        val source = dualLayerManager.getActiveSource()
        _uiState.update {
            it.copy(
                currentVersion = version,
                contentSource = source
            )
        }
    }

    /**
     * Memeriksa keberadaan pembaruan materi manasik di server statis HTTPS.
     */
    fun checkForUpdate() {
        _uiState.update { it.copy(status = OtaStatus.Checking) }

        viewModelScope.launch(ioDispatcher) {
            val currentVer = _uiState.value.currentVersion
            val result = otaEngine.checkForUpdate(manifestUrl, currentVer)

            result.onSuccess { checkResult ->
                when (checkResult) {
                    is OtaCheckResult.UpdateAvailable -> {
                        _uiState.update { it.copy(status = OtaStatus.UpdateAvailable(checkResult.manifest)) }
                    }
                    is OtaCheckResult.UpToDate -> {
                        _uiState.update { it.copy(status = OtaStatus.AlreadyUpToDate) }
                    }
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        status = OtaStatus.Error(
                            message = err.localizedMessage ?: "Gagal memeriksa pembaruan materi",
                            isIntegrityError = false
                        )
                    )
                }
            }
        }
    }

    /**
     * Mengunduh, memverifikasi hash SHA-256, dan menerapkan materi baru.
     */
    fun applyUpdate(manifest: OtaManifest) {
        _uiState.update { it.copy(status = OtaStatus.DownloadingAndVerifying) }

        viewModelScope.launch(ioDispatcher) {
            val result = otaEngine.downloadAndApplyUpdate(manifest, contentDir)

            result.onSuccess {
                refreshActiveContentInfo()
                _uiState.update { state ->
                    state.copy(
                        status = OtaStatus.UpdateSuccess(manifest.version),
                        userNotification = "Materi manasik berhasil diperbarui ke versi ${manifest.version}."
                    )
                }
            }.onFailure { err ->
                val isIntegrity = err is OtaIntegrityException
                _uiState.update { state ->
                    state.copy(
                        status = OtaStatus.Error(
                            message = err.localizedMessage ?: "Pembaruan materi gagal",
                            isIntegrityError = isIntegrity
                        )
                    )
                }
            }
        }
    }

    /**
     * Mengembalikan materi ke versi bawaan APK (Rollback).
     */
    fun rollbackToBundled() {
        dualLayerManager.rollbackToBundled()
        refreshActiveContentInfo()
        _uiState.update {
            it.copy(
                status = OtaStatus.Idle,
                userNotification = "Materi berhasil dikembalikan ke versi bawaan aplikasi."
            )
        }
    }

    /**
     * Menutup pesan notifikasi pengguna.
     */
    fun dismissNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }
}
