package com.haji2027.feature.places.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GpsAveragingEngineTest {

    @Test
    fun `calculateAveragedLocation with 5 samples calculates correct median and average accuracy`() {
        val engine = GpsAveragingEngine()

        // 5 sampel koordinat acak di sekitar Masjidil Haram
        val samples = listOf(
            GpsSample(latitude = 21.4201, longitude = 39.8255, accuracyMeters = 3.0f),
            GpsSample(latitude = 21.4205, longitude = 39.8251, accuracyMeters = 4.0f),
            GpsSample(latitude = 21.4203, longitude = 39.8253, accuracyMeters = 5.0f),
            GpsSample(latitude = 21.4202, longitude = 39.8254, accuracyMeters = 3.5f),
            GpsSample(latitude = 21.4204, longitude = 39.8252, accuracyMeters = 4.5f)
        )

        val result = engine.calculateAveragedLocation(samples)

        assertNotNull(result)
        // Sorted latitudes: 21.4201, 21.4202, 21.4203, 21.4204, 21.4205 -> Median = 21.4203
        assertEquals(21.4203, result!!.latitude, 0.00001)

        // Sorted longitudes: 39.8251, 39.8252, 39.8253, 39.8254, 39.8255 -> Median = 39.8253
        assertEquals(39.8253, result.longitude, 0.00001)

        // Average accuracy: (3.0 + 4.0 + 5.0 + 3.5 + 4.5) / 5 = 4.0f
        assertEquals(4.0f, result.accuracyMeters, 0.01f)
        assertEquals(5, result.validSampleCount)
        assertEquals(5, result.totalSampleCount)
    }

    @Test
    fun `calculateAveragedLocation with even sample size averages middle two values`() {
        val engine = GpsAveragingEngine()

        val samples = listOf(
            GpsSample(latitude = 21.4200, longitude = 39.8250, accuracyMeters = 4.0f),
            GpsSample(latitude = 21.4202, longitude = 39.8252, accuracyMeters = 4.0f),
            GpsSample(latitude = 21.4204, longitude = 39.8254, accuracyMeters = 4.0f),
            GpsSample(latitude = 21.4206, longitude = 39.8256, accuracyMeters = 4.0f)
        )

        val result = engine.calculateAveragedLocation(samples)
        assertNotNull(result)
        // Middle two: 21.4202 & 21.4204 -> Avg = 21.4203
        assertEquals(21.4203, result!!.latitude, 0.00001)
        // Middle two: 39.8252 & 39.8254 -> Avg = 39.8253
        assertEquals(39.8253, result.longitude, 0.00001)
        assertEquals(4, result.validSampleCount)
    }

    @Test
    fun `calculateAveragedLocation filters out noisy samples with accuracy greater than 25m`() {
        val engine = GpsAveragingEngine()

        val samples = listOf(
            GpsSample(latitude = 21.4201, longitude = 39.8251, accuracyMeters = 5.0f),
            GpsSample(latitude = 21.4202, longitude = 39.8252, accuracyMeters = 6.0f),
            GpsSample(latitude = 21.4203, longitude = 39.8253, accuracyMeters = 7.0f),
            GpsSample(latitude = 21.4500, longitude = 39.8900, accuracyMeters = 35.0f), // Outlier terfilter
            GpsSample(latitude = 21.4900, longitude = 39.9100, accuracyMeters = 80.0f)  // Outlier terfilter
        )

        val result = engine.calculateAveragedLocation(samples, maxAccuracyMeters = 25f)

        assertNotNull(result)
        assertEquals(3, result!!.validSampleCount)
        assertEquals(5, result.totalSampleCount)
        assertEquals(21.4202, result.latitude, 0.00001)
        assertEquals(39.8252, result.longitude, 0.00001)
        assertEquals(6.0f, result.accuracyMeters, 0.01f)
    }

    @Test
    fun `calculateAveragedLocation returns null when valid samples are less than minimum required`() {
        val engine = GpsAveragingEngine()

        val samples = listOf(
            GpsSample(latitude = 21.4201, longitude = 39.8251, accuracyMeters = 5.0f),
            GpsSample(latitude = 21.4202, longitude = 39.8252, accuracyMeters = 6.0f),
            GpsSample(latitude = 21.4500, longitude = 39.8900, accuracyMeters = 30.0f),
            GpsSample(latitude = 21.4600, longitude = 39.8900, accuracyMeters = 40.0f)
        )

        // Hanya 2 sampel valid, sedangkan batas minimum adalah 3
        val result = engine.calculateAveragedLocation(samples, maxAccuracyMeters = 25f, minValidSamples = 3)
        assertNull(result)
    }

    @Test
    fun `startSampling flow emits Sampling progress and Success when target samples reached`() = runBlocking {
        val testSamples = listOf(
            GpsSample(21.4201, 39.8251, 4f),
            GpsSample(21.4202, 39.8252, 5f),
            GpsSample(21.4203, 39.8253, 4.5f),
            GpsSample(21.4204, 39.8254, 5.5f),
            GpsSample(21.4205, 39.8255, 6f)
        )

        val fakeProvider = FakeLocationSampleProvider(testSamples)
        val engine = GpsAveragingEngine(fakeProvider)

        val states = engine.startSampling(targetSamples = 5, intervalMs = 10L, timeoutMs = 2000L).toList()

        assertTrue(states.first() is GpsSamplingState.Sampling)
        val lastState = states.last()
        assertTrue("Expected last state to be Success, but got: $lastState", lastState is GpsSamplingState.Success)

        val success = lastState as GpsSamplingState.Success
        assertEquals(21.4203, success.location.latitude, 0.0001)
        assertEquals(39.8253, success.location.longitude, 0.0001)
        assertEquals(5, success.location.validSampleCount)
    }

    @Test
    fun `startSampling flow emits POOR_ACCURACY_ALL_FILTERED when all samples exceed threshold`() = runBlocking {
        val noisySamples = listOf(
            GpsSample(21.4201, 39.8251, 35f),
            GpsSample(21.4202, 39.8252, 40f),
            GpsSample(21.4203, 39.8253, 50f)
        )

        val fakeProvider = FakeLocationSampleProvider(noisySamples)
        val engine = GpsAveragingEngine(fakeProvider)

        val states = engine.startSampling(targetSamples = 3, intervalMs = 10L, timeoutMs = 2000L, maxAccuracyMeters = 25f).toList()

        val lastState = states.last()
        assertTrue(lastState is GpsSamplingState.Error)
        val error = lastState as GpsSamplingState.Error
        assertEquals(GpsSamplingError.POOR_ACCURACY_ALL_FILTERED, error.reason)
    }

    @Test
    fun `startSampling flow emits TIMEOUT_INSUFFICIENT_SAMPLES when timeout occurs`() = runBlocking {
        // Provider yang hanya memancarkan 1 sampel lalu berhenti
        val insufficientSamples = listOf(
            GpsSample(21.4201, 39.8251, 4f)
        )

        val fakeProvider = FakeLocationSampleProvider(insufficientSamples)
        val engine = GpsAveragingEngine(fakeProvider)

        val states = engine.startSampling(targetSamples = 5, intervalMs = 10L, timeoutMs = 100L, minValidSamples = 3).toList()

        val lastState = states.last()
        assertTrue(lastState is GpsSamplingState.Error)
        val error = lastState as GpsSamplingState.Error
        assertEquals(GpsSamplingError.TIMEOUT_INSUFFICIENT_SAMPLES, error.reason)
    }

    @Test
    fun `startSampling flow emits PERMISSION_DENIED on SecurityException`() = runBlocking {
        val errorProvider = object : LocationSampleProvider {
            override fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample> = flow {
                throw SecurityException("ACCESS_FINE_LOCATION permission not granted")
            }
        }

        val engine = GpsAveragingEngine(errorProvider)
        val states = engine.startSampling().toList()

        val lastState = states.last()
        assertTrue(lastState is GpsSamplingState.Error)
        assertEquals(GpsSamplingError.PERMISSION_DENIED, (lastState as GpsSamplingState.Error).reason)
    }

    @Test
    fun `startSampling flow emits GPS_DISABLED when provider is null`() = runBlocking {
        val engine = GpsAveragingEngine(sampleProvider = null)
        val states = engine.startSampling().toList()

        val lastState = states.last()
        assertTrue(lastState is GpsSamplingState.Error)
        assertEquals(GpsSamplingError.GPS_DISABLED, (lastState as GpsSamplingState.Error).reason)
    }

    private class FakeLocationSampleProvider(
        private val samples: List<GpsSample>
    ) : LocationSampleProvider {
        override fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample> {
            return samples.asFlow()
        }
    }
}
