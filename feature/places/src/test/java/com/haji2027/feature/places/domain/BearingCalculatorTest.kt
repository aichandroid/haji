package com.haji2027.feature.places.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BearingCalculatorTest {

    // Koordinat Masjidil Haram / Ka'bah
    private val kaabaLat = 21.4225
    private val kaabaLng = 39.8262

    // Hotel di sebelah selatan Ka'bah (Ajyad)
    private val hotelSouthLat = 21.4190
    private val hotelSouthLng = 39.8262

    // Hotel di sebelah barat Ka'bah
    private val hotelWestLat = 21.4225
    private val hotelWestLng = 39.8200

    @Test
    fun `calculateDistanceMeters computes accurate haversine distance`() {
        val distance = BearingCalculator.calculateDistanceMeters(
            lat1 = hotelSouthLat,
            lon1 = hotelSouthLng,
            lat2 = kaabaLat,
            lon2 = kaabaLng
        )

        // Jarak ~389 meter
        assertTrue("Distance should be around 389m, was $distance", distance in 385.0..395.0)
    }

    @Test
    fun `calculateTargetBearing due north returns approximately zero degrees`() {
        val bearing = BearingCalculator.calculateTargetBearing(
            currentLat = hotelSouthLat,
            currentLng = hotelSouthLng,
            targetLat = kaabaLat,
            targetLng = kaabaLng
        )

        // Menuju tepat utara: bearing mendekati 0° atau 360°
        assertTrue("Bearing due north should be ~0°, was $bearing", bearing < 1.0f || bearing > 359.0f)
    }

    @Test
    fun `calculateTargetBearing due east returns approximately 90 degrees`() {
        val bearing = BearingCalculator.calculateTargetBearing(
            currentLat = hotelWestLat,
            currentLng = hotelWestLng,
            targetLat = kaabaLat,
            targetLng = kaabaLng
        )

        assertEquals(90.0f, bearing, 1.0f)
    }

    @Test
    fun `calculateTargetBearing due west returns approximately 270 degrees`() {
        val bearing = BearingCalculator.calculateTargetBearing(
            currentLat = kaabaLat,
            currentLng = kaabaLng,
            targetLat = hotelWestLat,
            targetLng = hotelWestLng
        )

        assertEquals(270.0f, bearing, 1.0f)
    }

    @Test
    fun `calculateTrueAzimuth compensates positive magnetic declination`() {
        val magneticAzimuth = 10.0f
        val declination = 2.8f // Makkah declination

        val trueAzimuth = BearingCalculator.calculateTrueAzimuth(magneticAzimuth, declination)
        assertEquals(12.8f, trueAzimuth, 0.01f)
    }

    @Test
    fun `calculateTrueAzimuth wraps negative values correctly`() {
        val magneticAzimuth = 1.0f
        val declination = -3.0f

        val trueAzimuth = BearingCalculator.calculateTrueAzimuth(magneticAzimuth, declination)
        assertEquals(358.0f, trueAzimuth, 0.01f)
    }

    @Test
    fun `calculateRelativeArrowAngle computes correct relative rotation`() {
        // Target berada tepat di utara (0°). Pengguna menghadap timur (90°).
        // Panah harus menunjuk ke kiri (270°).
        val relativeAngle = BearingCalculator.calculateRelativeArrowAngle(
            targetBearing = 0.0f,
            trueAzimuth = 90.0f
        )
        assertEquals(270.0f, relativeAngle, 0.01f)

        // Target berada di timur (90°). Pengguna menghadap timur (90°).
        // Panah harus menunjuk lurus ke depan (0°).
        val forwardAngle = BearingCalculator.calculateRelativeArrowAngle(
            targetBearing = 90.0f,
            trueAzimuth = 90.0f
        )
        assertEquals(0.0f, forwardAngle, 0.01f)

        // Target berada di barat (270°). Pengguna menghadap utara (0°).
        // Panah harus menunjuk ke kiri (270°).
        val leftAngle = BearingCalculator.calculateRelativeArrowAngle(
            targetBearing = 270.0f,
            trueAzimuth = 0.0f
        )
        assertEquals(270.0f, leftAngle, 0.01f)
    }

    @Test
    fun `shortestAngleDiff calculates smooth transitions across 360 degree boundary`() {
        // Dari 355° ke 5° harus berputar +10°, bukan -350°
        val forwardDiff = BearingCalculator.shortestAngleDiff(355.0f, 5.0f)
        assertEquals(10.0f, forwardDiff, 0.01f)

        // Dari 5° ke 355° harus berputar -10°, bukan +350°
        val backwardDiff = BearingCalculator.shortestAngleDiff(5.0f, 355.0f)
        assertEquals(-10.0f, backwardDiff, 0.01f)

        // Dari 10° ke 50° berputar +40°
        val normalDiff = BearingCalculator.shortestAngleDiff(10.0f, 50.0f)
        assertEquals(40.0f, normalDiff, 0.01f)
    }

    @Test
    fun `formatDistance formats meters and kilometers properly for elderly`() {
        assertEquals("450 m", BearingCalculator.formatDistance(450.0))
        assertEquals("999 m", BearingCalculator.formatDistance(999.4))
        assertEquals("1.0 km", BearingCalculator.formatDistance(1000.0))
        assertEquals("1.4 km", BearingCalculator.formatDistance(1420.0))
        assertEquals("12.5 km", BearingCalculator.formatDistance(12500.0))
    }
}
