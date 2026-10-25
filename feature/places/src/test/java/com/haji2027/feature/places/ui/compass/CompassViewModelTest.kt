package com.haji2027.feature.places.ui.compass

import com.haji2027.core.database.repository.PlacesRepository
import com.haji2027.core.model.PlaceCategory
import com.haji2027.core.model.SavedPlace
import com.haji2027.feature.places.domain.CompassAccuracy
import com.haji2027.feature.places.domain.CompassNavigationEngine
import com.haji2027.feature.places.domain.CompassOrientation
import com.haji2027.feature.places.domain.GpsSample
import com.haji2027.feature.places.domain.LocationSampleProvider
import com.haji2027.feature.places.domain.OrientationSensorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class CompassViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var fakeRepository: FakePlacesRepository
    private lateinit var fakeLocationProvider: FakeLocationProvider
    private lateinit var fakeSensorProvider: FakeSensorProvider
    private val navigationEngine = CompassNavigationEngine()

    private val targetPlace = SavedPlace(
        placeId = "place_target_01",
        name = "Pintu Ismail Masjidil Haram",
        category = PlaceCategory.DOOR,
        latitude = 21.4225,
        longitude = 39.8262,
        accuracyMeters = 4f,
        notes = "Pintu nomor 4",
        createdAtEpochMs = 1000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakePlacesRepository(listOf(targetPlace))
        fakeLocationProvider = FakeLocationProvider()
        fakeSensorProvider = FakeSensorProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads target place successfully`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.targetPlace)
        assertEquals("Pintu Ismail Masjidil Haram", state.targetPlace!!.name)
        assertTrue(state.isWaitingForGps)
        assertNull(state.navigationBearing)
    }

    @Test
    fun `navigation bearing updates when both GPS and orientation are emitted`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Pengguna 390m di selatan target, menghadap utara (azimuth 0°)
        fakeLocationProvider.emitSample(
            GpsSample(latitude = 21.4190, longitude = 39.8262, accuracyMeters = 5f)
        )
        fakeSensorProvider.emitOrientation(
            CompassOrientation(magneticAzimuth = 0f, trueAzimuth = 0f, declination = 0f, accuracy = CompassAccuracy.HIGH)
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isWaitingForGps)
        assertNotNull(state.navigationBearing)
        val bearing = state.navigationBearing!!

        // Panah harus menunjuk ke lurus depan (~0° / ~360°)
        assertEquals(0f, bearing.relativeArrowAngleDegrees, 1.0f)
        assertTrue(bearing.distanceMeters > 380 && bearing.distanceMeters < 400)
    }

    @Test
    fun `unreliable compass orientation automatically triggers calibration dialog`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showCalibrationDialog)

        fakeSensorProvider.emitOrientation(
            CompassOrientation(
                magneticAzimuth = 100f,
                trueAzimuth = 100f,
                declination = 0f,
                accuracy = CompassAccuracy.UNRELIABLE
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showCalibrationDialog)

        viewModel.dismissCalibrationDialog()
        assertFalse(viewModel.uiState.value.showCalibrationDialog)
    }

    @Test
    fun `manual open and dismiss calibration dialog updates state correctly`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showCalibrationDialog)

        viewModel.openCalibrationDialog()
        assertTrue(viewModel.uiState.value.showCalibrationDialog)

        viewModel.dismissCalibrationDialog()
        assertFalse(viewModel.uiState.value.showCalibrationDialog)
    }

    @Test
    fun `open and dismiss delete dialog updates state correctly`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.openDeleteDialog()
        assertTrue(viewModel.uiState.value.showDeleteConfirmDialog)

        viewModel.dismissDeleteDialog()
        assertFalse(viewModel.uiState.value.showDeleteConfirmDialog)
    }

    @Test
    fun `confirmDeletePlace removes place and sets isTargetDeleted flag`() = testScope.runTest {
        val viewModel = CompassViewModel(
            targetPlaceId = targetPlace.placeId,
            placesRepository = fakeRepository,
            navigationEngine = navigationEngine,
            locationProvider = fakeLocationProvider,
            sensorProvider = fakeSensorProvider
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(fakeRepository.getPlaceById(targetPlace.placeId))

        viewModel.openDeleteDialog()
        viewModel.confirmDeletePlace()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showDeleteConfirmDialog)
        assertTrue(state.isTargetDeleted)
        assertNull(fakeRepository.getPlaceById(targetPlace.placeId))
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

    private class FakeSensorProvider : OrientationSensorProvider {
        private val _orientationFlow = MutableStateFlow<CompassOrientation?>(null)

        fun emitOrientation(orientation: CompassOrientation) {
            _orientationFlow.value = orientation
        }

        override fun observeOrientation(): Flow<CompassOrientation> =
            _orientationFlow.filterNotNull()
    }
}
