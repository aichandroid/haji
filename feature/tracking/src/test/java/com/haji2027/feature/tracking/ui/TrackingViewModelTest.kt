package com.haji2027.feature.tracking.ui

import android.content.Context
import com.haji2027.core.model.UserLocation
import com.haji2027.core.network.api.TrackingApiClient
import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.auth.AuthState
import com.haji2027.core.network.auth.AuthUser
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.ClaimTokenResponse
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.GenerateTokenResponse
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.PurgeUserDataResponse
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.core.network.dto.RevokeAccessResponse
import com.haji2027.feature.tracking.service.TrackingController
import com.haji2027.feature.tracking.service.TrackingServiceStatus
import com.haji2027.feature.tracking.sync.SyncManager
import com.haji2027.feature.tracking.sync.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeApiClient: FakeTrackingApiClient
    private lateinit var fakeAuthManager: FakeAuthManager
    private lateinit var fakeController: FakeTrackingController
    private lateinit var fakeSyncManager: FakeSyncManager
    private lateinit var viewModel: TrackingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fakeApiClient = FakeTrackingApiClient()
        fakeAuthManager = FakeAuthManager()
        fakeController = FakeTrackingController()
        fakeSyncManager = FakeSyncManager()

        viewModel = TrackingViewModel(
            apiClient = fakeApiClient,
            authManager = fakeAuthManager,
            trackingController = fakeController,
            syncManager = fakeSyncManager,
            ioDispatcher = testDispatcher,
            tickerDelayMs = 1000L
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `testInitialStateAndAuthentication`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(TrackingTab.MAIN, state.currentTab)
        assertEquals(2, state.activeTrackers.size)
        assertFalse(state.isServiceRunning)
        assertEquals("user_test_123", state.currentUserId)
        assertTrue(state.shareQrState is ShareQrState.Idle)
        assertTrue(state.claimQrState is ClaimQrState.Idle)
    }

    @Test
    fun `testServiceStatusFlowUpdatesUiState`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        fakeController.updateStatus(TrackingServiceStatus(isRunning = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isServiceRunning)

        fakeController.updateStatus(TrackingServiceStatus(isRunning = false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isServiceRunning)
    }

    @Test
    fun `testGenerateNewQrTokenSuccessAndCountdownExpiry`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.generateNewQrToken()
        testDispatcher.scheduler.advanceUntilIdle()

        val readyState = viewModel.uiState.value.shareQrState
        assertTrue(readyState is ShareQrState.Ready)
        val ready = readyState as ShareQrState.Ready
        assertEquals("tok_abc_123", ready.tokenId)
        assertEquals(300, ready.remainingSeconds)
        assertEquals("hajj2027://pair?t=tok_abc_123&u=user_test_123", ready.qrPayload)

        // Majukan waktu 1 detik
        testDispatcher.scheduler.advanceTimeBy(1000L)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfter1Sec = viewModel.uiState.value.shareQrState as ShareQrState.Ready
        assertEquals(299, stateAfter1Sec.remainingSeconds)

        // Majukan waktu sisa 299 detik sehingga total 300 detik
        testDispatcher.scheduler.advanceTimeBy(300000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shareQrState is ShareQrState.Expired)
    }

    @Test
    fun `testGenerateNewQrTokenFailureSetsErrorState`() = testScope.runTest {
        fakeApiClient.shouldFailGenerate = true
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.generateNewQrToken()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.shareQrState
        assertTrue(state is ShareQrState.Error)
        assertEquals("Koneksi gagal ke server", (state as ShareQrState.Error).message)
    }

    @Test
    fun `testClaimScannedQrSuccess`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val qrUrl = "hajj2027://pair?t=tok_xyz_999&u=user_ayah_777"
        viewModel.claimScannedQr(qrUrl, "Istri Tercinta")
        testDispatcher.scheduler.advanceUntilIdle()

        val claimState = viewModel.uiState.value.claimQrState
        assertTrue(claimState is ClaimQrState.Success)
        val success = claimState as ClaimQrState.Success
        assertEquals("rel_999", success.relationId)
        assertEquals("user_ayah_777", success.targetUserId)
        assertEquals("tok_xyz_999", fakeApiClient.lastClaimRequest?.tokenId)
        assertEquals("Istri Tercinta", fakeApiClient.lastClaimRequest?.trackerLabel)
    }

    @Test
    fun `testClaimScannedQrBlankLabelRejectedWithoutApiCall`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val qrUrl = "hajj2027://pair?t=tok_xyz_999&u=user_ayah_777"
        viewModel.claimScannedQr(qrUrl, "   ")
        testDispatcher.scheduler.advanceUntilIdle()

        val claimState = viewModel.uiState.value.claimQrState
        assertTrue(claimState is ClaimQrState.Error)
        assertEquals("Label hubungan keluarga tidak boleh kosong", (claimState as ClaimQrState.Error).message)
        assertNull(fakeApiClient.lastClaimRequest)
    }

    @Test
    fun `testClaimScannedQrInvalidQrPayloadFormatRejected`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.claimScannedQr("https://google.com", "Anak")
        testDispatcher.scheduler.advanceUntilIdle()

        val claimState = viewModel.uiState.value.claimQrState
        assertTrue(claimState is ClaimQrState.Error)
        assertTrue((claimState as ClaimQrState.Error).message.contains("Format QR tidak valid"))
        assertNull(fakeApiClient.lastClaimRequest)
    }

    @Test
    fun `testClaimScannedQrApiFailure`() = testScope.runTest {
        fakeApiClient.shouldFailClaim = true
        testDispatcher.scheduler.advanceUntilIdle()

        val qrUrl = "hajj2027://pair?t=tok_xyz_999&u=user_ayah_777"
        viewModel.claimScannedQr(qrUrl, "Anak Pertama")
        testDispatcher.scheduler.advanceUntilIdle()

        val claimState = viewModel.uiState.value.claimQrState
        assertTrue(claimState is ClaimQrState.Error)
        assertEquals("Token pairing tidak valid atau telah kedaluwarsa", (claimState as ClaimQrState.Error).message)
    }

    @Test
    fun `testRevokeConsentSuccessRemovesTracker`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.activeTrackers.size)

        viewModel.revokeConsent("tracker_fatimah_01")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.uiState.value.activeTrackers
        assertEquals(1, updated.size)
        assertEquals("tracker_dahlan_02", updated[0].trackerUserId)
        assertEquals("Izin pelacakan berhasil dicabut.", viewModel.uiState.value.userFeedbackMessage)
    }

    @Test
    fun `testRevokeConsentFailurePreservesTrackerAndShowsError`() = testScope.runTest {
        fakeApiClient.shouldFailRevoke = true
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.revokeConsent("tracker_fatimah_01")
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = viewModel.uiState.value.activeTrackers
        assertEquals(2, updated.size)
        assertTrue(viewModel.uiState.value.userFeedbackMessage?.contains("Gagal mencabut izin") == true)
    }

    @Test
    fun `testStartAndStopTrackingService`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.startTrackingService()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("user_test_123", fakeController.lastStartedUserId)
        assertTrue(fakeSyncManager.isListening)

        viewModel.stopAllTracking()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeController.isStopCalled)
        assertFalse(fakeSyncManager.isListening)
    }

    @Test
    fun `testNavigationTransitionsAndBack`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.navigateTo(TrackingTab.SHARE_QR)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TrackingTab.SHARE_QR, viewModel.uiState.value.currentTab)
        assertTrue(viewModel.uiState.value.shareQrState is ShareQrState.Ready)

        viewModel.navigateBack()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TrackingTab.MAIN, viewModel.uiState.value.currentTab)

        viewModel.navigateTo(TrackingTab.SCAN_QR)
        assertEquals(TrackingTab.SCAN_QR, viewModel.uiState.value.currentTab)

        viewModel.navigateTo(TrackingTab.CONSENT_LIST)
        assertEquals(TrackingTab.CONSENT_LIST, viewModel.uiState.value.currentTab)
    }

    @Test
    fun `testResetClaimStateAndClearFeedbackMessage`() = testScope.runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.claimScannedQr("invalid_qr", "Anak")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.claimQrState is ClaimQrState.Error)

        viewModel.resetClaimState()
        assertTrue(viewModel.uiState.value.claimQrState is ClaimQrState.Idle)

        viewModel.stopAllTracking()
        assertNotNull(viewModel.uiState.value.userFeedbackMessage)

        viewModel.clearFeedbackMessage()
        assertNull(viewModel.uiState.value.userFeedbackMessage)
    }
}

