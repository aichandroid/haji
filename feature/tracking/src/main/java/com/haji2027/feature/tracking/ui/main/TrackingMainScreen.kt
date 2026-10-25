package com.haji2027.feature.tracking.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.designsystem.theme.TypographyButton
import com.haji2027.core.designsystem.theme.TypographyElderlyBody
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle
import com.haji2027.feature.tracking.ui.TrackingTab
import com.haji2027.feature.tracking.ui.TrackingViewModel
import com.haji2027.feature.tracking.ui.consent.ConsentManagerScreen
import com.haji2027.feature.tracking.ui.scan.QrScanScreen
import com.haji2027.feature.tracking.ui.share.QrShareScreen

/**
 * Layar pusat fitur pelacakan keluarga (Hub Navigasi Utama, SCR-05, dan SCR-06).
 */
@Composable
fun TrackingMainScreen(
    viewModel: TrackingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (uiState.currentTab) {
        TrackingTab.SHARE_QR -> {
            QrShareScreen(
                shareState = uiState.shareQrState,
                onGenerateNewCode = { viewModel.generateNewQrToken() },
                onClose = { viewModel.navigateBack() },
                modifier = modifier
            )
        }

        TrackingTab.SCAN_QR -> {
            QrScanScreen(
                claimState = uiState.claimQrState,
                onClaimToken = { qr, label -> viewModel.claimScannedQr(qr, label) },
                onResetClaimState = { viewModel.resetClaimState() },
                onBack = { viewModel.navigateBack() },
                modifier = modifier
            )
        }

        TrackingTab.CONSENT_LIST -> {
            ConsentManagerScreen(
                isServiceRunning = uiState.isServiceRunning,
                activeTrackers = uiState.activeTrackers,
                revokingTrackerId = uiState.revokingTrackerId,
                onRevokeConsent = { id -> viewModel.revokeConsent(id) },
                onStopAllTracking = { viewModel.stopAllTracking(context) },
                onStartTracking = { viewModel.startTrackingService(context) },
                onBack = { viewModel.navigateBack() },
                modifier = modifier
            )
        }

        TrackingTab.MAIN -> {
            TrackingHubContent(
                isServiceRunning = uiState.isServiceRunning,
                activeTrackerCount = uiState.activeTrackers.size,
                feedbackMessage = uiState.userFeedbackMessage,
                onDismissFeedback = { viewModel.clearFeedbackMessage() },
                onStartTracking = { viewModel.startTrackingService(context) },
                onStopTracking = { viewModel.stopAllTracking(context) },
                onNavigateToShare = { viewModel.navigateTo(TrackingTab.SHARE_QR) },
                onNavigateToScan = { viewModel.navigateTo(TrackingTab.SCAN_QR) },
                onNavigateToConsent = { viewModel.navigateTo(TrackingTab.CONSENT_LIST) },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TrackingHubContent(
    isServiceRunning: Boolean,
    activeTrackerCount: Int,
    feedbackMessage: String?,
    onDismissFeedback: () -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToConsent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = BackgroundSoftSky
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(HajjDimens.SpaceMedium)
                .verticalScroll(scrollState)
        ) {
            // Header Bar
            Text(
                text = "PELACAKAN KELUARGA",
                style = TypographyElderlyTitle.copy(fontSize = 24.sp),
                color = PrimaryDarkNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pantau dan bagikan koordinat lokasi secara aman, privat, dan hemat baterai di Tanah Suci.",
                style = TypographyElderlyBody,
                color = MutedTextSlate
            )

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Banner Pesan Feedback Pengguna
            if (feedbackMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .background(SurfaceWhite)
                        .border(1.dp, PrimaryTrustBlue, RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryTrustBlue,
                                modifier = Modifier.size(HajjDimens.IconSmall)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMessage,
                                style = TypographyElderlyBody.copy(fontSize = 15.sp),
                                color = PrimaryDarkNavy
                            )
                        }

                        IconButton(onClick = onDismissFeedback) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedTextSlate)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))
            }

            // Kartu Ringkasan Status Layanan Lokasi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .padding(HajjDimens.SpaceMedium)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isServiceRunning) AccentHajjGreen else MutedTextSlate,
                                modifier = Modifier.size(HajjDimens.IconMedium)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Layanan Lokasi",
                                style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryDarkNavy
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isServiceRunning) AccentHajjGreen.copy(alpha = 0.15f)
                                    else StatusAmberWarning.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isServiceRunning) "AKTIF" else "MATI",
                                style = TypographyElderlyBody.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isServiceRunning) AccentHajjGreen else StatusAmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isServiceRunning) {
                            "Layanan latar belakang aktif memperbarui posisi ke antrean data privasi."
                        } else {
                            "Layanan sedang mati. Aktifkan agar keluarga dapat memantau saat Anda membutuhkan."
                        },
                        style = TypographyElderlyBody.copy(fontSize = 15.sp),
                        color = MutedTextSlate
                    )

                    Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                    Button(
                        onClick = if (isServiceRunning) onStopTracking else onStartTracking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HajjDimens.MinTouchTargetSize),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceRunning) StatusRedError else AccentHajjGreen
                        ),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                    ) {
                        Text(
                            text = if (isServiceRunning) "HENTIKAN LAYANAN LOKASI" else "AKTIFKAN LAYANAN LOKASI",
                            style = TypographyButton
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Menu Navigasi Fitur Utama
            Text(
                text = "PILIHAN FITUR:",
                style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                color = PrimaryDarkNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceMedium)) {
                TrackingHubMenuCard(
                    title = "Bagikan Lokasi Saya",
                    subtitle = "Tampilkan kode QR 5 menit untuk dipindai oleh keluarga (SCR-05).",
                    icon = Icons.Default.QrCode,
                    badgeText = "QR 5 Menit",
                    onClick = onNavigateToShare
                )

                TrackingHubMenuCard(
                    title = "Pindai QR Keluarga",
                    subtitle = "Pindai kode QR atau tempel token untuk memantau anggota keluarga.",
                    icon = Icons.Default.QrCodeScanner,
                    badgeText = "Pindai / Input",
                    onClick = onNavigateToScan
                )

                TrackingHubMenuCard(
                    title = "Siapa yang Melacak Saya",
                    subtitle = "Kelola izin dan daftar keluarga yang dapat melihat lokasi Anda (SCR-06).",
                    icon = Icons.Default.Security,
                    badgeText = "$activeTrackerCount Keluarga",
                    onClick = onNavigateToConsent
                )
            }
        }
    }
}

@Composable
private fun TrackingHubMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
            .background(SurfaceWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
            .clickable(onClick = onClick)
            .padding(HajjDimens.SpaceMedium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryTrustBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryTrustBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = TypographyElderlyTitle.copy(fontSize = 18.sp),
                        color = PrimaryDarkNavy
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BorderSubtle.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = TypographyElderlyBody.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = PrimaryDarkNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    style = TypographyElderlyBody.copy(fontSize = 14.sp),
                    color = MutedTextSlate
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MutedTextSlate,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
