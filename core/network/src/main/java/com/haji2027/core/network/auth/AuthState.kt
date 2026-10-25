package com.haji2027.core.network.auth

/**
 * State mesin autentikasi pengguna secara reaktif.
 */
sealed interface AuthState {
    /** Belum terautentikasi (awal atau setelah sign-out). */
    data object Unauthenticated : AuthState

    /** Sedang memproses login anonim atau penyegaran token. */
    data object Loading : AuthState

    /** Berhasil terautentikasi dengan identitas akun aktif. */
    data class Authenticated(val user: AuthUser) : AuthState

    /** Kegagalan otentikasi (jaringan mati, kuota habis, error server). */
    data class Error(val message: String, val cause: Throwable? = null) : AuthState
}
