package com.haji2027.core.network.auth

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeAuthProvider: FakeFirebaseAuthProvider
    private lateinit var authManager: DefaultFirebaseAuthManager

    @Before
    fun setup() {
        fakeAuthProvider = FakeFirebaseAuthProvider()
        authManager = DefaultFirebaseAuthManager(fakeAuthProvider)
    }

    @Test
    fun `initial state is Unauthenticated`() {
        assertEquals(AuthState.Unauthenticated, authManager.authState.value)
        assertNull(fakeAuthProvider.activeUser)
    }

    @Test
    fun `ensureAuthenticated signs in anonymously if no session exists`() = testScope.runTest {
        val result = authManager.ensureAuthenticated()

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("user_anon_123", user.uid)
        assertTrue(user.isAnonymous)
        assertEquals("token_mock_abc", user.idToken)

        val state = authManager.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("user_anon_123", (state as AuthState.Authenticated).user.uid)
    }

    @Test
    fun `ensureAuthenticated reuses existing session without calling signInAnonymously again`() = testScope.runTest {
        fakeAuthProvider.activeUser = AuthUser(
            uid = "existing_user_456",
            isAnonymous = true,
            idToken = "existing_token_xyz"
        )

        val result = authManager.ensureAuthenticated()

        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("existing_user_456", user.uid)
        assertEquals(0, fakeAuthProvider.signInCallCount)
        assertTrue(authManager.authState.value is AuthState.Authenticated)
    }

    @Test
    fun `signInAnonymously sets Error state when provider throws exception`() = testScope.runTest {
        fakeAuthProvider.shouldFailSignIn = true

        val result = authManager.signInAnonymously()

        assertTrue(result.isFailure)
        val state = authManager.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Koneksi jaringan terputus ke Firebase Auth", (state as AuthState.Error).message)
    }

    @Test
    fun `getIdToken returns valid token and updates user state`() = testScope.runTest {
        authManager.ensureAuthenticated()

        fakeAuthProvider.tokenToReturn = "refreshed_token_789"
        val tokenResult = authManager.getIdToken(forceRefresh = true)

        assertTrue(tokenResult.isSuccess)
        assertEquals("refreshed_token_789", tokenResult.getOrThrow())

        val state = authManager.authState.value as AuthState.Authenticated
        assertEquals("refreshed_token_789", state.user.idToken)
    }

    @Test
    fun `getIdToken transparently re-authenticates when token retrieval fails`() = testScope.runTest {
        // Anggap sesi awal ada
        authManager.ensureAuthenticated()

        // Buat pengambilan token gagal tapi sign-in reauth berhasil
        fakeAuthProvider.shouldFailGetIdToken = true
        fakeAuthProvider.tokenToReturn = "reauth_token_999"

        val tokenResult = authManager.getIdToken()

        assertTrue(tokenResult.isSuccess)
        assertEquals("user_anon_123", authManager.getCurrentUserId())
    }

    @Test
    fun `signOut clears active session and resets state to Unauthenticated`() = testScope.runTest {
        authManager.ensureAuthenticated()
        assertTrue(authManager.authState.value is AuthState.Authenticated)

        authManager.signOut()

        assertEquals(AuthState.Unauthenticated, authManager.authState.value)
        assertNull(fakeAuthProvider.activeUser)
    }

    @Test
    fun `deleteAccount removes user permanently and resets state`() = testScope.runTest {
        authManager.ensureAuthenticated()
        assertNotNull(authManager.getCurrentUserId())

        val deleteResult = authManager.deleteAccount()

        assertTrue(deleteResult.isSuccess)
        assertEquals(AuthState.Unauthenticated, authManager.authState.value)
        assertNull(fakeAuthProvider.activeUser)
        assertTrue(fakeAuthProvider.isAccountDeleted)
    }

    private class FakeFirebaseAuthProvider : FirebaseAuthProvider {
        var activeUser: AuthUser? = null
        var shouldFailSignIn: Boolean = false
        var shouldFailGetIdToken: Boolean = false
        var tokenToReturn: String = "token_mock_abc"
        var signInCallCount: Int = 0
        var isAccountDeleted: Boolean = false

        override suspend fun signInAnonymously(): Result<AuthUser> {
            signInCallCount++
            if (shouldFailSignIn) {
                return Result.failure(IllegalStateException("Koneksi jaringan terputus ke Firebase Auth"))
            }
            val user = AuthUser(
                uid = "user_anon_123",
                isAnonymous = true,
                idToken = tokenToReturn
            )
            activeUser = user
            return Result.success(user)
        }

        override suspend fun getCurrentUser(): AuthUser? = activeUser

        override suspend fun getIdToken(forceRefresh: Boolean): Result<String> {
            if (shouldFailGetIdToken) {
                shouldFailGetIdToken = false // reset for subsequent re-auth
                return Result.failure(IllegalStateException("Token session expired (401)"))
            }
            return Result.success(tokenToReturn)
        }

        override suspend fun signOut() {
            activeUser = null
        }

        override suspend fun deleteCurrentUser(): Result<Unit> {
            activeUser = null
            isAccountDeleted = true
            return Result.success(Unit)
        }
    }
}
