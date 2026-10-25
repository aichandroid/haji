package com.haji2027.core.network.auth

/**
 * Abstraksi operasi tingkat rendah Firebase Authentication SDK.
 * Memisahkan interaksi platform agar [DefaultFirebaseAuthManager] dapat diuji unit tanpa koneksi cloud.
 */
interface FirebaseAuthProvider {
    /** Menjalankan sign in anonim pada SDK */
    suspend fun signInAnonymously(): Result<AuthUser>

    /** Mendapatkan sesi user saat ini jika sudah login sebelumnya */
    suspend fun getCurrentUser(): AuthUser?

    /** Mengambil JWT ID Token terbaru */
    suspend fun getIdToken(forceRefresh: Boolean): Result<String>

    /** Menutup sesi lokal */
    suspend fun signOut()

    /** Menghapus akun permanen di Firebase Auth */
    suspend fun deleteCurrentUser(): Result<Unit>
}
