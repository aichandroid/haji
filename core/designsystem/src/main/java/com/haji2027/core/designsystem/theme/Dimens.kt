package com.haji2027.core.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimensi standar antarmuka ramah lansia (Elderly Touch Standards) & irama spasi 8dp.
 */
object HajjDimens {
    // Dimensi Target Sentuh Khusus Lansia
    val MinTouchTargetSize: Dp  = 56.dp // Standar tombol utama (jauh di atas 48dp Android)
    val TouchTargetGap: Dp      = 12.dp // Jarak antar tombol untuk mencegah salah sentuh
    val CardCornerRadius: Dp    = 16.dp // Radius sudut kartu
    val FocusBorderWidth: Dp    = 3.dp  // Ketebalan garis fokus aksesibilitas

    // Skala Spasi Irama 8dp (Spacing Grid)
    val SpaceExtraSmall: Dp     = 4.dp
    val SpaceSmall: Dp          = 8.dp
    val SpaceMedium: Dp         = 16.dp
    val SpaceLarge: Dp          = 24.dp
    val SpaceExtraLarge: Dp     = 32.dp
    val SpaceHuge: Dp           = 48.dp

    // Dimensi Ikon Vektor Struktural (No Emoji)
    val IconSmall: Dp           = 24.dp
    val IconMedium: Dp          = 32.dp // Standar tombol aksi
    val IconLarge: Dp           = 48.dp
    val IconHero: Dp            = 64.dp // Indikator status utama

    // Dimensi Kompas Panah Spasial
    val CompassSize: Dp         = 240.dp
    val CompassArrowWidth: Dp   = 40.dp
    val CompassArrowHeight: Dp  = 140.dp
}
