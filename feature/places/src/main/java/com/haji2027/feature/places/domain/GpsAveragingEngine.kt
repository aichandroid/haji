package com.haji2027.feature.places.domain

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

/**
 * Mesin pengambil sampel GPS berulang (multi-sampling) dan agregasi median koordinat.
 * Bertujuan menstabilkan titik lokasi tempat tersimpan (Pintu Masjid, Hotel, Tenda Mina)
 * dan mengeliminasi noise/galat multipath di area berkerumun padat.
 */
class GpsAveragingEngine(
    private val sampleProvider: LocationSampleProvider? = null
) {
    companion object {
        const val DEFAULT_TARGET_SAMPLES = 5
        const val DEFAULT_SAMPLE_INTERVAL_MS = 800L
        const val DEFAULT_MAX_ACCURACY_METERS = 25f
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val MIN_REQUIRED_VALID_SAMPLES = 3
    }

    /**
     * Menghitung nilai koordinat median dan akurasi rata-rata dari sekumpulan sampel GPS.
     * Mengeliminasi sampel yang memiliki accuracy > [maxAccuracyMeters].
     *
     * @return [AveragedLocation] jika jumlah sampel valid >= [minValidSamples], atau null jika tidak mencukupi.
     */
    fun calculateAveragedLocation(
        samples: List<GpsSample>,
        maxAccuracyMeters: Float = DEFAULT_MAX_ACCURACY_METERS,
        minValidSamples: Int = MIN_REQUIRED_VALID_SAMPLES
    ): AveragedLocation? {
        val validSamples = samples.filter { it.accuracyMeters <= maxAccuracyMeters }
        if (validSamples.size < minValidSamples) {
            return null
        }

        val sortedLats = validSamples.map { it.latitude }.sorted()
        val sortedLngs = validSamples.map { it.longitude }.sorted()

        val medianLat = calculateMedian(sortedLats)
        val medianLng = calculateMedian(sortedLngs)
        val avgAccuracy = validSamples.map { it.accuracyMeters }.average().toFloat()

        return AveragedLocation(
            latitude = medianLat,
            longitude = medianLng,
            accuracyMeters = avgAccuracy,
            validSampleCount = validSamples.size,
            totalSampleCount = samples.size
        )
    }

    private fun calculateMedian(sortedValues: List<Double>): Double {
        val size = sortedValues.size
        return if (size % 2 == 1) {
            sortedValues[size / 2]
        } else {
            (sortedValues[(size / 2) - 1] + sortedValues[size / 2]) / 2.0
        }
    }

    /**
     * Memulai alur multi-sampling GPS secara reaktif.
     * Mengalirkan status progres [GpsSamplingState.Sampling] setiap sampel masuk,
     * dan menghasilkan [GpsSamplingState.Success] atau [GpsSamplingState.Error].
     */
    fun startSampling(
        targetSamples: Int = DEFAULT_TARGET_SAMPLES,
        intervalMs: Long = DEFAULT_SAMPLE_INTERVAL_MS,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        maxAccuracyMeters: Float = DEFAULT_MAX_ACCURACY_METERS,
        minValidSamples: Int = MIN_REQUIRED_VALID_SAMPLES
    ): Flow<GpsSamplingState> = flow {
        val provider = sampleProvider ?: run {
            emit(GpsSamplingState.Error(GpsSamplingError.GPS_DISABLED, "LocationSampleProvider tidak tersedia"))
            return@flow
        }

        emit(GpsSamplingState.Sampling(collectedSamples = 0, targetSamples = targetSamples))

        val collected = mutableListOf<GpsSample>()

        try {
            withTimeout(timeoutMs) {
                provider.requestLocationUpdates(intervalMs)
                    .collect { sample ->
                        collected.add(sample)
                        emit(
                            GpsSamplingState.Sampling(
                                collectedSamples = collected.size,
                                targetSamples = targetSamples,
                                latestAccuracy = sample.accuracyMeters
                            )
                        )

                        if (collected.size >= targetSamples) {
                            throw TargetSamplesReachedException()
                        }
                    }
            }
        } catch (_: TargetSamplesReachedException) {
            // Selesai mengumpulkan target sampel
        } catch (_: TimeoutCancellationException) {
            // Timeout tercapai
        } catch (e: SecurityException) {
            emit(GpsSamplingState.Error(GpsSamplingError.PERMISSION_DENIED, e.message))
            return@flow
        } catch (e: Exception) {
            emit(GpsSamplingState.Error(GpsSamplingError.GPS_DISABLED, e.message))
            return@flow
        }

        // Evaluasi sampel yang terkumpul
        val averaged = calculateAveragedLocation(collected, maxAccuracyMeters, minValidSamples)
        if (averaged != null) {
            emit(GpsSamplingState.Success(averaged))
        } else {
            if (collected.isEmpty() || collected.size < minValidSamples) {
                emit(
                    GpsSamplingState.Error(
                        GpsSamplingError.TIMEOUT_INSUFFICIENT_SAMPLES,
                        "Hanya terkumpul ${collected.size} sampel dalam batas waktu"
                    )
                )
            } else {
                emit(
                    GpsSamplingState.Error(
                        GpsSamplingError.POOR_ACCURACY_ALL_FILTERED,
                        "Seluruh sampel memiliki akurasi di atas batas toleransi (${maxAccuracyMeters}m)"
                    )
                )
            }
        }
    }

    private class TargetSamplesReachedException : Exception()
}
