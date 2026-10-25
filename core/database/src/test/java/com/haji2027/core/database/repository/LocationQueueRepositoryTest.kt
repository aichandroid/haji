package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.LocationSyncQueueDao
import com.haji2027.core.database.entity.LocationSyncQueueEntity
import com.haji2027.core.model.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationQueueRepositoryTest {

    private lateinit var fakeDao: FakeLocationSyncQueueDao
    private lateinit var repository: DefaultLocationQueueRepository

    @Before
    fun setup() {
        fakeDao = FakeLocationSyncQueueDao()
        repository = DefaultLocationQueueRepository(fakeDao, maxCapacity = 50)
    }

    @Test
    fun `enqueueLocation inserts item and retrieves pending count`() = runBlocking {
        val loc = UserLocation(
            userId = "usr_ayah_123",
            latitude = 21.4225,
            longitude = 39.8262,
            accuracyMeters = 4.5f,
            updatedAtEpochMs = 1000L
        )

        repository.enqueueLocation(loc)

        assertEquals(1, repository.getPendingCount())
        val latest = repository.getLatestPendingLocation()
        assertNotNull(latest)
        assertEquals("usr_ayah_123", latest!!.userId)
        assertEquals(21.4225, latest.latitude, 0.0001)
    }

    @Test
    fun `enqueueLocation enforces FIFO max capacity of 50 samples`() = runBlocking {
        // Enqueue 60 sampel berurutan
        for (i in 1..60) {
            val loc = UserLocation(
                userId = "usr_ayah_123",
                latitude = 21.4200 + (i * 0.0001),
                longitude = 39.8200 + (i * 0.0001),
                accuracyMeters = 5.0f,
                updatedAtEpochMs = i * 1000L
            )
            repository.enqueueLocation(loc)
        }

        // Kapasitas harus terpangkas tepat 50 (sampel tertua 1 s.d. 10 terhapus)
        assertEquals(50, repository.getPendingCount())

        val pending = repository.getPendingItems()
        assertEquals(50, pending.size)

        // Sampel tertua yang tersisa harus bertimestamp 11_000L (sampel ke-11)
        assertEquals(11_000L, pending.first().timestampEpochMs)
        // Sampel terbaru harus bertimestamp 60_000L (sampel ke-60)
        assertEquals(60_000L, pending.last().timestampEpochMs)
    }

    @Test
    fun `removeItems deletes specified queue IDs`() = runBlocking {
        val id1 = repository.enqueueLocation(UserLocation("u1", 21.0, 39.0, 5f, 1000L))
        val id2 = repository.enqueueLocation(UserLocation("u1", 21.1, 39.1, 5f, 2000L))
        val id3 = repository.enqueueLocation(UserLocation("u1", 21.2, 39.2, 5f, 3000L))

        assertEquals(3, repository.getPendingCount())

        repository.removeItems(listOf(id1, id2))

        assertEquals(1, repository.getPendingCount())
        val remaining = repository.getPendingItems().first()
        assertEquals(id3, remaining.queueId)
    }

    @Test
    fun `clearAll empties entire offline queue`() = runBlocking {
        repository.enqueueLocation(UserLocation("u1", 21.0, 39.0, 5f, 1000L))
        repository.enqueueLocation(UserLocation("u1", 21.1, 39.1, 5f, 2000L))

        assertEquals(2, repository.getPendingCount())

        repository.clearAll()
        assertEquals(0, repository.getPendingCount())
        assertNull(repository.getLatestPendingLocation())
    }

    private class FakeLocationSyncQueueDao : LocationSyncQueueDao {
        private val items = mutableListOf<LocationSyncQueueEntity>()
        private var nextId = 1L
        private val _flow = MutableStateFlow<List<LocationSyncQueueEntity>>(emptyList())

        override suspend fun insert(entity: LocationSyncQueueEntity): Long {
            val id = nextId++
            val saved = entity.copy(queueId = id)
            items.add(saved)
            _flow.value = items.toList()
            return id
        }

        override fun observeQueue(): Flow<List<LocationSyncQueueEntity>> = _flow

        override suspend fun getAllItems(): List<LocationSyncQueueEntity> {
            return items.sortedBy { it.timestampEpochMs }
        }

        override suspend fun getLatestItem(): LocationSyncQueueEntity? {
            return items.maxByOrNull { it.timestampEpochMs }
        }

        override suspend fun getQueueCount(): Int = items.size

        override suspend fun deleteById(queueId: Long) {
            items.removeAll { it.queueId == queueId }
            _flow.value = items.toList()
        }

        override suspend fun deleteBatch(queueIds: List<Long>) {
            items.removeAll { it.queueId in queueIds }
            _flow.value = items.toList()
        }

        override suspend fun trimQueueToSize(maxItems: Int) {
            if (items.size > maxItems) {
                // Pertahankan maxItems terbaru (timestamp descending)
                val sortedDesc = items.sortedByDescending { it.timestampEpochMs }
                val toKeep = sortedDesc.take(maxItems).map { it.queueId }.toSet()
                items.removeAll { it.queueId !in toKeep }
                _flow.value = items.toList()
            }
        }

        override suspend fun clearQueue() {
            items.clear()
            _flow.value = emptyList()
        }
    }
}
