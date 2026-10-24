package com.haji2027.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haji2027.core.database.entity.GuideSearchEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk pencarian teks penuh (FTS4) pada panduan manasik haji.
 */
@Dao
interface GuideSearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GuideSearchEntity>)

    @Query("SELECT * FROM guide_search_fts WHERE guide_search_fts MATCH :query")
    fun search(query: String): Flow<List<GuideSearchEntity>>

    @Query("SELECT * FROM guide_search_fts WHERE guide_search_fts MATCH :query")
    suspend fun searchDirect(query: String): List<GuideSearchEntity>

    @Query("SELECT COUNT(*) FROM guide_search_fts")
    suspend fun getCount(): Int

    @Query("DELETE FROM guide_search_fts")
    suspend fun clearAll()
}
