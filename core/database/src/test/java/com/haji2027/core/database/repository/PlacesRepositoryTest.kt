package com.haji2027.core.database.repository

import com.haji2027.core.database.dao.PlacesDao
import com.haji2027.core.database.entity.SavedPlaceEntity
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlacesRepositoryTest {

    private lateinit var fakeDao: FakePlacesDao
    private lateinit var repository: PlacesRepository

    @Before
    fun setup() {
        fakeDao = FakePlacesDao()
        repository = DefaultPlacesRepository(fakeDao)
    }

    @Test
    fun `savePlace inserts new place and retrieves by id`() = runBlocking {
        val place = SavedPlace(
            placeId = "place_hotel_01",
            name = "Hotel Elaf Al Mashaer",
            category = PlaceCategory.HOTEL,
            latitude = 21.4200,
            longitude = 39.8250,
            accuracyMeters = 4.5f,
            notes = "Kamar 704, lantai 7",
            photoUri = "content://media/photos/1",
            createdAtEpochMs = 1715000000000L
        )

        repository.savePlace(place)

        val retrieved = repository.getPlaceById("place_hotel_01")
        assertNotNull(retrieved)
        assertEquals("Hotel Elaf Al Mashaer", retrieved!!.name)
        assertEquals(PlaceCategory.HOTEL, retrieved.category)
        assertEquals(21.4200, retrieved.latitude, 0.0001)
        assertEquals(39.8250, retrieved.longitude, 0.0001)
        assertEquals(4.5f, retrieved.accuracyMeters)
        assertEquals("Kamar 704, lantai 7", retrieved.notes)
        assertEquals("content://media/photos/1", retrieved.photoUri)
        assertEquals(1715000000000L, retrieved.createdAtEpochMs)

        assertEquals(1, repository.getPlacesCount())
    }

    @Test
    fun `observeAllPlaces sorts places by createdAtEpochMs descending`() = runBlocking {
        val placeOlder = SavedPlace(
            placeId = "place_old",
            name = "Pintu King Abdulaziz",
            category = PlaceCategory.DOOR,
            latitude = 21.4190,
            longitude = 39.8260,
            accuracyMeters = 5f,
            createdAtEpochMs = 1000L
        )
        val placeNewer = SavedPlace(
            placeId = "place_new",
            name = "Tenda Mina Maktab 42",
            category = PlaceCategory.CAMP,
            latitude = 21.4130,
            longitude = 39.8920,
            accuracyMeters = 3f,
            createdAtEpochMs = 2000L
        )

        repository.savePlace(placeOlder)
        repository.savePlace(placeNewer)

        val allPlaces = repository.observeAllPlaces().first()
        assertEquals(2, allPlaces.size)
        assertEquals("place_new", allPlaces[0].placeId)
        assertEquals("place_old", allPlaces[1].placeId)
    }

    @Test
    fun `observePlacesByCategory filters places correctly`() = runBlocking {
        repository.savePlace(
            SavedPlace(
                placeId = "door_1",
                name = "Pintu Babus Salam",
                category = PlaceCategory.DOOR,
                latitude = 21.4225,
                longitude = 39.8262,
                accuracyMeters = 4f
            )
        )
        repository.savePlace(
            SavedPlace(
                placeId = "hotel_1",
                name = "Hotel Pullman Zamzam",
                category = PlaceCategory.HOTEL,
                latitude = 21.4192,
                longitude = 39.8255,
                accuracyMeters = 3f
            )
        )
        repository.savePlace(
            SavedPlace(
                placeId = "door_2",
                name = "Pintu Al Umrah",
                category = PlaceCategory.DOOR,
                latitude = 21.4230,
                longitude = 39.8250,
                accuracyMeters = 5f
            )
        )

        val doors = repository.observePlacesByCategory(PlaceCategory.DOOR).first()
        assertEquals(2, doors.size)
        assertTrue(doors.all { it.category == PlaceCategory.DOOR })

        val hotels = repository.observePlacesByCategory(PlaceCategory.HOTEL).first()
        assertEquals(1, hotels.size)
        assertEquals("hotel_1", hotels[0].placeId)

        val camps = repository.observePlacesByCategory(PlaceCategory.CAMP).first()
        assertEquals(0, camps.size)
    }

    @Test
    fun `deletePlace removes entity`() = runBlocking {
        val place = SavedPlace(
            placeId = "bus_01",
            name = "Bus Shalawat Rute 3",
            category = PlaceCategory.BUS,
            latitude = 21.4200,
            longitude = 39.8300,
            accuracyMeters = 6f
        )
        repository.savePlace(place)
        assertEquals(1, repository.getPlacesCount())

        repository.deletePlace(place)
        assertEquals(0, repository.getPlacesCount())
        assertNull(repository.getPlaceById("bus_01"))
    }

    @Test
    fun `deletePlaceById removes target entity only`() = runBlocking {
        val p1 = SavedPlace(placeId = "p1", name = "P1", category = PlaceCategory.OTHER, latitude = 0.0, longitude = 0.0, accuracyMeters = 1f)
        val p2 = SavedPlace(placeId = "p2", name = "P2", category = PlaceCategory.OTHER, latitude = 0.0, longitude = 0.0, accuracyMeters = 1f)

        repository.savePlace(p1)
        repository.savePlace(p2)
        assertEquals(2, repository.getPlacesCount())

        repository.deletePlaceById("p1")
        assertEquals(1, repository.getPlacesCount())
        assertNull(repository.getPlaceById("p1"))
        assertNotNull(repository.getPlaceById("p2"))
    }

    @Test
    fun `deleteAllPlaces clears all places for right to be forgotten`() = runBlocking {
        repository.savePlace(SavedPlace(placeId = "p1", name = "P1", category = PlaceCategory.CAMP, latitude = 0.0, longitude = 0.0, accuracyMeters = 1f))
        repository.savePlace(SavedPlace(placeId = "p2", name = "P2", category = PlaceCategory.CAMP, latitude = 0.0, longitude = 0.0, accuracyMeters = 1f))
        assertEquals(2, repository.getPlacesCount())

        repository.deleteAllPlaces()
        assertEquals(0, repository.getPlacesCount())
        assertTrue(repository.observeAllPlaces().first().isEmpty())
    }

    @Test
    fun `entity mapping handles invalid category gracefully`() {
        val entityWithUnknownCategory = SavedPlaceEntity(
            placeId = "p_unknown",
            name = "Unknown Spot",
            category = "SUPERMARKET",
            latitude = 21.0,
            longitude = 39.0,
            accuracyMeters = 10f
        )

        val domain = entityWithUnknownCategory.toDomain()
        assertEquals(PlaceCategory.OTHER, domain.category)

        val backToEntity = SavedPlaceEntity.fromDomain(domain)
        assertEquals("OTHER", backToEntity.category)
    }

    private class FakePlacesDao : PlacesDao {
        private val state = MutableStateFlow<Map<String, SavedPlaceEntity>>(emptyMap())

        override fun getAllPlacesFlow(): Flow<List<SavedPlaceEntity>> {
            return state.map { map ->
                map.values.sortedByDescending { it.createdAtEpochMs }
            }
        }

        override suspend fun getPlaceById(placeId: String): SavedPlaceEntity? {
            return state.value[placeId]
        }

        override fun getPlaceByIdFlow(placeId: String): Flow<SavedPlaceEntity?> {
            return state.map { it[placeId] }
        }

        override fun getPlacesByCategoryFlow(category: String): Flow<List<SavedPlaceEntity>> {
            return state.map { map ->
                map.values
                    .filter { it.category == category }
                    .sortedByDescending { it.createdAtEpochMs }
            }
        }

        override suspend fun insertPlace(place: SavedPlaceEntity) {
            val current = state.value.toMutableMap()
            current[place.placeId] = place
            state.value = current
        }

        override suspend fun insertPlaces(places: List<SavedPlaceEntity>) {
            val current = state.value.toMutableMap()
            places.forEach { current[it.placeId] = it }
            state.value = current
        }

        override suspend fun updatePlace(place: SavedPlaceEntity) {
            val current = state.value.toMutableMap()
            current[place.placeId] = place
            state.value = current
        }

        override suspend fun deletePlace(place: SavedPlaceEntity) {
            val current = state.value.toMutableMap()
            current.remove(place.placeId)
            state.value = current
        }

        override suspend fun deletePlaceById(placeId: String): Int {
            val current = state.value.toMutableMap()
            val removed = current.remove(placeId)
            state.value = current
            return if (removed != null) 1 else 0
        }

        override suspend fun deleteAllPlaces() {
            state.value = emptyMap()
        }

        override suspend fun getPlacesCount(): Int {
            return state.value.size
        }
    }
}
