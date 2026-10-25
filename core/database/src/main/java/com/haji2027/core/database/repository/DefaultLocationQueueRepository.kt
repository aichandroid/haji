package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.LocationSyncQueueDao
import com.haji2027.core.database.entity.LocationSyncQueueEntity
import com.haji2027.core.model.UserLocation
import kotlinx.coroutines.flow.Flow

/**
 * Implementasi standar repositori antrean offline telemetri koordinat.
 */
class DefaultLocationQueueRepository(
    private val dao: LocationSyncQueueDao,
    private val maxCapacity: Int = LocationSyncQueueEntity.MAX_QUEUE_CAPACITY
) : LocationQueueRepository {

    override fun observePendingQueue(): Flow<List<LocationSyncQueueEntity>> {
        return dao.observeQueue()
    }

    override suspend fun enqueueLocation(location: UserLocation): Long {
        val entity = LocationSyncQueueEntity.fromDomainModel(location)
        val id = dao.insert(entity)
        // Pangkas otomatis melebihi 50 sampel secara FIFO
        dao.trimQueueToSize(maxCapacity)
        return id
    }

    override suspend fun getPendingItems(): List<LocationSyncQueueEntity> {
        return dao.getAllItems()
    }

    override suspend fun getLatestPendingLocation(): UserLocation? {
        return dao.getLatestItem()?.toDomainModel()
    }

    override suspend fun getPendingCount(): Int {
        return dao.getQueueCount()
    }

    override suspend fun removeItems(queueIds: List<Long>) {
        if (queueIds.isNotEmpty()) {
            dao.deleteBatch(queueIds)
        }
    }

    override suspend fun clearAll() {
        dao.clearQueue()
    }
}
