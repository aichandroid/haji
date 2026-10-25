package com.haji2027.feature.places.domain

/**
 * Mesin fusi navigasi yang memadukan posisi koordinat GPS terkini, lokasi target tempat tersimpan,
 * dan sensor kompas orientasi perangkat untuk menghasilkan arah panah dan estimasi jarak real-time.
 */
class CompassNavigationEngine {

    /**
     * Menghitung status navigasi komprehensif menuju target.
     *
     * @param currentLat Lintang posisi pengguna saat ini.
     * @param currentLng Bujur posisi pengguna saat ini.
     * @param gpsAccuracyMeters Estimasi galat radius GPS saat ini.
     * @param targetLat Lintang target tempat tersimpan.
     * @param targetLng Bujur target tempat tersimpan.
     * @param orientation Orientasi kompas terkini perangkat.
     */
    fun computeNavigation(
        currentLat: Double,
        currentLng: Double,
        gpsAccuracyMeters: Float,
        targetLat: Double,
        targetLng: Double,
        orientation: CompassOrientation
    ): NavigationTargetBearing {
        val distanceMeters = BearingCalculator.calculateDistanceMeters(
            lat1 = currentLat,
            lon1 = currentLng,
            lat2 = targetLat,
            lon2 = targetLng
        )

        val targetBearing = BearingCalculator.calculateTargetBearing(
            currentLat = currentLat,
            currentLng = currentLng,
            targetLat = targetLat,
            targetLng = targetLng
        )

        val relativeArrowAngle = BearingCalculator.calculateRelativeArrowAngle(
            targetBearing = targetBearing,
            trueAzimuth = orientation.trueAzimuth
        )

        val accuracyStatus = when {
            orientation.accuracy == CompassAccuracy.UNRELIABLE -> NavigationAccuracyStatus.CALIBRATION_NEEDED
            gpsAccuracyMeters <= 10f -> NavigationAccuracyStatus.HIGH
            gpsAccuracyMeters <= 30f -> NavigationAccuracyStatus.MODERATE
            else -> NavigationAccuracyStatus.DEGRADED
        }

        return NavigationTargetBearing(
            targetBearingDegrees = targetBearing,
            trueAzimuthDegrees = orientation.trueAzimuth,
            relativeArrowAngleDegrees = relativeArrowAngle,
            distanceMeters = distanceMeters,
            formattedDistance = BearingCalculator.formatDistance(distanceMeters),
            accuracyStatus = accuracyStatus
        )
    }
}
