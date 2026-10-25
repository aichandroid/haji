package com.haji2027.feature.tracking.sync

import kotlinx.coroutines.flow.Flow

/**
 * Penyedia status konektivitas jaringan seluler/Wi-Fi untuk penentu mode sinkronisasi.
 */
interface NetworkConnectivityProvider {
    fun isConnected(): Boolean
    fun observeConnectivity(): Flow<Boolean>
}
