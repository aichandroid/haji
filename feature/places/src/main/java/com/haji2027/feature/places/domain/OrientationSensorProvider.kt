package com.haji2027.feature.places.domain

import kotlinx.coroutines.flow.Flow

/**
 * Kontrak penyedia aliran data sensor orientasi kompas perangkat.
 */
interface OrientationSensorProvider {
    /**
     * Mengamati aliran orientasi ponsel secara kontinu.
     *
     * @param declinationDegrees Nilai deklinasi magnetik lokal (default Makkah/Madinah ~ +2.8°).
     */
    fun observeOrientation(declinationDegrees: Float = MAKKAH_DEFAULT_DECLINATION): Flow<CompassOrientation>

    companion object {
        /**
         * Nilai perkiraan deklinasi magnetik rata-rata kawasan Tanah Suci Makkah dan Madinah (+2.8° Timur).
         */
        const val MAKKAH_DEFAULT_DECLINATION = 2.8f
    }
}
