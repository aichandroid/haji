package com.haji2027.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.haji2027.core.database.entity.SavedPlaceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk tabel `saved_places`.
 * Mengelola operasi basis data tempat tersimpan (Pintu Masjid, Hotel, Tenda Mina/Arafah, Bus).
 */
@Dao
interface PlacesDao {

    @Query("SELECT * FROM saved_places ORDER BY created_at_epoch_ms DESC")
    fun getAllPlacesFlow(): Flow<List<SavedPlaceEntity>>

    @Query("SELECT * FROM saved_places WHERE place_id = :placeId LIMIT 1")
    suspend fun getPlaceById(placeId: String): SavedPlaceEntity?

    @Query("SELECT * FROM saved_places WHERE place_id = :placeId LIMIT 1")
    fun getPlaceByIdFlow(placeId: String): Flow<SavedPlaceEntity?>

    @Query("SELECT * FROM saved_places WHERE category = :category ORDER BY created_at_epoch_ms DESC")
    fun getPlacesByCategoryFlow(category: String): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: SavedPlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<SavedPlaceEntity>)

    @Update
    suspend fun updatePlace(place: SavedPlaceEntity)

    @Delete
    suspend fun deletePlace(place: SavedPlaceEntity)

    @Query("DELETE FROM saved_places WHERE place_id = :placeId")
    suspend fun deletePlaceById(placeId: String): Int

    @Query("DELETE FROM saved_places")
    suspend fun deleteAllPlaces()

    @Query("SELECT COUNT(*) FROM saved_places")
    suspend fun getPlacesCount(): Int
}
