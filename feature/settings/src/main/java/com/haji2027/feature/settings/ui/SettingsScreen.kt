package com.haji2027.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.haji2027.feature.settings.battery.BatteryWhitelistCard
import com.haji2027.feature.settings.ota.ContentSource

/**
 * Layar Pengaturan & Pembaruan Materi Manasik Over-The-Air (OTA) dengan verifikasi hash SHA-256.
 */
@Composable
fun SettingsScreen(
    viewModel: OtaViewModel,
    purgeViewModel: com.haji2027.feature.settings.purge.PurgeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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
                text = "PENGATURAN & PEMBARUAN",
                style = TypographyElderlyTitle.copy(fontSize = 24.sp),
                color = PrimaryDarkNavy
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Kelola pembaruan materi manasik dan verifikasi keabsahan data tanpa instal ulang aplikasi.",
                style = TypographyElderlyBody,
                color = MutedTextSlate
            )

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Banner Notifikasi Pengguna
            if (uiState.userNotification != null) {
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
                                text = uiState.userNotification ?: "",
                                style = TypographyElderlyBody.copy(fontSize = 15.sp),
                                color = PrimaryDarkNavy
                            )
                        }

                        IconButton(onClick = { viewModel.dismissNotification() }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MutedTextSlate)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))
            }

            // Kartu Status Materi Aktif Saat Ini
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
                        Text(
                            text = "Materi Manasik Aktif:",
                            style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryDarkNavy
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (uiState.contentSource == ContentSource.OTA_OVERLAY) AccentHajjGreen.copy(alpha = 0.15f)
                                    else PrimaryTrustBlue.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Versi ${uiState.currentVersion}",
                                style = TypographyElderlyBody.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (uiState.contentSource == ContentSource.OTA_OVERLAY) AccentHajjGreen else PrimaryTrustBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (uiState.contentSource == ContentSource.OTA_OVERLAY) {
                            "Lapisan Aktif: Layer 2 (Pembaruan OTA Terverifikasi)"
                        } else {
                            "Lapisan Aktif: Layer 1 (Materi Bawaan APK)"
                        },
                        style = TypographyElderlyBody.copy(fontSize = 15.sp),
                        color = MutedTextSlate
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AccentHajjGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Integritas Kriptografis: Terverifikasi SHA-256",
                            style = TypographyElderlyBody.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = AccentHajjGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Status & Hasil Pengecekan OTA
            when (val status = uiState.status) {
                is OtaStatus.Checking -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PrimaryTrustBlue,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Memeriksa pembaruan di server resmi...",
                                style = TypographyElderlyBody,
                                color = MutedTextSlate
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                is OtaStatus.DownloadingAndVerifying -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = AccentHajjGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Mengunduh materi dan memverifikasi integritas hash SHA-256...",
                                style = TypographyElderlyBody,
                                color = MutedTextSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                is OtaStatus.UpdateAvailable -> {
                    val manifest = status.manifest
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(2.dp, AccentHajjGreen, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceMedium)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = AccentHajjGreen,
                                    modifier = Modifier.size(HajjDimens.IconMedium)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pembaruan Materi Tersedia!",
                                    style = TypographyElderlyTitle.copy(fontSize = 20.sp),
                                    color = AccentHajjGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Versi Baru: ${manifest.version} (Rilis: ${manifest.publishedDate})",
                                style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryDarkNavy
                            )

                            if (manifest.releaseNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Catatan: ${manifest.releaseNotes}",
                                    style = TypographyElderlyBody.copy(fontSize = 15.sp),
                                    color = MutedTextSlate
                                )
                            }

                            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                            Button(
                                onClick = { viewModel.applyUpdate(manifest) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(HajjDimens.MinTouchTargetSize),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                                shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "UNDUH & TERAPKAN PEMBARUAN",
                                    style = TypographyButton
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                is OtaStatus.AlreadyUpToDate -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(1.dp, AccentHajjGreen, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceMedium)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentHajjGreen,
                                modifier = Modifier.size(HajjDimens.IconMedium)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Materi manasik Anda sudah paling mutakhir (Versi ${uiState.currentVersion}).",
                                style = TypographyElderlyBody,
                                color = PrimaryDarkNavy
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                is OtaStatus.UpdateSuccess -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(2.dp, AccentHajjGreen, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceMedium)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentHajjGreen,
                                modifier = Modifier.size(HajjDimens.IconMedium)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Materi manasik berhasil diperbarui ke Versi ${status.version}!",
                                style = TypographyElderlyTitle.copy(fontSize = 18.sp),
                                color = AccentHajjGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                is OtaStatus.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(2.dp, StatusRedError, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceMedium)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusRedError,
                                    modifier = Modifier.size(HajjDimens.IconMedium)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (status.isIntegrityError) "Pembaruan Ditolak!" else "Gagal Memeriksa",
                                    style = TypographyElderlyTitle.copy(fontSize = 18.sp),
                                    color = StatusRedError
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = status.message,
                                style = TypographyElderlyBody.copy(fontSize = 15.sp),
                                color = MutedTextSlate
                            )

                            if (status.isIntegrityError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Aplikasi tetap aman menggunakan materi bawaan (Layer 1) demi keabsahan manasik Anda.",
                                    style = TypographyElderlyBody.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                    color = PrimaryDarkNavy
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))
                }

                OtaStatus.Idle -> {
                    // Idle state
                }
            }

            // Tombol Aksi Utama Pengecekan
            Column(
                verticalArrangement = Arrangement.spacedBy(HajjDimens.TouchTargetGap),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.checkForUpdate() },
                    enabled = uiState.status !is OtaStatus.Checking && uiState.status !is OtaStatus.DownloadingAndVerifying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTrustBlue),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(HajjDimens.IconSmall)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PERIKSA PEMBARUAN MATERI",
                        style = TypographyButton
                    )
                }

                // Tombol Rollback jika sedang menggunakan Layer 2
                if (uiState.contentSource == ContentSource.OTA_OVERLAY) {
                    OutlinedButton(
                        onClick = { viewModel.rollbackToBundled() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(HajjDimens.MinTouchTargetSize),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAmberWarning),
                        shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = StatusAmberWarning,
                            modifier = Modifier.size(HajjDimens.IconSmall)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KEMBALIKAN KE MATERI BAWAAN",
                            style = TypographyButton,
                            color = StatusAmberWarning
                        )
                    }
                }
            }

            // Bagian Optimasi Baterai & Ketahanan Pelacakan OEM (FR-ONB-004 & TC-BAT-01)
            Spacer(modifier = Modifier.height(HajjDimens.SpaceExtraLarge))
            BatteryWhitelistCard()

            // Bagian Hak Subjek Data: Pembersihan Total (Right to be Forgotten - TASK-18)
            if (purgeViewModel != null) {
                Spacer(modifier = Modifier.height(HajjDimens.SpaceExtraLarge))
                PurgeDataSection(viewModel = purgeViewModel)
            }
        }
    }
}
