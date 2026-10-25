package com.haji2027.feature.places.domain

/**
 * Tingkat keandalan kalibrasi sensor magnetometer perangkat.
 */
enum class CompassAccuracy {
    HIGH,
    MEDIUM,
    LOW,
    UNRELIABLE
}

/**
 * Model orientasi ponsel terhadap Kutub Magnetik dan Kutub Sejati.
 */
data class CompassOrientation(
    val magneticAzimuth: Float,
    val trueAzimuth: Float,
    val declination: Float,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val accuracy: CompassAccuracy = CompassAccuracy.HIGH,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

/**
 * Status akurasi navigasi spasial berdasarkan kualitas GPS dan sensor.
 */
enum class NavigationAccuracyStatus {
    /** Akurasi GPS <= 10m: Posisi sangat presisi untuk navigasi luar ruangan */
    HIGH,
    /** Akurasi GPS 11m - 30m: Posisi moderat (area kanopi/tenda) */
    MODERATE,
    /** Akurasi GPS > 30m: Sinyal terdegradasi (dalam gedung/basement) */
    DEGRADED,
    /** Sensor kompas terdistorsi oleh medan elektromagnetik, perlu kalibrasi angka 8 */
    CALIBRATION_NEEDED
}

/**
 * Model hasil kalkulasi navigasi menuju target fisik tersimpan.
 */
data class NavigationTargetBearing(
    val targetBearingDegrees: Float,
    val trueAzimuthDegrees: Float,
    val relativeArrowAngleDegrees: Float,
    val distanceMeters: Double,
    val formattedDistance: String,
    val accuracyStatus: NavigationAccuracyStatus
)
