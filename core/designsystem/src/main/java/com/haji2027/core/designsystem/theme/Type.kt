package com.haji2027.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp

/**
 * Tipografi sistem "Accessible & Ethical" untuk kemudahan baca jemaah lansia dan teks doa Arab.
 */
val TypographyArabicTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 44.sp,
    textDirection = TextDirection.Rtl
)

val TypographyArabicBody = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 40.sp,
    textDirection = TextDirection.Rtl
)

val TypographyElderlyTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 30.sp
)

val TypographyElderlyBody = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 26.sp
)

val TypographyButton = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp,
    lineHeight = 28.sp
)

// Material 3 Typography Overrides
val HajjTypography = Typography(
    headlineLarge = TypographyElderlyTitle.copy(fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = TypographyElderlyTitle,
    headlineSmall = TypographyElderlyTitle.copy(fontSize = 20.sp, lineHeight = 28.sp),
    bodyLarge = TypographyElderlyBody,
    bodyMedium = TypographyElderlyBody.copy(fontSize = 16.sp, lineHeight = 24.sp),
    labelLarge = TypographyButton
)
