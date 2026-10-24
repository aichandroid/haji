package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.GuideSearchDao
import com.haji2027.core.database.entity.GuideSearchEntity
import com.haji2027.core.database.search.FtsQueryUtils
import com.haji2027.core.database.search.HajjSearchIndexer
import com.haji2027.core.model.HajjContentPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implementasi default GuideSearchRepository menggunakan GuideSearchDao FTS4 Room.
 */
class DefaultGuideSearchRepository(
    private val guideSearchDao: GuideSearchDao
) : GuideSearchRepository {

    override fun search(rawQuery: String): Flow<List<GuideSearchEntity>> {
        val sanitized = FtsQueryUtils.sanitizeFtsQuery(rawQuery)
        if (sanitized.isBlank()) return flowOf(emptyList())
        return guideSearchDao.search(sanitized)
    }

    override suspend fun searchDirect(rawQuery: String): List<GuideSearchEntity> {
        val sanitized = FtsQueryUtils.sanitizeFtsQuery(rawQuery)
        if (sanitized.isBlank()) return emptyList()
        return guideSearchDao.searchDirect(sanitized)
    }

    override suspend fun populateIndex(contentPackage: HajjContentPackage) {
        val indexItems = HajjSearchIndexer.buildIndex(contentPackage)
        guideSearchDao.clearAll()
        guideSearchDao.insertAll(indexItems)
    }

    override suspend fun isIndexPopulated(): Boolean {
        return guideSearchDao.getCount() > 0
    }

    override suspend fun clearIndex() {
        guideSearchDao.clearAll()
    }
}
