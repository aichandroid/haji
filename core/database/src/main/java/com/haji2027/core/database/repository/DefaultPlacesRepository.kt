package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.PlacesDao
import com.haji2027.core.database.entity.SavedPlaceEntity
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi default PlacesRepository menggunakan PlacesDao Room.
 */
class DefaultPlacesRepository(
    private val placesDao: PlacesDao
) : PlacesRepository {

    override fun observeAllPlaces(): Flow<List<SavedPlace>> {
        return placesDao.getAllPlacesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePlacesByCategory(category: PlaceCategory): Flow<List<SavedPlace>> {
        return placesDao.getPlacesByCategoryFlow(category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observePlaceById(placeId: String): Flow<SavedPlace?> {
        return placesDao.getPlaceByIdFlow(placeId).map { it?.toDomain() }
    }

    override suspend fun getPlaceById(placeId: String): SavedPlace? {
        return placesDao.getPlaceById(placeId)?.toDomain()
    }

    override suspend fun savePlace(place: SavedPlace) {
        placesDao.insertPlace(SavedPlaceEntity.fromDomain(place))
    }

    override suspend fun deletePlace(place: SavedPlace) {
        placesDao.deletePlace(SavedPlaceEntity.fromDomain(place))
    }

    override suspend fun deletePlaceById(placeId: String) {
        placesDao.deletePlaceById(placeId)
    }

    override suspend fun deleteAllPlaces() {
        placesDao.deleteAllPlaces()
    }

    override suspend fun getPlacesCount(): Int {
        return placesDao.getPlacesCount()
    }
}
