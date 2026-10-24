package com.haji2027.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Token pairing sekali pakai berbatas waktu 5 menit.
 */
@Serializable
data class PairingToken(
    @SerialName("token_id") val tokenId: String,
    @SerialName("target_uid") val targetUid: String,
    @SerialName("status") val status: TokenStatus = TokenStatus.PENDING,
    @SerialName("expires_at_epoch_ms") val expiresAtEpochMs: Long,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Serializable
enum class TokenStatus {
    @SerialName("PENDING") PENDING,
    @SerialName("CLAIMED") CLAIMED,
    @SerialName("EXPIRED") EXPIRED
}

/**
 * Relasi pelacakan konsensus antara orang yang dilacak (target) dan pelacak (tracker).
 */
@Serializable
data class TrackingRelation(
    @SerialName("relation_id") val relationId: String,
    @SerialName("target_uid") val targetUid: String,
    @SerialName("tracker_uid") val trackerUid: String,
    @SerialName("tracker_label") val trackerLabel: String? = null,
    @SerialName("status") val status: TrackingStatus = TrackingStatus.ACTIVE,
    @SerialName("paired_at_epoch_ms") val pairedAtEpochMs: Long = System.currentTimeMillis()
)

@Serializable
enum class TrackingStatus {
    @SerialName("ACTIVE") ACTIVE,
    @SerialName("REVOKED") REVOKED,
    @SerialName("PAUSED") PAUSED
}

/**
 * Koordinat telemetri lokasi mutakhir (Last Known Location).
 */
@Serializable
data class UserLocation(
    @SerialName("user_id") val userId: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("accuracy_meters") val accuracyMeters: Float,
    @SerialName("updated_at_epoch_ms") val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Status posisi relatif anggota keluarga untuk ditampilkan pada antarmuka pelacak.
 */
data class RelativeTarget(
    val userId: String,
    val displayName: String,
    val distanceMeters: Float,
    val bearingDegrees: Float,
    val accuracyMeters: Float,
    val lastUpdatedEpochMs: Long,
    val isStale: Boolean
)
