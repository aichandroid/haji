package com.haji2027.core.network.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Kontrak manajemen autentikasi Firebase Anonymous untuk aplikasi Haji 2027.
 * Bertanggung jawab mengelola identitas unik jemaah secara zero-friction
 * dan mendukung pembersihan total akun ("Right to be Forgotten").
 */
interface AuthManager {
    /** Aliran state autentikasi reaktif untuk diamati UI & Service */
    val authState: StateFlow<AuthState>

    /**
     * Memastikan sesi pengguna aktif; jika belum terautentikasi, melakukan sign-in anonim.
     */
    suspend fun ensureAuthenticated(): Result<AuthUser>

    /**
     * Melakukan sign-in anonim eksplisit ke Firebase Authentication.
     */
    suspend fun signInAnonymously(): Result<AuthUser>

    /**
     * Mengambil UID jemaah saat ini, atau null jika belum login.
     */
    suspend fun getCurrentUserId(): String?

    /**
     * Mengambil ID Token Firebase (Bearer Token) untuk panggilan Cloud Functions yang diautentikasi.
     * @param forceRefresh paksa pembaruan token jika mendekati masa kedaluwarsa 1 jam.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): Result<String>

    /**
     * Menutup sesi autentikasi lokal.
     */
    suspend fun signOut()

    /**
     * Menghapus akun anonim dari Firebase Auth (Right to be Forgotten).
     */
    suspend fun deleteAccount(): Result<Unit>
}
