package com.haji2027.feature.tracking.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovementClassifierTest {

    private lateinit var classifier: MovementClassifier

    @Before
    fun setup() {
        classifier = MovementClassifier()
    }

    @Test
    fun `initial location sample starts in MOVING mode with 30s interval`() {
        val eval = classifier.onLocationSample(
            latitude = 21.4225,
            longitude = 39.8262,
            speedMps = 0.0f,
            timestampMs = 1000L
        )

        assertEquals(MovementMode.MOVING, eval.mode)
        assertEquals(30_000L, eval.intervalMs)
        assertEquals(10f, eval.minDistanceMeters, 0.01f)
        assertEquals(0L, eval.stationaryDurationMs)
        assertEquals(MovementMode.MOVING, classifier.getCurrentMode())
    }

    @Test
    fun `moving faster than 0_5 mps keeps MOVING mode and resets stationary duration`() {
        // Initial sample di t=1000ms
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 1000L)

        // Sample kedua di t=60_000ms dengan kecepatan jalan 1.2 m/s
        val eval = classifier.onLocationSample(
            latitude = 21.4226,
            longitude = 39.8263,
            speedMps = 1.2f,
            timestampMs = 60_000L
        )

        assertEquals(MovementMode.MOVING, eval.mode)
        assertEquals(30_000L, eval.intervalMs)
        assertEquals(0L, eval.stationaryDurationMs)
        assertEquals(MovementMode.MOVING, classifier.getCurrentMode())
    }

    @Test
    fun `stationary for under 3 minutes remains in MOVING mode during grace period`() {
        // Initial sample di t=0s
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 0L)

        // Sample kedua setelah 2 menit (120.000 ms) di tempat yang sama
        val eval = classifier.onLocationSample(
            latitude = 21.4225,
            longitude = 39.8262,
            speedMps = 0.1f,
            timestampMs = 120_000L
        )

        // Tetap MOVING karena belum melewati ambang 3 menit (180.000 ms)
        assertEquals(MovementMode.MOVING, eval.mode)
        assertEquals(30_000L, eval.intervalMs)
        assertEquals(120_000L, eval.stationaryDurationMs)
    }

    @Test
    fun `stationary for 3 minutes or more switches to STATIONARY mode with 300s interval`() {
        // Initial sample di t=0s
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 0L)

        // Sample kedua tepat di t=180.000ms (3 menit)
        val eval = classifier.onLocationSample(
            latitude = 21.4225,
            longitude = 39.8262,
            speedMps = 0.0f,
            timestampMs = 180_000L
        )

        // Harus beralih ke STATIONARY (5 menit / 300.000 ms interval)
        assertEquals(MovementMode.STATIONARY, eval.mode)
        assertEquals(300_000L, eval.intervalMs)
        assertEquals(0f, eval.minDistanceMeters, 0.01f)
        assertEquals(180_000L, eval.stationaryDurationMs)
        assertEquals(MovementMode.STATIONARY, classifier.getCurrentMode())
    }

    @Test
    fun `resuming movement immediately switches from STATIONARY back to MOVING mode`() {
        // Masuk ke mode STATIONARY
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 0L)
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 180_000L)
        assertEquals(MovementMode.STATIONARY, classifier.getCurrentMode())

        // Jemaah mulai berjalan kaki (kecepatan 1.0 m/s dan berpindah > 10m)
        val eval = classifier.onLocationSample(
            latitude = 21.4235, // Geser ~110 meter ke utara
            longitude = 39.8262,
            speedMps = 1.0f,
            timestampMs = 210_000L
        )

        assertEquals(MovementMode.MOVING, eval.mode)
        assertEquals(30_000L, eval.intervalMs)
        assertEquals(0L, eval.stationaryDurationMs)
        assertEquals(MovementMode.MOVING, classifier.getCurrentMode())
    }

    @Test
    fun `reset clears state back to initial values`() {
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 0L)
        classifier.onLocationSample(21.4225, 39.8262, 0.0f, 200_000L)
        assertEquals(MovementMode.STATIONARY, classifier.getCurrentMode())

        classifier.reset()
        assertEquals(MovementMode.MOVING, classifier.getCurrentMode())
    }
}
