package com.haji2027.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haji2027.core.database.entity.LocationSyncQueueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk antrean pengiriman koordinat offline SQLite.
 */
@Dao
interface LocationSyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationSyncQueueEntity): Long

    @Query("SELECT * FROM location_sync_queue ORDER BY timestamp_epoch_ms ASC")
    fun observeQueue(): Flow<List<LocationSyncQueueEntity>>

    @Query("SELECT * FROM location_sync_queue ORDER BY timestamp_epoch_ms ASC")
    suspend fun getAllItems(): List<LocationSyncQueueEntity>

    @Query("SELECT * FROM location_sync_queue ORDER BY timestamp_epoch_ms DESC LIMIT 1")
    suspend fun getLatestItem(): LocationSyncQueueEntity?

    @Query("SELECT COUNT(*) FROM location_sync_queue")
    suspend fun getQueueCount(): Int

    @Query("DELETE FROM location_sync_queue WHERE queue_id = :queueId")
    suspend fun deleteById(queueId: Long)

    @Query("DELETE FROM location_sync_queue WHERE queue_id IN (:queueIds)")
    suspend fun deleteBatch(queueIds: List<Long>)

    /**
     * Memangkas antrean jika melampaui batas maksimal [maxItems] menggunakan strategi FIFO
     * (menghapus sampel tertua dan mempertahankan sampel paling mutakhir).
     */
    @Query("DELETE FROM location_sync_queue WHERE queue_id NOT IN (SELECT queue_id FROM location_sync_queue ORDER BY timestamp_epoch_ms DESC LIMIT :maxItems)")
    suspend fun trimQueueToSize(maxItems: Int = LocationSyncQueueEntity.MAX_QUEUE_CAPACITY)

    @Query("DELETE FROM location_sync_queue")
    suspend fun clearQueue()
}
