package com.haji2027.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload untuk pembuatan token pairing QR sekali pakai (POST /api/generatePairingToken).
 */
@Serializable
data class GenerateTokenRequest(
    @SerialName("client_timestamp_ms") val clientTimestampMs: Long = System.currentTimeMillis()
)

/**
 * Response payload sukses pembuatan token pairing QR (HTTP 201 Created).
 */
@Serializable
data class GenerateTokenResponse(
    @SerialName("status") val status: String,
    @SerialName("token_id") val tokenId: String,
    @SerialName("qr_payload") val qrPayload: String,
    @SerialName("expires_at_epoch_ms") val expiresAtEpochMs: Long,
    @SerialName("validity_seconds") val validitySeconds: Int = 300
)

/**
 * Request payload saat pihak keluarga memindai QR untuk mengklaim token (POST /api/claimPairingToken).
 */
@Serializable
data class ClaimTokenRequest(
    @SerialName("token_id") val tokenId: String,
    @SerialName("tracker_label") val trackerLabel: String
)

/**
 * Response payload sukses klaim token pairing (HTTP 200 OK).
 */
@Serializable
data class ClaimTokenResponse(
    @SerialName("status") val status: String,
    @SerialName("relation_id") val relationId: String,
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("paired_at_epoch_ms") val pairedAtEpochMs: Long,
    @SerialName("message") val message: String
)

/**
 * Request payload untuk pencabutan izin pelacakan secara instan (POST /api/revokeTrackingAccess).
 */
@Serializable
data class RevokeAccessRequest(
    @SerialName("tracker_user_id") val trackerUserId: String
)

/**
 * Response payload sukses pencabutan izin pelacakan (HTTP 200 OK).
 */
@Serializable
data class RevokeAccessResponse(
    @SerialName("status") val status: String,
    @SerialName("revoked_tracker_user_id") val revokedTrackerUserId: String,
    @SerialName("timestamp_ms") val timestampMs: Long
)

/**
 * Response payload saat akun dan seluruh data server dibersihkan (POST /api/purgeUserData).
 */
@Serializable
data class PurgeUserDataResponse(
    @SerialName("status") val status: String,
    @SerialName("purged_user_id") val purgedUserId: String,
    @SerialName("message") val message: String
)

/**
 * DTO untuk sinkronisasi batch koordinat dari antrean offline SQLite ke Cloud Firestore.
 */
@Serializable
data class LocationSyncRequest(
    @SerialName("locations") val locations: List<LocationPointDto>
)

@Serializable
data class LocationPointDto(
    @SerialName("user_id") val userId: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("accuracy_meters") val accuracyMeters: Float,
    @SerialName("timestamp_epoch_ms") val timestampEpochMs: Long
)
