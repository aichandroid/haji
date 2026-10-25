package com.haji2027.feature.tracking.ui.consent

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.designsystem.theme.TypographyButton
import com.haji2027.core.designsystem.theme.TypographyElderlyBody
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle
import com.haji2027.feature.tracking.ui.TrackerConsentItem

/**
 * Layar SCR-06: Manajemen Izin ("Siapa yang Melacak Saya").
 * Memberikan transparansi mutlak dan kontrol penuh kepada jemaah atas
 * siapa saja yang memiliki akses koordinatnya, dengan tombol pencabutan instan (>= 56dp).
 */
@Composable
fun ConsentManagerScreen(
    isServiceRunning: Boolean,
    activeTrackers: List<TrackerConsentItem>,
    revokingTrackerId: String?,
    onRevokeConsent: (trackerUserId: String) -> Unit,
    onStopAllTracking: () -> Unit,
    onStartTracking: () -> Unit,
    onBack: () -> Unit,
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(HajjDimens.MinTouchTargetSize)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = PrimaryDarkNavy
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "SIAPA YANG MELACAK SAYA",
                    style = TypographyElderlyTitle,
                    color = PrimaryDarkNavy
                )
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Kartu Status Berbagi Lokasi
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
                            text = "Status Berbagi Lokasi:",
                            style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryDarkNavy
                        )

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
                                text = if (isServiceRunning) "[ AKTIF - BERJALAN ]" else "[ TIDAK AKTIF ]",
                                style = TypographyElderlyBody.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isServiceRunning) AccentHajjGreen else StatusAmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isServiceRunning) {
                            "Layanan lokasi berjalan di latar belakang (Foreground)."
                        } else {
                            "Layanan lokasi saat ini sedang mati."
                        },
                        style = TypographyElderlyBody.copy(fontSize = 15.sp),
                        color = MutedTextSlate
                    )

                    if (!isServiceRunning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onStartTracking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AKTIFKAN KEMBALI LAYANAN", style = TypographyButton.copy(fontSize = 16.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Daftar Anggota Keluarga Terdaftar
            Text(
                text = "DAFTAR ANGGOTA KELUARGA TERDAFTAR:",
                style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                color = PrimaryDarkNavy,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTrackers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .background(SurfaceWhite)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .padding(HajjDimens.SpaceLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada anggota keluarga yang terdaftar memantau lokasi Anda.",
                        style = TypographyElderlyBody,
                        color = MutedTextSlate
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceMedium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activeTrackers.forEachIndexed { index, item ->
                        val isBeingRevoked = revokingTrackerId == item.trackerUserId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                                .background(SurfaceWhite)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                                .padding(HajjDimens.SpaceMedium)
                        ) {
                            Column {
                                Text(
                                    text = "${index + 1}. ${item.label}",
                                    style = TypographyElderlyTitle.copy(fontSize = 20.sp),
                                    color = PrimaryDarkNavy
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Terhubung sejak: ${item.connectedSinceFormatted}",
                                    style = TypographyElderlyBody.copy(fontSize = 15.sp),
                                    color = MutedTextSlate
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AccentHajjGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Status: Aktif Memantau",
                                        style = TypographyElderlyBody.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = AccentHajjGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                                OutlinedButton(
                                    onClick = { onRevokeConsent(item.trackerUserId) },
                                    enabled = !isBeingRevoked,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(HajjDimens.MinTouchTargetSize),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = StatusRedError
                                    ),
                                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                                ) {
                                    if (isBeingRevoked) {
                                        CircularProgressIndicator(
                                            color = StatusRedError,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "MENCABUT IZIN...", style = TypographyButton)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = StatusRedError,
                                            modifier = Modifier.size(HajjDimens.IconSmall)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "CABUT IZIN",
                                            style = TypographyButton,
                                            color = StatusRedError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Tombol Stop Semua Pelacakan (Minimal 56dp)
            Button(
                onClick = onStopAllTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                colors = ButtonDefaults.buttonColors(containerColor = StatusRedError),
                shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(HajjDimens.IconSmall)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STOP SEMUA PELACAKAN",
                    style = TypographyButton
                )
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Peringatan Penting
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .padding(HajjDimens.SpaceMedium)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = StatusAmberWarning,
                        modifier = Modifier.size(HajjDimens.IconSmall)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Menekan tombol stop akan seketika mematikan pengiriman posisi Anda ke seluruh keluarga terdaftar.",
                        style = TypographyElderlyBody.copy(fontSize = 15.sp),
                        color = MutedTextSlate
                    )
                }
            }
        }
    }
}
