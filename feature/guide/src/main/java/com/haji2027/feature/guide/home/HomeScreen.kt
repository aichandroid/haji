package com.haji2027.feature.guide.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.feature.guide.home.components.BigActionButton
import com.haji2027.feature.guide.home.components.HomeDateHeader
import com.haji2027.feature.guide.home.components.HomeDaySummaryCard

/**
 * Layar SCR-01: Beranda Utama ("Hari Ini").
 * Menyajikan konteks waktu ibadah instan, progres checklist, dan 3 tombol aksi raksasa ramah lansia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGuide: (dayId: String) -> Unit,
    onNavigateToPlaces: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToGuide = onNavigateToGuide,
        onNavigateToPlaces = onNavigateToPlaces,
        onNavigateToTracking = onNavigateToTracking,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToGuide: (dayId: String) -> Unit,
    onNavigateToPlaces: () -> Unit,
    onNavigateToTracking: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSoftSky,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PANDUAN HAJI 2027",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                },
                actions = {
                    // Badge Status Offline Siap
                    Box(
                        modifier = Modifier
                            .background(
                                color = AccentHajjGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Offline: Siap",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentHajjGreen
                        )
                    }

                    // Tombol Pengaturan Aksesibel (Target Sentuh >= 56dp)
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(HajjDimens.MinTouchTargetSize)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Aplikasi",
                            tint = PrimaryDarkNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryDarkNavy)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = HajjDimens.SpaceMedium, vertical = HajjDimens.SpaceSmall),
                verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceMedium)
            ) {
                // 1. Header Status Tanggal Tanah Suci
                HomeDateHeader(
                    hijriDateFormatted = uiState.hijriDateFormatted,
                    gregorianDateFormatted = uiState.gregorianDateFormatted,
                    appliedOffsetDays = uiState.appliedOffsetDays
                )

                // 2. Kartu Rangkuman Ibadah Hari Ini
                HomeDaySummaryCard(
                    day = uiState.activeDay,
                    isPeakHajj = uiState.isHajjPeakActive,
                    totalSteps = uiState.totalStepsToday,
                    completedSteps = uiState.completedStepsToday,
                    progressPercentage = uiState.progressPercentage,
                    progressFraction = uiState.progressFraction,
                    onClick = {
                        uiState.activeDay?.let { onNavigateToGuide(it.dayId) }
                    }
                )

                // 3. Tiga Tombol Aksi Raksasa Ramah Lansia
                BigActionButton(
                    icon = Icons.Default.MenuBook,
                    title = "1. PANDUAN HARI INI",
                    subtitle = "Buka rangkaian ibadah & doa hari ini",
                    onClick = {
                        uiState.activeDay?.let { onNavigateToGuide(it.dayId) }
                    }
                )

                BigActionButton(
                    icon = Icons.Default.Place,
                    title = "2. TEMPAT SAYA",
                    subtitle = "Arah kembali ke Hotel, Pintu, & Tenda",
                    onClick = onNavigateToPlaces
                )

                BigActionButton(
                    icon = Icons.Default.People,
                    title = "3. LACAK KELUARGA",
                    subtitle = "Pantau posisi kerabat & bagikan lokasi",
                    onClick = onNavigateToTracking
                )

                // 4. Banner Bantuan Darurat
                EmergencyHelpBanner()
            }
        }
    }
}

@Composable
private fun EmergencyHelpBanner() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HajjDimens.SpaceSmall),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HajjDimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Bantuan",
                tint = PrimaryDarkNavy,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "Butuh bantuan darurat? Hubungi Ketua Rombongan atau Petugas Haji Indonesia terdekat.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MutedTextSlate,
                lineHeight = 20.sp
            )
        }
    }
}
