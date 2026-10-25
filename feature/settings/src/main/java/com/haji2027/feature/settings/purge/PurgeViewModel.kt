package com.haji2027.feature.settings.purge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel pengelola dialog konfirmasi 2-langkah dan eksekusi Right to be Forgotten.
 */
class PurgeViewModel(
    private val dataPurgeManager: DataPurgeManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurgeUiState())
    val uiState: StateFlow<PurgeUiState> = _uiState.asStateFlow()

    /**
     * Membuka dialog langkah 1: Peringatan dampak penghapusan data.
     */
    fun requestPurgeStep1() {
        _uiState.update { it.copy(step = PurgeStep.DialogStep1) }
    }

    /**
     * Menyetujui langkah 1 dan beralih ke dialog langkah 2 (Konfirmasi final).
     */
    fun proceedToStep2() {
        _uiState.update { it.copy(step = PurgeStep.DialogStep2) }
    }

    /**
     * Membatalkan proses penghapusan data pada langkah mana pun.
     */
    fun cancelPurge() {
        _uiState.update { it.copy(step = PurgeStep.Idle) }
    }

    /**
     * Menjalankan eksekusi pembersihan total end-to-end setelah konfirmasi 2-langkah disetujui.
     */
    fun executeFinalPurge() {
        _uiState.update { it.copy(step = PurgeStep.Purging) }

        viewModelScope.launch(ioDispatcher) {
            val result = dataPurgeManager.executePurgeAllUserData()

            result.onSuccess { purgeResult ->
                _uiState.update {
                    it.copy(
                        step = PurgeStep.Success(purgeResult),
                        feedbackMessage = purgeResult.message
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        step = PurgeStep.Error(err.localizedMessage ?: "Gagal menghapus data"),
                        feedbackMessage = err.localizedMessage
                    )
                }
            }
        }
    }

    /**
     * Menutup pesan notifikasi dan mengembalikan state ke Idle.
     */
    fun dismissFeedback() {
        _uiState.update {
            it.copy(
                step = PurgeStep.Idle,
                feedbackMessage = null
            )
        }
    }
}
