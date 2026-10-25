package com.haji2027.core.network.api

import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.auth.AuthState
import com.haji2027.core.network.auth.AuthUser
import com.haji2027.core.network.dto.ApiErrorCode
import com.haji2027.core.network.dto.ApiException
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.RevokeAccessRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrackingApiClientTest {

    private lateinit var fakeAuthManager: FakeAuthManager
    private lateinit var fakeFunctionsCaller: FakeFunctionsCaller
    private lateinit var apiClient: DefaultTrackingApiClient

    @Before
    fun setup() {
        fakeAuthManager = FakeAuthManager()
        fakeFunctionsCaller = FakeFunctionsCaller()
        apiClient = DefaultTrackingApiClient(fakeAuthManager, fakeFunctionsCaller)
    }

    @Test
    fun `generatePairingToken calls Cloud Functions and returns valid response`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf(
            "status" to "SUCCESS",
            "token_id" to "tok_9f8c2e1b4a7d",
            "qr_payload" to "hajj2027://pair?t=tok_9f8c2e1b4a7d&u=usr_ayah_123",
            "expires_at_epoch_ms" to 1779600300000L,
            "validity_seconds" to 300
        )

        val result = apiClient.generatePairingToken(GenerateTokenRequest(1779600000000L))

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("tok_9f8c2e1b4a7d", response.tokenId)
        assertEquals(300, response.validitySeconds)
        assertEquals("generatePairingToken", fakeFunctionsCaller.lastCalledFunction)
    }

    @Test
    fun `claimPairingToken calls Cloud Functions and returns relation id`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf(
            "status" to "SUCCESS",
            "relation_id" to "rel_usr_ayah_123_usr_fatimah_456",
            "target_user_id" to "usr_ayah_123",
            "paired_at_epoch_ms" to 1779600050000L,
            "message" to "Penyambungan keluarga berhasil. Lokasi mulai dipantau."
        )

        val result = apiClient.claimPairingToken(
            ClaimTokenRequest("tok_9f8c2e1b4a7d", "Fatimah (Anak)")
        )

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("rel_usr_ayah_123_usr_fatimah_456", response.relationId)
        assertEquals("usr_ayah_123", response.targetUserId)
        assertEquals("claimPairingToken", fakeFunctionsCaller.lastCalledFunction)
    }

    @Test
    fun `revokeTrackingAccess calls Cloud Functions and returns confirmation`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf(
            "status" to "SUCCESS",
            "revoked_tracker_user_id" to "usr_fatimah_456",
            "timestamp_ms" to 1779600100000L
        )

        val result = apiClient.revokeTrackingAccess(RevokeAccessRequest("usr_fatimah_456"))

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("usr_fatimah_456", response.revokedTrackerUserId)
        assertEquals("revokeTrackingAccess", fakeFunctionsCaller.lastCalledFunction)
    }

    @Test
    fun `purgeUserData calls Cloud Functions and triggers account deletion`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf(
            "status" to "SUCCESS",
            "purged_user_id" to "usr_ayah_123",
            "message" to "Seluruh data Anda telah dihapus secara permanen dari server."
        )

        val result = apiClient.purgeUserData()

        assertTrue(result.isSuccess)
        val response = result.getOrThrow()
        assertEquals("usr_ayah_123", response.purgedUserId)
        assertTrue(fakeAuthManager.isDeleteAccountCalled)
    }

    @Test
    fun `api returns error_code throws ApiException with proper code`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf(
            "error_code" to "TOKEN_EXPIRED",
            "message" to "Kode QR telah kedaluwarsa. Silakan minta kerabat menampilkan kode QR baru."
        )

        val result = apiClient.claimPairingToken(
            ClaimTokenRequest("tok_expired", "Anak")
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ApiException)
        val apiException = exception as ApiException
        assertEquals(ApiErrorCode.TOKEN_EXPIRED, apiException.code)
        assertEquals(410, apiException.httpStatus)
        assertTrue(apiException.message.contains("kedaluwarsa"))
    }

    @Test
    fun `syncLocations sends batch without error`() = runBlocking {
        fakeFunctionsCaller.mockResponse = mapOf("status" to "SUCCESS")

        val result = apiClient.syncLocations(
            listOf(
                LocationPointDto("usr_1", 21.42, 39.82, 5f, 1000L)
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("syncLocations", fakeFunctionsCaller.lastCalledFunction)
    }

    private class FakeAuthManager : AuthManager {
        val user = AuthUser("usr_ayah_123", isAnonymous = true, idToken = "id_token_123")
        private val _authState = MutableStateFlow<AuthState>(AuthState.Authenticated(user))
        override val authState: StateFlow<AuthState> = _authState

        var isDeleteAccountCalled: Boolean = false

        override suspend fun ensureAuthenticated(): Result<AuthUser> = Result.success(user)
        override suspend fun signInAnonymously(): Result<AuthUser> = Result.success(user)
        override suspend fun getCurrentUserId(): String? = user.uid
        override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = Result.success(user.idToken ?: "")
        override suspend fun signOut() {
            _authState.value = AuthState.Unauthenticated
        }
        override suspend fun deleteAccount(): Result<Unit> {
            isDeleteAccountCalled = true
            _authState.value = AuthState.Unauthenticated
            return Result.success(Unit)
        }
    }

    private class FakeFunctionsCaller : FunctionsCaller {
        var mockResponse: Map<String, Any?> = emptyMap()
        var lastCalledFunction: String? = null
        var lastCalledData: Map<String, Any?>? = null

        override suspend fun call(functionName: String, data: Map<String, Any?>): Result<Map<String, Any?>> {
            lastCalledFunction = functionName
            lastCalledData = data
            return Result.success(mockResponse)
        }
    }
}
