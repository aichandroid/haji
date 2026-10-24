package com.haji2027.feature.guide.data

import com.haji2027.core.model.HajjContentPackage
import com.haji2027.core.model.HajjDay

/**
 * Kontrak repositori untuk mengakses konten manasik haji.
 */
interface HajjGuideRepository {
    suspend fun getContentPackage(): HajjContentPackage
    suspend fun getAllDays(): List<HajjDay>
    suspend fun getDayById(dayId: String): HajjDay?
    suspend fun getDayByHijri(hijriDay: Int, hijriMonth: Int): HajjDay?
    suspend fun getReadingById(readingId: String): com.haji2027.core.model.DoaReading?
}
