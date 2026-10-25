package com.haji2027.core.database.repository

import com.haji2027.core.database.entity.LocationSyncQueueEntity
import com.haji2027.core.model.UserLocation
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori antrean offline telemetri koordinat jemaah.
 * Menegakkan kapasitas maksimal 50 sampel secara FIFO (FR-TRACK-006).
 */
interface LocationQueueRepository {
    fun observePendingQueue(): Flow<List<LocationSyncQueueEntity>>
    suspend fun enqueueLocation(location: UserLocation): Long
    suspend fun getPendingItems(): List<LocationSyncQueueEntity>
    suspend fun getLatestPendingLocation(): UserLocation?
    suspend fun getPendingCount(): Int
    suspend fun removeItems(queueIds: List<Long>)
    suspend fun clearAll()
}
