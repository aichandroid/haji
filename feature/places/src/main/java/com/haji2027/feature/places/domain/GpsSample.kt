package com.haji2027.feature.places.domain

/**
 * Model sampel tunggal pembacaan GPS.
 */
data class GpsSample(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

/**
 * Hasil agregasi dari beberapa sampel GPS menggunakan median koordinat dan rata-rata akurasi.
 */
data class AveragedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val validSampleCount: Int,
    val totalSampleCount: Int
)

/**
 * Status reaktif proses pengambilan sampel multi-sampling GPS.
 */
sealed interface GpsSamplingState {
    data object Idle : GpsSamplingState

    data class Sampling(
        val collectedSamples: Int,
        val targetSamples: Int,
        val latestAccuracy: Float? = null
    ) : GpsSamplingState

    data class Success(
        val location: AveragedLocation
    ) : GpsSamplingState

    data class Error(
        val reason: GpsSamplingError,
        val message: String? = null
    ) : GpsSamplingState
}

/**
 * Kode galat sampling GPS.
 */
enum class GpsSamplingError {
    TIMEOUT_INSUFFICIENT_SAMPLES,
    POOR_ACCURACY_ALL_FILTERED,
    GPS_DISABLED,
    PERMISSION_DENIED
}
