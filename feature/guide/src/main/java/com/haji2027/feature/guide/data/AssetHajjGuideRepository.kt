package com.haji2027.feature.guide.data

import com.haji2027.core.common.parser.HajjContentParser
import com.haji2027.core.model.HajjContentPackage
import com.haji2027.core.model.HajjDay

/**
 * Implementasi repositori berbasis aset JSON lokal (Offline-First).
 * Menerima penyedia string JSON agar dapat diuji di lingkungan JVM tanpa memerlukan Android Context.
 */
class AssetHajjGuideRepository(
    private val jsonContentProvider: () -> String
) : HajjGuideRepository {

    private var cachedPackage: HajjContentPackage? = null

    override suspend fun getContentPackage(): HajjContentPackage {
        return cachedPackage ?: synchronized(this) {
            cachedPackage ?: HajjContentParser.parse(jsonContentProvider()).also {
                cachedPackage = it
            }
        }
    }

    override suspend fun getAllDays(): List<HajjDay> {
        return getContentPackage().days
    }

    override suspend fun getDayById(dayId: String): HajjDay? {
        return getAllDays().find { it.dayId == dayId }
    }

    override suspend fun getDayByHijri(hijriDay: Int, hijriMonth: Int): HajjDay? {
        return getAllDays().find { it.hijriDay == hijriDay && it.hijriMonth == hijriMonth }
    }
}
