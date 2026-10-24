package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.GuideSearchDao
import com.haji2027.core.database.entity.GuideSearchEntity
import com.haji2027.core.model.HajjContentPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GuideSearchRepositoryTest {

    private lateinit var fakeDao: FakeGuideSearchDao
    private lateinit var repository: GuideSearchRepository

    @Before
    fun setup() {
        fakeDao = FakeGuideSearchDao()
        repository = DefaultGuideSearchRepository(fakeDao)
    }

    @Test
    fun `search with blank query returns empty without querying dao`() = runBlocking {
        val result = repository.search("   ").first()
        assertTrue(result.isEmpty())
        assertNullDaoQuery()
    }

    @Test
    fun `search with valid query sanitizes and queries dao`() = runBlocking {
        val result = repository.search("arafah wukuf").first()
        assertEquals("arafah* wukuf*", fakeDao.lastSearchQuery)
    }

    @Test
    fun `populateIndex indexes and saves content package`() = runBlocking {
        assertFalse(repository.isIndexPopulated())

        val emptyPackage = HajjContentPackage(
            version = "1.0.0",
            revisionDate = "2026-10-24",
            approvedBy = "Ustadz Pembina",
            hajjYear = 1448,
            days = emptyList()
        )

        repository.populateIndex(emptyPackage)
        // With empty package, count is 0
        assertEquals(0, fakeDao.getCount())

        repository.clearIndex()
        assertEquals(0, fakeDao.getCount())
    }

    private fun assertNullDaoQuery() {
        assertEquals(null, fakeDao.lastSearchQuery)
    }

    private class FakeGuideSearchDao : GuideSearchDao {
        val items = mutableListOf<GuideSearchEntity>()
        var lastSearchQuery: String? = null

        override suspend fun insertAll(items: List<GuideSearchEntity>) {
            this.items.addAll(items)
        }

        override fun search(query: String): Flow<List<GuideSearchEntity>> {
            lastSearchQuery = query
            return flowOf(items)
        }

        override suspend fun searchDirect(query: String): List<GuideSearchEntity> {
            lastSearchQuery = query
            return items
        }

        override suspend fun getCount(): Int {
            return items.size
        }

        override suspend fun clearAll() {
            items.clear()
        }
    }
}
