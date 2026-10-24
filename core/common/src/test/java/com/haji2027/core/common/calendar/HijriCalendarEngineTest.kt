package com.haji2027.core.common.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Pengujian unit untuk [HijriCalendarEngine] memastikan konversi tanggal Umm al-Qura
 * dan kompensasi manual offset bekerja secara akurat.
 */
class HijriCalendarEngineTest {

    private lateinit var engine: HijriCalendarEngine

    @Before
    fun setUp() {
        engine = HijriCalendarEngine(ZoneId.of("Asia/Riyadh"))
    }

    @Test
    fun testIndonesianMonthNames() {
        assertEquals("Muharram", engine.getHijriMonthName(1))
        assertEquals("Ramadan", engine.getHijriMonthName(9))
        assertEquals("Syawal", engine.getHijriMonthName(10))
        assertEquals("Zulkaidah", engine.getHijriMonthName(11))
        assertEquals("Zulhijah", engine.getHijriMonthName(12))
    }

    @Test
    fun testHajjPeakDaysRecognition() {
        // 8 s.d. 13 Zulhijah adalah puncak haji
        assertTrue("8 Zulhijah harus terdeteksi puncak haji", engine.isPeakHajjDays(8, 12))
        assertTrue("9 Zulhijah (Arafah) harus terdeteksi puncak haji", engine.isPeakHajjDays(9, 12))
        assertTrue("10 Zulhijah (Nahar) harus terdeteksi puncak haji", engine.isPeakHajjDays(10, 12))
        assertTrue("11 Zulhijah (Tasyrik) harus terdeteksi puncak haji", engine.isPeakHajjDays(11, 12))
        assertTrue("12 Zulhijah (Tasyrik) harus terdeteksi puncak haji", engine.isPeakHajjDays(12, 12))
        assertTrue("13 Zulhijah (Tasyrik) harus terdeteksi puncak haji", engine.isPeakHajjDays(13, 12))

        // Di luar rentang puncak
        assertFalse("7 Zulhijah bukan puncak haji", engine.isPeakHajjDays(7, 12))
        assertFalse("14 Zulhijah bukan puncak haji", engine.isPeakHajjDays(14, 12))
        assertFalse("9 Ramadan bukan puncak haji", engine.isPeakHajjDays(9, 9))
    }

    @Test
    fun testManualOffsetCalculation() {
        // Gunakan tanggal tetap
        val testDate = LocalDate.of(2027, 5, 20)
        
        val baseHijri = engine.calculateHijriDate(testDate, offsetDays = 0)
        val plusOneHijri = engine.calculateHijriDate(testDate, offsetDays = 1)
        val minusOneHijri = engine.calculateHijriDate(testDate, offsetDays = -1)

        assertEquals("Offset dasar harus 0", 0, baseHijri.appliedOffsetDays)
        assertEquals("Offset +1 harus tercatat", 1, plusOneHijri.appliedOffsetDays)
        assertEquals("Offset -1 harus tercatat", -1, minusOneHijri.appliedOffsetDays)

        // Verifikasi pergeseran hari
        assertEquals(baseHijri.dayOfMonth + 1, plusOneHijri.dayOfMonth)
        assertEquals(baseHijri.dayOfMonth - 1, minusOneHijri.dayOfMonth)
    }

    @Test
    fun testOffsetClampingSafety() {
        val testDate = LocalDate.of(2027, 5, 20)

        // Offset melebihi batas maksimal (+3) harus dibatasi ke +3
        val clampedMax = engine.calculateHijriDate(testDate, offsetDays = 10)
        assertEquals(HijriCalendarEngine.MAX_OFFSET_DAYS, clampedMax.appliedOffsetDays)

        // Offset melebihi batas minimal (-3) harus dibatasi ke -3
        val clampedMin = engine.calculateHijriDate(testDate, offsetDays = -10)
        assertEquals(HijriCalendarEngine.MIN_OFFSET_DAYS, clampedMin.appliedOffsetDays)
    }

    @Test
    fun testFormattedDateOutput() {
        val testDate = LocalDate.of(2027, 5, 20)
        val hijriDate = engine.calculateHijriDate(testDate, offsetDays = 0)

        assertTrue(
            "Format tanggal harus memuat kata Zulhijah dan tahun Hijriah",
            hijriDate.formattedDate.contains("Zulhijah") && hijriDate.formattedDate.endsWith("H")
        )
        assertTrue(hijriDate.isHajjSeason)
    }
}
