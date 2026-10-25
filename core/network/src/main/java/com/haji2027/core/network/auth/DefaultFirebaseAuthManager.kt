package com.haji2027.core.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementasi standar [AuthManager] untuk Firebase Anonymous Authentication.
 * Dilengkapi proteksi concurrency (Mutex) dan alur pemulihan sesi transparan.
 */
class DefaultFirebaseAuthManager(
    private val authProvider: FirebaseAuthProvider
) : AuthManager {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authMutex = Mutex()

    override suspend fun ensureAuthenticated(): Result<AuthUser> = authMutex.withLock {
        // Cek jika state saat ini sudah authenticated
        val current = _authState.value
        if (current is AuthState.Authenticated) {
            return Result.success(current.user)
        }

        // Cek apakah provider sudah memiliki sesi lokal tersimpan
        val existingUser = authProvider.getCurrentUser()
        if (existingUser != null) {
            val userWithToken = resolveUserWithToken(existingUser)
            _authState.value = AuthState.Authenticated(userWithToken)
            return Result.success(userWithToken)
        }

        // Jika belum ada, jalankan sign-in anonim baru
        return performAnonymousSignInInternal()
    }

    override suspend fun signInAnonymously(): Result<AuthUser> = authMutex.withLock {
        performAnonymousSignInInternal()
    }

    private suspend fun performAnonymousSignInInternal(): Result<AuthUser> {
        _authState.value = AuthState.Loading
        val result = authProvider.signInAnonymously()

        return result.fold(
            onSuccess = { user ->
                _authState.value = AuthState.Authenticated(user)
                Result.success(user)
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Gagal melakukan autentikasi anonim"
                _authState.value = AuthState.Error(errorMsg, error)
                Result.failure(error)
            }
        )
    }

    private suspend fun resolveUserWithToken(user: AuthUser): AuthUser {
        val tokenResult = authProvider.getIdToken(forceRefresh = false)
        return user.copy(idToken = tokenResult.getOrNull())
    }

    override suspend fun getCurrentUserId(): String? {
        val state = _authState.value
        if (state is AuthState.Authenticated) {
            return state.user.uid
        }
        return authProvider.getCurrentUser()?.uid
    }

    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = authMutex.withLock {
        // Ambil token dari provider
        val tokenResult = authProvider.getIdToken(forceRefresh)
        if (tokenResult.isSuccess) {
            val token = tokenResult.getOrThrow()
            updateCachedToken(token)
            return Result.success(token)
        }

        // Jika gagal karena sesi hilang/kedaluwarsa, lakukan re-autentikasi transparan
        val reAuthResult = performAnonymousSignInInternal()
        if (reAuthResult.isSuccess) {
            val reAuthUser = reAuthResult.getOrThrow()
            if (reAuthUser.idToken != null) {
                return Result.success(reAuthUser.idToken)
            }
            return authProvider.getIdToken(forceRefresh = true)
        }

        return Result.failure(
            tokenResult.exceptionOrNull()
                ?: IllegalStateException("Gagal memperbarui ID Token dan re-autentikasi gagal")
        )
    }

    private fun updateCachedToken(token: String) {
        val current = _authState.value
        if (current is AuthState.Authenticated) {
            _authState.value = AuthState.Authenticated(current.user.copy(idToken = token))
        }
    }

    override suspend fun signOut() = authMutex.withLock {
        authProvider.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun deleteAccount(): Result<Unit> = authMutex.withLock {
        val result = authProvider.deleteCurrentUser()
        _authState.value = AuthState.Unauthenticated
        return result
    }
}