// -------------------------------------------------------------
// FAKES & STUBS UNTUK PENGUJIAN UNIT
// -------------------------------------------------------------

private class FakeTrackingApiClient : TrackingApiClient {
    var shouldFailGenerate = false
    var shouldFailClaim = false
    var shouldFailRevoke = false

    var lastClaimRequest: ClaimTokenRequest? = null
    var lastRevokeRequest: RevokeAccessRequest? = null

    override suspend fun generatePairingToken(request: GenerateTokenRequest): Result<GenerateTokenResponse> {
        return if (shouldFailGenerate) {
            Result.failure(RuntimeException("Koneksi gagal ke server"))
        } else {
            Result.success(
                GenerateTokenResponse(
                    status = "success",
                    tokenId = "tok_abc_123",
                    qrPayload = "hajj2027://pair?t=tok_abc_123&u=user_test_123",
                    expiresAtEpochMs = System.currentTimeMillis() + 300_000L,
                    validitySeconds = 300
                )
            )
        }
    }

    override suspend fun claimPairingToken(request: ClaimTokenRequest): Result<ClaimTokenResponse> {
        lastClaimRequest = request
        return if (shouldFailClaim) {
            Result.failure(RuntimeException("Token pairing tidak valid atau telah kedaluwarsa"))
        } else {
            Result.success(
                ClaimTokenResponse(
                    status = "success",
                    relationId = "rel_999",
                    targetUserId = "user_ayah_777",
                    pairedAtEpochMs = System.currentTimeMillis(),
                    message = "Hubungan pelacakan berhasil diaktifkan"
                )
            )
        }
    }

