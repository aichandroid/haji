package com.haji2027.core.network.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Implementasi riil [FirebaseAuthProvider] menggunakan Firebase Authentication SDK.
 */
class DefaultFirebaseAuthProvider(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : FirebaseAuthProvider {

    override suspend fun signInAnonymously(): Result<AuthUser> = runCatching {
        val authResult = firebaseAuth.signInAnonymously().await()
        val user = authResult.user ?: throw IllegalStateException("Firebase user bernilai null setelah sign in anonim")
        val tokenResult = user.getIdToken(false).await()
        AuthUser(
            uid = user.uid,
            isAnonymous = user.isAnonymous,
            idToken = tokenResult.token
        )
    }

    override suspend fun getCurrentUser(): AuthUser? {
        val user = firebaseAuth.currentUser ?: return null
        return AuthUser(
            uid = user.uid,
            isAnonymous = user.isAnonymous
        )
    }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = runCatching {
        val user = firebaseAuth.currentUser ?: throw IllegalStateException("Tidak ada sesi pengguna aktif")
        val tokenResult = user.getIdToken(forceRefresh).await()
        tokenResult.token ?: throw IllegalStateException("Gagal memperoleh ID token dari Firebase")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteCurrentUser(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: return@runCatching
        user.delete().await()
    }
}
