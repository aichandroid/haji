package com.haji2027.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 4 Tab Navigasi Utama Aplikasi Panduan Haji 2027.
 * Didesain khusus untuk aksesibilitas jemaah lansia (WCAG AAA, target sentuh >= 56dp).
 */
enum class AppTab(
    val title: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    GUIDE(
        title = "Panduan",
        icon = Icons.Default.AutoStories,
        contentDescription = "Panduan Manasik dan Doa Hari Ini"
    ),
    PLACES(
        title = "Tempat Saya",
        icon = Icons.Default.NearMe,
        contentDescription = "Tandai Tempat dan Kompas Spasial"
    ),
    TRACKING(
        title = "Lacak",
        icon = Icons.Default.People,
        contentDescription = "Pelacakan Keluarga dan Izin Privasi"
    ),
    SETTINGS(
        title = "Pengaturan",
        icon = Icons.Default.Settings,
        contentDescription = "Pengaturan, Unduhan OTA dan Privasi"
    )
}

/**
 * Rute navigasi internal untuk pilar fitur Panduan.
 */
sealed interface GuideRoute {
    data object Home : GuideRoute
    data class Detail(val stepId: String) : GuideRoute
    data class Doa(val doaId: String? = null) : GuideRoute
}

/**
 * Rute navigasi internal untuk pilar fitur Tempat Saya & Kompas.
 */
sealed interface PlacesRoute {
    data object List : PlacesRoute
    data class Compass(val placeId: String) : PlacesRoute
}
