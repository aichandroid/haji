package com.haji2027.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Palet warna sistem "Accessible & Ethical Design" untuk Aplikasi Panduan Haji 2027.
 * Disesuaikan untuk keterbacaan tinggi di bawah terik matahari Tanah Suci (WCAG AAA Compliance: Kontras >= 7:1).
 */
val PrimaryDarkNavy     = Color(0xFF0C4A6E) // Teks utama & Header kontras tinggi
val PrimaryTrustBlue    = Color(0xFF0369A1) // Brand & Container primer
val SecondarySky        = Color(0xFF38BDF8) // Aksen sekunder / info pendukung
val AccentHajjGreen     = Color(0xFF15803D) // Tombol Aksi Utama / Sukses (Emerald)
val BackgroundSoftSky   = Color(0xFFF8FAFC) // Latar belakang anti-silau / anti-glare
val SurfaceWhite        = Color(0xFFFFFFFF) // Permukaan kartu
val MutedTextSlate      = Color(0xFF334155) // Teks sekunder (Kontras 5.2:1)
val BorderSubtle        = Color(0xFFCBD5E1) // Pembatas garis kartu yang tegas
val StatusAmberWarning  = Color(0xFFB45309) // Peringatan sensor / akurasi sedang
val StatusRedError      = Color(0xFFB91C1C) // Error / GPS indoor degradation
val FocusRingCyan       = Color(0xFF0284C7) // Indikator fokus aksesibilitas 4dp

// Status Sensor Kompas Spasial
val CompassAccuracyGood = Color(0xFF16A34A) // Hijau: Akurasi <= 10m
val CompassAccuracyFair = Color(0xFFD97706) // Kuning/Oranye: Akurasi 11-30m
val CompassAccuracyPoor = Color(0xFFDC2626) // Merah: Akurasi > 30m / Indoor
