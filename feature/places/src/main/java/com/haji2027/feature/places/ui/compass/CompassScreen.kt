package com.haji2027.feature.places.ui.compass

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.BackgroundSoftSky
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.feature.places.domain.NavigationAccuracyStatus
import com.haji2027.feature.places.ui.components.AccuracyStatusBadge
import com.haji2027.feature.places.ui.components.CalibrationGuideDialog
import com.haji2027.feature.places.ui.components.RotatingCompassArrow

/**
 * Layar utama kompas navigasi panah spasial (SCR-04).
 * Memandu jemaah kembali ke pintu, hotel, atau tenda menggunakan panah putar garis lurus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassScreen(
    viewModel: CompassViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isTargetDeleted) {
        if (uiState.isTargetDeleted) {
            onBackClick()
        }
    }

    val targetName = uiState.targetPlace?.name ?: "TEMPAT SAYA"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MENUJU: ${targetName.uppercase()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = PrimaryDarkNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundSoftSky
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lencana Status Akurasi Sensor (FR-PLACE-005)
            val accuracyStatus = uiState.navigationBearing?.accuracyStatus
                ?: NavigationAccuracyStatus.DEGRADED

            AccuracyStatusBadge(
                status = accuracyStatus,
                gpsAccuracyMeters = uiState.currentLocation?.accuracyMeters,
                onCalibrationClick = { viewModel.openCalibrationDialog() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Jarum Panah Navigasi Raksasa (SCR-04)
            RotatingCompassArrow(
                targetRelativeAngleDegrees = uiState.navigationBearing?.relativeArrowAngleDegrees ?: 0f,
                size = HajjDimens.CompassSize
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Indikator Jarak Garis Lurus
            Text(
                text = "JARAK GARIS LURUS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MutedTextSlate,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isWaitingForGps && uiState.navigationBearing == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = PrimaryDarkNavy
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mengunci sinyal satelit...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryDarkNavy
                    )
                }
            } else {
                Text(
                    text = uiState.navigationBearing?.formattedDistance?.uppercase() ?: "-- METER",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryDarkNavy
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Kotak Disclaimer Garis Lurus Permanen (FR-PLACE-007)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .background(SurfaceWhite)
                    .border(1.5.dp, StatusAmberWarning.copy(alpha = 0.5f), RoundedCornerShape(HajjDimens.CardCornerRadius))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = StatusAmberWarning,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Panah menunjukkan GARIS LURUS fisik, bukan rute jalan kaki. Perhatikan tembok, tangga, pembatas jalan, dan kepadatan kerumunan.",
                        fontSize = 13.sp,
                        color = PrimaryDarkNavy,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Catatan Tempat (Jika ada)
            if (!uiState.targetPlace?.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .background(SurfaceWhite)
                        .border(1.5.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryDarkNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Catatan Tempat:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDarkNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.targetPlace!!.notes!!,
                                fontSize = 14.sp,
                                color = MutedTextSlate,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dua Tombol Aksi Bawah: Kalibrasi Kompas & Hapus Tempat (Tinggi >= 56dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HajjDimens.TouchTargetGap)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openCalibrationDialog() },
                    modifier = Modifier
                        .weight(1f)
                        .height(HajjDimens.MinTouchTargetSize),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = PrimaryDarkNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "KALIBRASI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkNavy
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.openDeleteDialog() },
                    modifier = Modifier
                        .weight(1f)
                        .height(HajjDimens.MinTouchTargetSize),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRedError)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = StatusRedError,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HAPUS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusRedError
                    )
                }
            }
        }
    }

    // Modal Panduan Kalibrasi Angka 8
    if (uiState.showCalibrationDialog) {
        CalibrationGuideDialog(
            onDismiss = { viewModel.dismissCalibrationDialog() }
        )
    }

    // Modal Konfirmasi Hapus Tempat
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "Hapus Tempat Ini?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkNavy
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus \"$targetName\" dari daftar tempat tersimpan?",
                    fontSize = 14.sp,
                    color = MutedTextSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeletePlace() },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedError),
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("YA, HAPUS", fontWeight = FontWeight.Bold, color = SurfaceWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("BATAL", color = PrimaryDarkNavy)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
        )
    }
}
