package com.haji2027.feature.places.domain

import kotlinx.coroutines.flow.Flow

/**
 * Kontrak penyedia aliran sampel koordinat GPS.
 * Memisahkan logika platform Android dari logika agregasi matematis.
 */
interface LocationSampleProvider {
    /**
     * Meminta pembaruan sampel lokasi dengan interval sampling tertentu dalam milidetik.
     */
    fun requestLocationUpdates(intervalMs: Long): Flow<GpsSample>
}
