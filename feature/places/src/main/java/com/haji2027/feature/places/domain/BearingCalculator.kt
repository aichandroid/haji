package com.haji2027.feature.places.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilitas komputasi trigonometri bola besar (Great Circle) dan navigasi arah panah.
 * Beroperasi murni matematis dan independen dari framework Android.
 */
object BearingCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    /**
     * Menghitung jarak garis lurus antara dua titik koordinat menggunakan formula Haversine.
     *
     * @return Jarak dalam satuan meter.
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
                cos(rLat1) * cos(rLat2) *
                sin(dLon / 2).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Menghitung arah bantalan awal (Initial Great Circle Bearing, $T_b$) dari posisi saat ini menuju target.
     *
     * @return Derajat sudut dalam rentang [0.0, 360.0). 0° = Utara, 90° = Timur, 180° = Selatan, 270° = Barat.
     */
    fun calculateTargetBearing(
        currentLat: Double,
        currentLng: Double,
        targetLat: Double,
        targetLng: Double
    ): Float {
        val lat1 = Math.toRadians(currentLat)
        val lat2 = Math.toRadians(targetLat)
        val dLng = Math.toRadians(targetLng - currentLng)

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) -
                sin(lat1) * cos(lat2) * cos(dLng)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad).toFloat()
        return normalizeDegrees(bearingDeg)
    }

    /**
     * Menghitung True Azimuth ($A_t$) dengan mengompensasikan deklinasi magnetik lokal ($D$).
     * $$A_t = (A_m + D + 360^\circ) \pmod{360^\circ}$$
     */
    fun calculateTrueAzimuth(
        magneticAzimuth: Float,
        declinationDegrees: Float
    ): Float {
        return normalizeDegrees(magneticAzimuth + declinationDegrees)
    }

    /**
     * Menghitung sudut rotasi panah antarmuka ($\theta$) relatif terhadap bagian atas smartphone:
     * $$\theta = (T_b - A_t + 360^\circ) \pmod{360^\circ}$$
     *
     * Nilai 0° berarti target berada tepat lurus di depan arah pandang pengguna.
     */
    fun calculateRelativeArrowAngle(
        targetBearing: Float,
        trueAzimuth: Float
    ): Float {
        return normalizeDegrees(targetBearing - trueAzimuth)
    }

    /**
     * Menormalkan derajat agar selalu berada di dalam rentang tertutup [0.0, 360.0).
     */
    fun normalizeDegrees(degrees: Float): Float {
        val normalized = degrees % 360f
        return if (normalized < 0f) normalized + 360f else normalized
    }

    /**
     * Menghitung selisih sudut terpendek antara dua sudut dalam lingkaran [-180.0, 180.0].
     * Digunakan untuk interpolasi / smoothing perputaran jarum panah tanpa melompat liar di perbatasan 360°.
     */
    fun shortestAngleDiff(fromAngle: Float, toAngle: Float): Float {
        val diff = (toAngle - fromAngle) % 360f
        return when {
            diff > 180f -> diff - 360f
            diff < -180f -> diff + 360f
            else -> diff
        }
    }

    /**
     * Memformat jarak dalam meter ke format teks ramah lansia yang mudah dipahami sekilas.
     */
    fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters < 1000.0) {
            "${distanceMeters.toInt()} m"
        } else {
            val km = distanceMeters / 1000.0
            String.format(java.util.Locale.US, "%.1f km", km)
        }
    }
}
