package com.haji2027.core.network.api

import com.haji2027.core.network.auth.AuthManager
import com.haji2027.core.network.dto.ApiErrorCode
import com.haji2027.core.network.dto.ApiException
import com.haji2027.core.network.dto.ClaimTokenRequest
import com.haji2027.core.network.dto.ClaimTokenResponse
import com.haji2027.core.network.dto.GenerateTokenRequest
import com.haji2027.core.network.dto.GenerateTokenResponse
import com.haji2027.core.network.dto.LocationPointDto
import com.haji2027.core.network.dto.PurgeUserDataResponse
import com.haji2027.core.network.dto.RevokeAccessRequest
import com.haji2027.core.network.dto.RevokeAccessResponse

/**
 * Implementasi standar [TrackingApiClient] yang terintegrasi dengan Firebase Auth dan Cloud Functions.
 */
class DefaultTrackingApiClient(
    private val authManager: AuthManager,
    private val functionsCaller: FunctionsCaller
) : TrackingApiClient {

    override suspend fun generatePairingToken(request: GenerateTokenRequest): Result<GenerateTokenResponse> {
        val authResult = authManager.ensureAuthenticated()
        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull() ?: ApiException(ApiErrorCode.AUTH_REQUIRED, "Autentikasi gagal"))
        }

        val params = mapOf<String, Any?>(
            "client_timestamp_ms" to request.clientTimestampMs
        )

        val callResult = functionsCaller.call("generatePairingToken", params)
        return callResult.mapCatching { data ->
            checkErrorResponse(data)
            GenerateTokenResponse(
                status = data["status"] as? String ?: "SUCCESS",
                tokenId = data["token_id"] as? String ?: throw IllegalStateException("token_id tidak ada dalam response"),
                qrPayload = data["qr_payload"] as? String ?: "",
                expiresAtEpochMs = (data["expires_at_epoch_ms"] as? Number)?.toLong() ?: 0L,
                validitySeconds = (data["validity_seconds"] as? Number)?.toInt() ?: 300
            )
        }
    }

    override suspend fun claimPairingToken(request: ClaimTokenRequest): Result<ClaimTokenResponse> {
        val authResult = authManager.ensureAuthenticated()
        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull() ?: ApiException(ApiErrorCode.AUTH_REQUIRED, "Autentikasi gagal"))
        }

        val params = mapOf<String, Any?>(
            "token_id" to request.tokenId,
            "tracker_label" to request.trackerLabel
        )

        val callResult = functionsCaller.call("claimPairingToken", params)
        return callResult.mapCatching { data ->
            checkErrorResponse(data)
            ClaimTokenResponse(
                status = data["status"] as? String ?: "SUCCESS",
                relationId = data["relation_id"] as? String ?: throw IllegalStateException("relation_id tidak ada dalam response"),
                targetUserId = data["target_user_id"] as? String ?: "",
                pairedAtEpochMs = (data["paired_at_epoch_ms"] as? Number)?.toLong() ?: 0L,
                message = data["message"] as? String ?: "Penyambungan keluarga berhasil."
            )
        }
    }

    override suspend fun revokeTrackingAccess(request: RevokeAccessRequest): Result<RevokeAccessResponse> {
        val authResult = authManager.ensureAuthenticated()
        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull() ?: ApiException(ApiErrorCode.AUTH_REQUIRED, "Autentikasi gagal"))
        }

        val params = mapOf<String, Any?>(
            "tracker_user_id" to request.trackerUserId
        )

        val callResult = functionsCaller.call("revokeTrackingAccess", params)
        return callResult.mapCatching { data ->
            checkErrorResponse(data)
            RevokeAccessResponse(
                status = data["status"] as? String ?: "SUCCESS",
                revokedTrackerUserId = data["revoked_tracker_user_id"] as? String ?: request.trackerUserId,
                timestampMs = (data["timestamp_ms"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }

    override suspend fun purgeUserData(): Result<PurgeUserDataResponse> {
        val currentUserId = authManager.getCurrentUserId()
        val callResult = functionsCaller.call("purgeUserData", emptyMap())

        return callResult.mapCatching { data ->
            checkErrorResponse(data)
            // Hapus sesi akun dari auth manager
            authManager.deleteAccount()
            PurgeUserDataResponse(
                status = data["status"] as? String ?: "SUCCESS",
                purgedUserId = data["purged_user_id"] as? String ?: (currentUserId ?: "unknown"),
                message = data["message"] as? String ?: "Seluruh data Anda telah dihapus secara permanen dari server."
            )
        }
    }

    override suspend fun syncLocations(locations: List<LocationPointDto>): Result<Unit> {
        if (locations.isEmpty()) return Result.success(Unit)

        val authResult = authManager.ensureAuthenticated()
        if (authResult.isFailure) {
            return Result.failure(authResult.exceptionOrNull() ?: ApiException(ApiErrorCode.AUTH_REQUIRED, "Autentikasi gagal"))
        }

        val locationsList = locations.map { loc ->
            mapOf(
                "user_id" to loc.userId,
                "latitude" to loc.latitude,
                "longitude" to loc.longitude,
                "accuracy_meters" to loc.accuracyMeters,
                "timestamp_epoch_ms" to loc.timestampEpochMs
            )
        }

        val params = mapOf<String, Any?>("locations" to locationsList)
        val callResult = functionsCaller.call("syncLocations", params)
        return callResult.mapCatching { data ->
            checkErrorResponse(data)
            Unit
        }
    }

    private fun checkErrorResponse(data: Map<String, Any?>) {
        val errorCodeStr = data["error_code"] as? String
        if (!errorCodeStr.isNullOrBlank()) {
            val code = ApiErrorCode.fromCode(errorCodeStr)
            val message = data["message"] as? String ?: code.description
            throw ApiException(code = code, message = message)
        }
    }
}
