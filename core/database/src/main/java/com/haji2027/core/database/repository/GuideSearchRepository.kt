package com.haji2027.core.database.repository

import com.haji2027.core.database.entity.GuideSearchEntity
import com.haji2027.core.model.HajjContentPackage
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk pencarian teks panduan offline (FTS4).
 */
interface GuideSearchRepository {
    fun search(rawQuery: String): Flow<List<GuideSearchEntity>>
    suspend fun searchDirect(rawQuery: String): List<GuideSearchEntity>
    suspend fun populateIndex(contentPackage: HajjContentPackage)
    suspend fun isIndexPopulated(): Boolean
    suspend fun clearIndex()
}
