package com.haji2027.core.common.calendar

import com.haji2027.core.model.HijriDateModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/**
 * Mesin kalkulasi kalender Hijriah Umm al-Qura bawaan Android (HijrahChronology)
 * dengan dukungan penyesuaian manual offset (+/- hari) untuk menyelaraskan dengan
 * ketetapan ru'yah hilal resmi Kerajaan Arab Saudi.
 */
class HijriCalendarEngine(
    private val defaultZoneId: ZoneId = ZoneId.of("Asia/Riyadh")
) {

    companion object {
        const val MIN_OFFSET_DAYS = -3
        const val MAX_OFFSET_DAYS = 3
        const val HAJJ_MONTH_NUMBER = 12 // Zulhijah

        private val HIJRI_MONTH_NAMES = arrayOf(
            "", // Index 0 tidak digunakan
            "Muharram",
            "Safar",
            "Rabiul Awal",
            "Rabiul Akhir",
            "Jumadil Awal",
            "Jumadil Akhir",
            "Rajab",
            "Syakban",
            "Ramadan",
            "Syawal",
            "Zulkaidah",
            "Zulhijah"
        )
    }

    /**
     * Mendapatkan nama bulan Hijriah dalam Bahasa Indonesia standar.
     */
    fun getHijriMonthName(monthNumber: Int): String {
        return if (monthNumber in 1..12) {
            HIJRI_MONTH_NAMES[monthNumber]
        } else {
            "Bulan $monthNumber"
        }
    }

    /**
     * Menghitung tanggal Hijriah saat ini di zona waktu Arab Saudi (atau zona kustom)
     * dengan penyesuaian manual offset.
     */
    fun getCurrentHijriDate(offsetDays: Int = 0): HijriDateModel {
        val todayInSaudi = LocalDate.now(defaultZoneId)
        return calculateHijriDate(todayInSaudi, offsetDays)
    }

    /**
     * Mengonversi [LocalDate] Gregorian tertentu menjadi [HijriDateModel] Hijriah
     * dengan penerapan kompensasi offset (-3 s.d. +3 hari).
     */
    fun calculateHijriDate(gregorianDate: LocalDate, offsetDays: Int = 0): HijriDateModel {
        val clampedOffset = offsetDays.coerceIn(MIN_OFFSET_DAYS, MAX_OFFSET_DAYS)
        val baseHijrahDate: HijrahDate = HijrahChronology.INSTANCE.date(gregorianDate)
        
        val adjustedHijrahDate: HijrahDate = if (clampedOffset != 0) {
            baseHijrahDate.plus(clampedOffset.toLong(), ChronoUnit.DAYS) as HijrahDate
        } else {
            baseHijrahDate
        }

        val day = adjustedHijrahDate.get(ChronoField.DAY_OF_MONTH)
        val month = adjustedHijrahDate.get(ChronoField.MONTH_OF_YEAR)
        val year = adjustedHijrahDate.get(ChronoField.YEAR)
        val monthName = getHijriMonthName(month)
        val isHajjSeason = (month == HAJJ_MONTH_NUMBER)
        val formattedDate = "$day $monthName $year H"

        return HijriDateModel(
            dayOfMonth = day,
            monthNumber = month,
            monthName = monthName,
            year = year,
            isHajjSeason = isHajjSeason,
            appliedOffsetDays = clampedOffset,
            formattedDate = formattedDate
        )
    }

    /**
     * Menentukan apakah suatu tanggal Hijriah berada dalam rentang puncak manasik haji
     * (8 s.d. 13 Zulhijah: Tarwiyah, Arafah, Muzdalifah, Nahar, dan Hari Tasyrik).
     */
    fun isPeakHajjDays(hijriDay: Int, hijriMonth: Int): Boolean {
        return hijriMonth == HAJJ_MONTH_NUMBER && hijriDay in 8..13
    }
}
