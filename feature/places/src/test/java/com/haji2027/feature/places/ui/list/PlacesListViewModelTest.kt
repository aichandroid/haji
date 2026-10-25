package com.haji2027.feature.places.ui.list

import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.AveragedLocation
import com.haji2027.feature.places.domain.GpsAveragingEngine
import com.haji2027.feature.places.domain.GpsSample
import com.haji2027.feature.places.domain.LocationSampleProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakePlacesRepository
    private lateinit var fakeLocationProvider: FakeLocationProvider

    private val samplePlaces = listOf(
        SavedPlace(
            placeId = "p1",
            name = "Pintu King Abdulaziz",
            category = PlaceCategory.DOOR,
            latitude = 21.4190,
            longitude = 39.8260,
            accuracyMeters = 4f,
            notes = "Dekat tower jam",
            createdAtEpochMs = 1000L
        ),
        SavedPlace(
            placeId = "p2",
            name = "Hotel Elaf",
            category = PlaceCategory.HOTEL,
            latitude = 21.4200,
            longitude = 39.8270,
            accuracyMeters = 5f,
            notes = "Kamar 302",
            createdAtEpochMs = 2000L
        ),
        SavedPlace(
            placeId = "p3",
            name = "Tenda Mina Maktab 112",
            category = PlaceCategory.CAMP,
            latitude = 21.4150,
            longitude = 39.8800,
            accuracyMeters = 6f,
            notes = null,
            createdAtEpochMs = 3000L
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePlacesRepository(samplePlaces)
        fakeLocationProvider = FakeLocationProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load populates all places and clears loading state`() = testScope.runTest {
        val averagingEngine = GpsAveragingEngine(fakeLocationProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.places.size)
        assertNull(state.selectedCategory)
    }

    @Test
    fun `filter places by category returns only matched places`() = testScope.runTest {
        val averagingEngine = GpsAveragingEngine(fakeLocationProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        // Filter DOOR
        viewModel.setCategoryFilter(PlaceCategory.DOOR)
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(PlaceCategory.DOOR, state.selectedCategory)
        assertEquals(1, state.places.size)
        assertEquals("Pintu King Abdulaziz", state.places.first().name)

        // Filter CAMP
        viewModel.setCategoryFilter(PlaceCategory.CAMP)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(PlaceCategory.CAMP, state.selectedCategory)
        assertEquals(1, state.places.size)
        assertEquals("Tenda Mina Maktab 112", state.places.first().name)

        // Clear filter (null)
        viewModel.setCategoryFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertNull(state.selectedCategory)
        assertEquals(3, state.places.size)
    }

    @Test
    fun `openAddPlaceDialog triggers sampling and updates state on success`() = testScope.runTest {
        val accurateSamples = listOf(
            GpsSample(21.4201, 39.8251, 3f),
            GpsSample(21.4202, 39.8252, 4f),
            GpsSample(21.4203, 39.8253, 3.5f),
            GpsSample(21.4204, 39.8254, 4.5f),
            GpsSample(21.4205, 39.8255, 3.8f)
        )
        val fixedSamplesProvider = object : LocationSampleProvider {
            override fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample> = accurateSamples.asFlow()
        }
        val averagingEngine = GpsAveragingEngine(fixedSamplesProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddPlaceDialog()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showAddDialog)
        assertFalse(state.isSamplingGps)
        assertNotNull(state.sampledLocation)
        assertEquals(21.4203, state.sampledLocation!!.latitude, 0.0001)
        assertEquals(39.8253, state.sampledLocation!!.longitude, 0.0001)
    }

    @Test
    fun `saveNewPlace saves entity into repository and closes dialog`() = testScope.runTest {
        val averagingEngine = GpsAveragingEngine(fakeLocationProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddPlaceDialog()
        val loc = AveragedLocation(
            latitude = 21.4250,
            longitude = 39.8300,
            accuracyMeters = 3.5f,
            validSampleCount = 5,
            totalSampleCount = 5
        )

        viewModel.saveNewPlace(
            name = "Stasiun Kereta Cepat Haramain",
            category = PlaceCategory.BUS,
            notes = "Gate 3",
            location = loc
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showAddDialog)
        assertEquals(4, state.places.size)

        val savedPlace = fakeRepository.getPlaceById(state.places.last().placeId)
        assertNotNull(savedPlace)
        assertEquals("Stasiun Kereta Cepat Haramain", savedPlace!!.name)
        assertEquals(PlaceCategory.BUS, savedPlace.category)
        assertEquals(21.4250, savedPlace.latitude, 0.0001)
    }

    @Test
    fun `deletePlace removes item from repository and updates list`() = testScope.runTest {
        val averagingEngine = GpsAveragingEngine(fakeLocationProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.places.size)

        viewModel.deletePlace("p2")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.places.size)
        assertTrue(state.places.none { it.placeId == "p2" })
    }

    @Test
    fun `closeAddPlaceDialog cancels dialog and resets sampling state`() = testScope.runTest {
        val averagingEngine = GpsAveragingEngine(fakeLocationProvider)
        val viewModel = PlacesListViewModel(fakeRepository, averagingEngine, fakeLocationProvider)

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.openAddPlaceDialog()
        assertTrue(viewModel.uiState.value.showAddDialog)

        viewModel.closeAddPlaceDialog()
        val state = viewModel.uiState.value
        assertFalse(state.showAddDialog)
        assertFalse(state.isSamplingGps)
        assertNull(state.sampledLocation)
    }

    private class FakePlacesRepository(
        initialPlaces: List<SavedPlace> = emptyList()
    ) : PlacesRepository {
        private val _places = MutableStateFlow(initialPlaces)

        override fun observeAllPlaces(): Flow<List<SavedPlace>> = _places

        override fun observePlacesByCategory(category: PlaceCategory): Flow<List<SavedPlace>> =
            _places.map { list -> list.filter { it.category == category } }

        override fun observePlaceById(placeId: String): Flow<SavedPlace?> =
            _places.map { list -> list.find { it.placeId == placeId } }

        override suspend fun getPlaceById(placeId: String): SavedPlace? =
            _places.value.find { it.placeId == placeId }

        override suspend fun savePlace(place: SavedPlace) {
            _places.value = _places.value.filter { it.placeId != place.placeId } + place
        }

        override suspend fun deletePlace(place: SavedPlace) {
            _places.value = _places.value.filter { it.placeId != place.placeId }
        }

        override suspend fun deletePlaceById(placeId: String) {
            _places.value = _places.value.filter { it.placeId != placeId }
        }

        override suspend fun deleteAllPlaces() {
            _places.value = emptyList()
        }

        override suspend fun getPlacesCount(): Int = _places.value.size
    }

    private class FakeLocationProvider : LocationSampleProvider {
        private val _sampleFlow = MutableStateFlow<GpsSample?>(null)

        fun emitSample(sample: GpsSample) {
            _sampleFlow.value = sample
        }

        override fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample> =
            _sampleFlow.filterNotNull()
    }
}