    override suspend fun revokeTrackingAccess(request: RevokeAccessRequest): Result<RevokeAccessResponse> {
        lastRevokeRequest = request
        return if (shouldFailRevoke) {
            Result.failure(RuntimeException("Gagal menghapus relasi izin pelacakan"))
        } else {
            Result.success(
                RevokeAccessResponse(
                    status = "success",
                    revokedTrackerUserId = request.trackerUserId,
                    timestampMs = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun purgeUserData(): Result<PurgeUserDataResponse> {
        return Result.success(PurgeUserDataResponse("success", "user_test_123", "Data purged"))
    }

    override suspend fun syncLocations(locations: List<LocationPointDto>): Result<Unit> {
        return Result.success(Unit)
    }
}

private class FakeAuthManager : AuthManager {
    private val testUser = AuthUser(uid = "user_test_123", isAnonymous = true)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Authenticated(testUser))
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun ensureAuthenticated(): Result<AuthUser> = Result.success(testUser)
    override suspend fun signInAnonymously(): Result<AuthUser> = Result.success(testUser)
    override suspend fun getCurrentUserId(): String? = testUser.uid
    override suspend fun getIdToken(forceRefresh: Boolean): Result<String> = Result.success("mock_bearer_token")
    override suspend fun signOut() { _authState.value = AuthState.Unauthenticated }
    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}

private class FakeTrackingController : TrackingController {
    private val _status = MutableStateFlow(TrackingServiceStatus())
    override val status: StateFlow<TrackingServiceStatus> = _status

    var lastStartedUserId: String? = null
    var isStopCalled: Boolean = false

    fun updateStatus(newStatus: TrackingServiceStatus) {
        _status.value = newStatus
    }

    override fun startTracking(context: Context?, userId: String) {
        lastStartedUserId = userId
        _status.value = _status.value.copy(isRunning = true)
    }

    override fun stopTracking(context: Context?) {
        isStopCalled = true
        _status.value = _status.value.copy(isRunning = false)
    }
}

private class FakeSyncManager : SyncManager {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState

    var isListening: Boolean = false

    override suspend fun enqueueLocation(location: UserLocation): Long = 1L
    override suspend fun syncPendingLocations(): Result<Int> = Result.success(0)

    override fun startListeningToTrackingService() {
        isListening = true
    }

    override fun stopListeningToTrackingService() {
        isListening = false
    }
}
