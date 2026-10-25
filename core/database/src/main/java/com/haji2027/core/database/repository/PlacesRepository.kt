package com.haji2027.core.database.repository

import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk mengelola tempat tersimpan (Pintu Masjid, Hotel, Tenda, Bus, Lainnya).
 * Beroperasi 100% offline secara lokal.
 */
interface PlacesRepository {
    fun observeAllPlaces(): Flow<List<SavedPlace>>
    fun observePlacesByCategory(category: PlaceCategory): Flow<List<SavedPlace>>
    fun observePlaceById(placeId: String): Flow<SavedPlace?>
    suspend fun getPlaceById(placeId: String): SavedPlace?
    suspend fun savePlace(place: SavedPlace)
    suspend fun deletePlace(place: SavedPlace)
    suspend fun deletePlaceById(placeId: String)
    suspend fun deleteAllPlaces()
    suspend fun getPlacesCount(): Int
}
