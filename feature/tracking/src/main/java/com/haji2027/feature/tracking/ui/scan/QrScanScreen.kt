package com.haji2027.feature.tracking.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.designsystem.theme.TypographyButton
import com.haji2027.core.designsystem.theme.TypographyElderlyBody
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle
import com.haji2027.feature.tracking.ui.ClaimQrState

/**
 * Layar pemindaian atau input teks QR token pasangan pelacakan keluarga.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrScanScreen(
    claimState: ClaimQrState,
    onClaimToken: (qrInput: String, label: String) -> Unit,
    onResetClaimState: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var qrInput by remember { mutableStateOf("") }
    var trackerLabel by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val quickLabels = listOf("Suami", "Istri", "Anak Perempuan", "Anak Laki-laki", "Orang Tua")

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
                    text = "PINDAI QR KELUARGA",
                    style = TypographyElderlyTitle,
                    color = PrimaryDarkNavy
                )
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            if (claimState is ClaimQrState.Success) {
                // Tampilan Sukses Pairing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .background(SurfaceWhite)
                        .border(2.dp, AccentHajjGreen, RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .padding(HajjDimens.SpaceLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sukses",
                            tint = AccentHajjGreen,
                            modifier = Modifier.size(HajjDimens.IconHero)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PELACAKAN TERHUBUNG!",
                            style = TypographyElderlyTitle,
                            color = AccentHajjGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = claimState.message,
                            style = TypographyElderlyBody,
                            color = MutedTextSlate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ID Target: ${claimState.targetUserId}",
                            style = TypographyElderlyBody.copy(fontSize = 14.sp),
                            color = MutedTextSlate
                        )

                        Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

                        Button(
                            onClick = {
                                onResetClaimState()
                                onBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(HajjDimens.MinTouchTargetSize),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                            shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                        ) {
                            Text(
                                text = "KEMBALI KE MENU UTAMA",
                                style = TypographyButton
                            )
                        }
                    }
                }
            } else {
                // Tampilan Formulir Input Klaim QR
                Text(
                    text = "Tempelkan kode QR jemaah (skema hajj2027://pair?...) dan tentukan hubungan keluarga untuk mulai memantau lokasi:",
                    style = TypographyElderlyBody,
                    color = MutedTextSlate,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

                OutlinedTextField(
                    value = qrInput,
                    onValueChange = { qrInput = it },
                    label = { Text("Teks Kode Pasangan / QR", style = TypographyElderlyBody) },
                    placeholder = { Text("hajj2027://pair?t=...&u=...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                )

                Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                OutlinedTextField(
                    value = trackerLabel,
                    onValueChange = { trackerLabel = it },
                    label = { Text("Label Hubungan Keluarga", style = TypographyElderlyBody) },
                    placeholder = { Text("Contoh: Istri, Fatimah (Anak)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tombol Cepat Pilihan Label (Memudahkan Lansia)
                Text(
                    text = "Pilihan cepat label:",
                    style = TypographyElderlyBody.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = MutedTextSlate,
                    modifier = Modifier.fillMaxWidth()
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickLabels.forEach { label ->
                        FilterChip(
                            selected = trackerLabel == label,
                            onClick = { trackerLabel = label },
                            label = { Text(label) }
                        )
                    }
                }

                if (claimState is ClaimQrState.Error) {
                    Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(1.dp, StatusRedError, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceMedium)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = StatusRedError,
                                modifier = Modifier.size(HajjDimens.IconSmall)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = claimState.message,
                                style = TypographyElderlyBody.copy(fontSize = 16.sp),
                                color = StatusRedError
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

                Button(
                    onClick = { onClaimToken(qrInput, trackerLabel) },
                    enabled = claimState !is ClaimQrState.Loading && qrInput.isNotBlank() && trackerLabel.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTrustBlue),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    if (claimState is ClaimQrState.Loading) {
                        CircularProgressIndicator(
                            color = SurfaceWhite,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "MEMPROSES...", style = TypographyButton)
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(HajjDimens.IconSmall)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HUBUNGKAN PELACAKAN",
                            style = TypographyButton
                        )
                    }
                }
            }
        }
    }
}
