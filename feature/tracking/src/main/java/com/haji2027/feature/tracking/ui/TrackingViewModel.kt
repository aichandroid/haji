package com.haji2027.feature.tracking.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.auth.AuthState
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.QrPayload
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.feature.tracking.service.TrackingController
import com.haji2027.feature.tracking.sync.SyncManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orkestrasi layar pelacakan keluarga Haji 2027 (SCR-05 & SCR-06).
 * Mengelola pembuatan token pairing QR 5 menit, klaim token keluarga,
 * pencabutan izin (revoke consent), dan siklus hidup layanan lokasi foreground.
 */
class TrackingViewModel(
    private val apiClient: TrackingApiClient,
    private val authManager: AuthManager,
    private val trackingController: TrackingController,
    private val syncManager: SyncManager? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val tickerDelayMs: Long = 1000L
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        // Pantau status layanan foreground lokasi secara reaktif
        viewModelScope.launch {
            trackingController.status.collect { status ->
                _uiState.update { it.copy(isServiceRunning = status.isRunning) }
            }
        }

        // Pantau sesi otentikasi
        viewModelScope.launch {
            authManager.authState.collect { authState ->
                val uid = if (authState is AuthState.Authenticated) authState.user.uid else null
                _uiState.update { it.copy(currentUserId = uid) }
            }
        }

        // Inisialisasi otentikasi anonim jika belum aktif
        viewModelScope.launch(ioDispatcher) {
            authManager.ensureAuthenticated()
        }
    }

    /**
     * Berpindah antar tab fitur pelacakan (MAIN, SHARE_QR, SCAN_QR, CONSENT_LIST).
     */
    fun navigateTo(tab: TrackingTab) {
        _uiState.update { it.copy(currentTab = tab) }
        if (tab == TrackingTab.SHARE_QR) {
            val currentState = _uiState.value.shareQrState
            if (currentState is ShareQrState.Idle || currentState is ShareQrState.Expired) {
                generateNewQrToken()
            }
        }
    }

    /**
     * Kembali ke layar utama pelacakan keluarga.
     */
    fun navigateBack() {
        tickerJob?.cancel()
        _uiState.update { it.copy(currentTab = TrackingTab.MAIN) }
    }

    /**
     * Layar SCR-05: Meminta token baru dan memulai hitung mundur 5 menit (300 detik).
     */
    fun generateNewQrToken() {
        tickerJob?.cancel()
        _uiState.update { it.copy(shareQrState = ShareQrState.Loading) }

        viewModelScope.launch(ioDispatcher) {
            authManager.ensureAuthenticated()
            val result = apiClient.generatePairingToken(GenerateTokenRequest())

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        shareQrState = ShareQrState.Ready(
                            tokenId = response.tokenId,
                            qrPayload = response.qrPayload,
                            remainingSeconds = response.validitySeconds,
                            expiresAtEpochMs = response.expiresAtEpochMs
                        )
                    )
                }
                startCountdownTicker(response.validitySeconds)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        shareQrState = ShareQrState.Error(
                            err.localizedMessage ?: "Gagal membuat kode QR baru"
                        )
                    )
                }
            }
        }
    }

    /**
     * Menjalankan ticker hitung mundur 1 detik sampai kedaluwarsa.
     */
    private fun startCountdownTicker(initialSeconds: Int) {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(ioDispatcher) {
            var remaining = initialSeconds
            while (remaining > 0) {
                delay(tickerDelayMs)
                remaining--
                if (remaining > 0) {
                    _uiState.update { state ->
                        val current = state.shareQrState
                        if (current is ShareQrState.Ready) {
                            state.copy(shareQrState = current.copy(remainingSeconds = remaining))
                        } else {
                            state
                        }
                    }
                } else {
                    _uiState.update { it.copy(shareQrState = ShareQrState.Expired) }
                    break
                }
            }
        }
    }

    /**
     * Layar Pemindai QR: Memvalidasi skema QR dan mengklaim token pasangan pelacakan.
     */
    fun claimScannedQr(qrInput: String, trackerLabel: String) {
        val trimmedLabel = trackerLabel.trim()
        if (trimmedLabel.isBlank()) {
            _uiState.update {
                it.copy(claimQrState = ClaimQrState.Error("Label hubungan keluarga tidak boleh kosong"))
            }
            return
        }

        val parseResult = QrPayload.parse(qrInput.trim())
        val payload = parseResult.getOrElse { error ->
            _uiState.update {
                it.copy(claimQrState = ClaimQrState.Error("Format QR tidak valid: ${error.message}"))
            }
            return
        }

        _uiState.update { it.copy(claimQrState = ClaimQrState.Loading) }

        viewModelScope.launch(ioDispatcher) {
            val result = apiClient.claimPairingToken(
                ClaimTokenRequest(
                    tokenId = payload.tokenId,
                    trackerLabel = trimmedLabel
                )
            )

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        claimQrState = ClaimQrState.Success(
                            relationId = response.relationId,
                            targetUserId = response.targetUserId,
                            message = response.message
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        claimQrState = ClaimQrState.Error(
                            err.localizedMessage ?: "Gagal menghubungkan token pelacakan"
                        )
                    )
                }
            }
        }
    }

    /**
     * Mengatur ulang state formulir klaim QR.
     */
    fun resetClaimState() {
        _uiState.update { it.copy(claimQrState = ClaimQrState.Idle) }
    }

    /**
     * Layar SCR-06: Mencabut izin pelacakan untuk anggota keluarga tertentu secara instan.
     */
    fun revokeConsent(trackerUserId: String) {
        _uiState.update { it.copy(revokingTrackerId = trackerUserId) }

        viewModelScope.launch(ioDispatcher) {
            val result = apiClient.revokeTrackingAccess(RevokeAccessRequest(trackerUserId))

            result.onSuccess {
                _uiState.update { state ->
                    val updatedList = state.activeTrackers.filterNot { it.trackerUserId == trackerUserId }
                    state.copy(
                        activeTrackers = updatedList,
                        revokingTrackerId = null,
                        userFeedbackMessage = "Izin pelacakan berhasil dicabut."
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        revokingTrackerId = null,
                        userFeedbackMessage = "Gagal mencabut izin: ${err.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Layar SCR-06: Menghentikan seluruh layanan pelacakan lokasi seketika.
     */
    fun stopAllTracking(context: Context? = null) {
        trackingController.stopTracking(context)
        syncManager?.stopListeningToTrackingService()
        _uiState.update {
            it.copy(userFeedbackMessage = "Seluruh layanan pelacakan lokasi telah dihentikan.")
        }
    }

    /**
     * Mengaktifkan layanan pelacakan latar belakang.
     */
    fun startTrackingService(context: Context? = null) {
        val uid = _uiState.value.currentUserId ?: "anonymous_user"
        trackingController.startTracking(context, uid)
        syncManager?.startListeningToTrackingService()
        _uiState.update {
            it.copy(userFeedbackMessage = "Layanan pelacakan latar belakang telah aktif.")
        }
    }

    /**
     * Membersihkan pesan notifikasi banner pengguna.
     */
    fun clearFeedbackMessage() {
        _uiState.update { it.copy(userFeedbackMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
