package com.haji2027.feature.places.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassNavigationEngineTest {

    private val engine = CompassNavigationEngine()

    private val kaabaLat = 21.4225
    private val kaabaLng = 39.8262

    private val userLat = 21.4190
    private val userLng = 39.8262

    @Test
    fun `computeNavigation calculates correct bearing, relative angle, and distance`() {
        val orientation = CompassOrientation(
            magneticAzimuth = 45.0f,
            trueAzimuth = 45.0f,
            declination = 0f,
            accuracy = CompassAccuracy.HIGH
        )

        val nav = engine.computeNavigation(
            currentLat = userLat,
            currentLng = userLng,
            gpsAccuracyMeters = 5.0f,
            targetLat = kaabaLat,
            targetLng = kaabaLng,
            orientation = orientation
        )

        // Target tepat di utara (~0° / ~360°)
        // Pengguna menghadap 45° (Timur Laut)
        // Panah harus menunjuk ke kiri: (0 - 45 + 360) = 315°
        assertEquals(315.0f, nav.relativeArrowAngleDegrees, 1.0f)
        assertTrue(nav.distanceMeters in 385.0..395.0)
        assertEquals("389 m", nav.formattedDistance)
        assertEquals(NavigationAccuracyStatus.HIGH, nav.accuracyStatus)
    }

    @Test
    fun `computeNavigation assigns accuracy status according to GPS error thresholds`() {
        val baseOrientation = CompassOrientation(
            magneticAzimuth = 0f,
            trueAzimuth = 0f,
            declination = 0f,
            accuracy = CompassAccuracy.HIGH
        )

        // GPS <= 10m -> HIGH
        val highNav = engine.computeNavigation(userLat, userLng, 8.0f, kaabaLat, kaabaLng, baseOrientation)
        assertEquals(NavigationAccuracyStatus.HIGH, highNav.accuracyStatus)

        // GPS 11 - 30m -> MODERATE
        val modNav = engine.computeNavigation(userLat, userLng, 22.0f, kaabaLat, kaabaLng, baseOrientation)
        assertEquals(NavigationAccuracyStatus.MODERATE, modNav.accuracyStatus)

        // GPS > 30m -> DEGRADED
        val degNav = engine.computeNavigation(userLat, userLng, 45.0f, kaabaLat, kaabaLng, baseOrientation)
        assertEquals(NavigationAccuracyStatus.DEGRADED, degNav.accuracyStatus)
    }

    @Test
    fun `computeNavigation requires calibration when compass sensor is unreliable`() {
        val unreliableOrientation = CompassOrientation(
            magneticAzimuth = 0f,
            trueAzimuth = 0f,
            declination = 0f,
            accuracy = CompassAccuracy.UNRELIABLE
        )

        val nav = engine.computeNavigation(userLat, userLng, 5.0f, kaabaLat, kaabaLng, unreliableOrientation)
        assertEquals(NavigationAccuracyStatus.CALIBRATION_NEEDED, nav.accuracyStatus)
    }
}
