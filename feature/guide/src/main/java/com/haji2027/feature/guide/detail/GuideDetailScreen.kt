package com.haji2027.feature.guide.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.feature.guide.detail.components.ActivityCard

/**
 * Layar SCR-02: Detail Kegiatan Manasik & Checklist Progres.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideDetailScreen(
    viewModel: GuideDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDoa: (readingId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSoftSky,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.day?.title ?: "Panduan Manasik",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(HajjDimens.MinTouchTargetSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
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
            val day = uiState.day
            if (day == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Data manasik tidak ditemukan.",
                        fontSize = 16.sp,
                        color = MutedTextSlate
                    )
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
                    // Kartu Rangkuman Hari & Progres
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BorderSubtle),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(HajjDimens.SpaceMedium),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${day.title} (${day.hijriDay}/${day.hijriMonth} H)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkNavy
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = PrimaryTrustBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Lokasi: ${day.location}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MutedTextSlate
                                )
                            }

                            Text(
                                text = day.summary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = PrimaryDarkNavy,
                                lineHeight = 22.sp
                            )

                            // Status Progres Checklist
                            Text(
                                text = "Progres: ${uiState.completedSteps} dari ${uiState.totalSteps} langkah selesai (${uiState.progressPercentage}%)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.progressPercentage == 100) AccentHajjGreen else PrimaryDarkNavy,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            LinearProgressIndicator(
                                progress = { uiState.progressFraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = AccentHajjGreen,
                                trackColor = BorderSubtle
                            )
                        }
                    }

                    // Daftar Kegiatan (Activities)
                    for (activity in day.activities) {
                        ActivityCard(
                            activity = activity,
                            completedStepIds = uiState.completedStepIds,
                            onToggleComplete = { stepId, isCompleted ->
                                viewModel.toggleStepCompleted(stepId, isCompleted)
                            },
                            onNavigateToDoa = onNavigateToDoa
                        )
                    }
                }
            }
        }
    }
}
