package com.haji2027.core.designsystem

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.designsystem.theme.TypographyArabicBody
import com.haji2027.core.designsystem.theme.TypographyArabicTitle
import com.haji2027.core.designsystem.theme.TypographyButton
import com.haji2027.core.designsystem.theme.TypographyElderlyBody
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit token desain untuk memastikan standar aksesibilitas lansia WCAG AAA terpenuhi.
 */
class TokensTest {

    @Test
    fun testElderlyTouchTargetMeetsAccessibilityStandard() {
        // Standar produk mewajibkan target sentuh minimal 56dp (melampaui baseline Android 48dp)
        assertTrue(
            "Target sentuh lansia minimal 56dp",
            HajjDimens.MinTouchTargetSize >= 56.dp
        )
    }

    @Test
    fun testTouchTargetGapPreventsAccidentalMisTaps() {
        // Jarak antar tombol minimal 12dp untuk mencegah salah sentuh tangan bergetar/lansia
        assertTrue(
            "Jarak antar tombol minimal 12dp",
            HajjDimens.TouchTargetGap >= 12.dp
        )
    }

    @Test
    fun testElderlyTypographySizes() {
        // Teks tubuh lansia minimal 18sp
        assertTrue("Ukuran teks tubuh minimal 18sp", TypographyElderlyBody.fontSize >= 18.sp)
        
        // Judul lansia minimal 22sp
        assertTrue("Ukuran judul minimal 22sp", TypographyElderlyTitle.fontSize >= 22.sp)
        
        // Tombol aksi minimal 20sp
        assertTrue("Ukuran teks tombol minimal 20sp", TypographyButton.fontSize >= 20.sp)
        
        // Teks Arab minimal 24sp untuk keterbacaan harakat
        assertTrue("Ukuran teks Arab minimal 24sp", TypographyArabicBody.fontSize >= 24.sp)
        assertTrue("Ukuran judul Arab minimal 28sp", TypographyArabicTitle.fontSize >= 28.sp)
    }

    @Test
    fun testColorTokenAlphas() {
        // Warna utama tidak boleh transparan (alpha = 1.0f) untuk menjamin kontras maksimal
        assertEquals(1.0f, PrimaryDarkNavy.alpha, 0.01f)
        assertEquals(1.0f, SurfaceWhite.alpha, 0.01f)
    }
}
