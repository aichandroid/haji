package com.haji2027.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.haji2027.core.model.UserLocation

/**
 * Entitas tabel SQLite `location_sync_queue` untuk antrean offline telemetri koordinat jemaah.
 * Kapasitas maksimal dibatasi 50 sampel dengan strategi FIFO untuk menghemat memori perangkat.
 *
 * Selaras dengan FR-TRACK-006 dan Bagian 17.6 & 18.1 SYSTEM_DESIGN_DOCUMENT.md.
 */
@Entity(
    tableName = "location_sync_queue",
    indices = [
        Index(value = ["timestamp_epoch_ms"], name = "idx_queue_timestamp")
    ]
)
data class LocationSyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "queue_id")
    val queueId: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "accuracy_meters")
    val accuracyMeters: Float,

    @ColumnInfo(name = "timestamp_epoch_ms")
    val timestampEpochMs: Long,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0
) {
    fun toDomainModel(): UserLocation {
        return UserLocation(
            userId = userId,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            updatedAtEpochMs = timestampEpochMs
        )
    }

    companion object {
        const val MAX_QUEUE_CAPACITY = 50

        fun fromDomainModel(userLocation: UserLocation, retryCount: Int = 0): LocationSyncQueueEntity {
            return LocationSyncQueueEntity(
                userId = userLocation.userId,
                latitude = userLocation.latitude,
                longitude = userLocation.longitude,
                accuracyMeters = userLocation.accuracyMeters,
                timestampEpochMs = userLocation.updatedAtEpochMs,
                retryCount = retryCount
            )
        }
    }
}
