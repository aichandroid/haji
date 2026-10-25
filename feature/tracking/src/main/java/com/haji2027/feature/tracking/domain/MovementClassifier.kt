package com.haji2027.feature.tracking.domain

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Status mobilitas fisik pengguna untuk penyesuaian interval telemetri GPS secara adaptif.
 * Selaras dengan FR-TRACK-005 dan Bagian 17.5.3 SYSTEM_DESIGN_DOCUMENT.md.
 */
enum class MovementMode {
    MOVING,
    STATIONARY
}

/**
 * Hasil evaluasi mode mobilitas dan parameter interval yang direkomendasikan.
 */
data class MovementEvaluation(
    val mode: MovementMode,
    val intervalMs: Long,
    val minDistanceMeters: Float,
    val stationaryDurationMs: Long
)

/**
 * Mesin klasifikasi pergerakan adaptif untuk penghematan daya baterai (NFR-BAT-001).
 *
 * - Mode Bergerak (kecepatan > 0.5 m/s): Interval 30 detik, displacement minimal 10 meter.
 * - Mode Diam (kecepatan <= 0.5 m/s selama > 3 menit): Interval diperpanjang menjadi 5 menit (300 detik).
 */
class MovementClassifier(
    val movingIntervalMs: Long = DEFAULT_MOVING_INTERVAL_MS,
    val stationaryIntervalMs: Long = DEFAULT_STATIONARY_INTERVAL_MS,
    val speedThresholdMps: Float = DEFAULT_SPEED_THRESHOLD_MPS,
    val stationaryThresholdMs: Long = DEFAULT_STATIONARY_THRESHOLD_MS,
    val minDisplacementMeters: Float = DEFAULT_MIN_DISPLACEMENT_METERS
) {
    companion object {
        const val DEFAULT_MOVING_INTERVAL_MS = 30_000L // 30 detik
        const val DEFAULT_STATIONARY_INTERVAL_MS = 300_000L // 5 menit (300 detik)
        const val DEFAULT_SPEED_THRESHOLD_MPS = 0.5f // 0.5 m/s (~1.8 km/jam jalan santai)
        const val DEFAULT_STATIONARY_THRESHOLD_MS = 180_000L // 3 menit diam berturut-turut
        const val DEFAULT_MIN_DISPLACEMENT_METERS = 10f // 10 meter
    }

    private var lastMovingTimestampMs: Long = 0L
    private var lastRecordedLat: Double? = null
    private var lastRecordedLng: Double? = null
    private var currentMode: MovementMode = MovementMode.MOVING

    /**
     * Memproses sampel lokasi baru dan mengevaluasi mode pergerakan terkini.
     *
     * @param latitude Lintang perangkat
     * @param longitude Bujur perangkat
     * @param speedMps Kecepatan perangkat dalam meter/detik (opsional, jika negatif diabaikan)
     * @param timestampMs Waktu pencatatan sampel dalam epoch milliseconds
     */
    fun onLocationSample(
        latitude: Double,
        longitude: Double,
        speedMps: Float?,
        timestampMs: Long
    ): MovementEvaluation {
        if (lastMovingTimestampMs == 0L) {
            lastMovingTimestampMs = timestampMs
            lastRecordedLat = latitude
            lastRecordedLng = longitude
            currentMode = MovementMode.MOVING
            return MovementEvaluation(
                mode = MovementMode.MOVING,
                intervalMs = movingIntervalMs,
                minDistanceMeters = minDisplacementMeters,
                stationaryDurationMs = 0L
            )
        }

        val distanceMoved = calculateDistanceMeters(
            lat1 = lastRecordedLat ?: latitude,
            lon1 = lastRecordedLng ?: longitude,
            lat2 = latitude,
            lon2 = longitude
        )

        val isMovingBySpeed = speedMps != null && speedMps > speedThresholdMps
        val isMovingByDistance = distanceMoved > minDisplacementMeters

        if (isMovingBySpeed || isMovingByDistance) {
            // Perangkat aktif bergerak
            lastMovingTimestampMs = timestampMs
            lastRecordedLat = latitude
            lastRecordedLng = longitude
            currentMode = MovementMode.MOVING

            return MovementEvaluation(
                mode = MovementMode.MOVING,
                intervalMs = movingIntervalMs,
                minDistanceMeters = minDisplacementMeters,
                stationaryDurationMs = 0L
            )
        } else {
            // Perangkat berada di radius yang sama
            val stationaryDuration = timestampMs - lastMovingTimestampMs

            if (stationaryDuration >= stationaryThresholdMs) {
                currentMode = MovementMode.STATIONARY
                return MovementEvaluation(
                    mode = MovementMode.STATIONARY,
                    intervalMs = stationaryIntervalMs,
                    minDistanceMeters = 0f,
                    stationaryDurationMs = stationaryDuration
                )
            } else {
                // Masih dalam masa toleransi 3 menit sebelum masuk mode istirahat
                return MovementEvaluation(
                    mode = MovementMode.MOVING,
                    intervalMs = movingIntervalMs,
                    minDistanceMeters = minDisplacementMeters,
                    stationaryDurationMs = stationaryDuration
                )
            }
        }
    }

    fun getCurrentMode(): MovementMode = currentMode

    fun reset() {
        lastMovingTimestampMs = 0L
        lastRecordedLat = null
        lastRecordedLng = null
        currentMode = MovementMode.MOVING
    }

    private fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // Radius bumi dalam meter
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
