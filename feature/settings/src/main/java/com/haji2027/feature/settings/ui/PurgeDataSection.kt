package com.haji2027.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.haji2027.feature.settings.purge.PurgeStep
import com.haji2027.feature.settings.purge.PurgeViewModel

/**
 * Komponen UI untuk eksekusi Right to be Forgotten (FR-DIST-003, FR-DIST-004, TC-PRIV-01).
 * Mengimplementasikan dialog konfirmasi 2-langkah dengan target sentuh ramah lansia (>= 56dp).
 */
@Composable
fun PurgeDataSection(
    viewModel: PurgeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "PRIVASI & HAK SUBJEK DATA",
            style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
            color = PrimaryDarkNavy
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Kartu Penjelasan Right to be Forgotten
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                .background(SurfaceWhite)
                .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                .padding(HajjDimens.SpaceMedium)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = StatusRedError,
                        modifier = Modifier.size(HajjDimens.IconMedium)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Seluruh Data Saya",
                        style = TypographyElderlyTitle.copy(fontSize = 18.sp),
                        color = StatusRedError
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Menghapus tuntas data tempat tersimpan, checklist manasik, akun anonim, relasi pelacakan, dan riwayat telemetri di server tanpa sisa (Right to be Forgotten).",
                    style = TypographyElderlyBody.copy(fontSize = 14.sp),
                    color = MutedTextSlate
                )

                Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                Button(
                    onClick = { viewModel.requestPurgeStep1() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedError),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HAPUS SELURUH DATA SAYA",
                        style = TypographyButton
                    )
                }
            }
        }
    }

    // Dialog Langkah 1: Peringatan Konsekuensi
    if (uiState.step is PurgeStep.DialogStep1) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelPurge() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAmberWarning,
                    modifier = Modifier.size(HajjDimens.IconLarge)
                )
            },
            title = {
                Text(
                    text = "Hapus Seluruh Data?",
                    style = TypographyElderlyTitle,
                    color = PrimaryDarkNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tindakan ini akan menghapus data berikut secara permanen:",
                        style = TypographyElderlyBody,
                        color = MutedTextSlate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• Seluruh tempat tersimpan (hotel, tenda, pintu)", style = TypographyElderlyBody.copy(fontSize = 15.sp), color = PrimaryDarkNavy)
                    Text(text = "• Seluruh centang checklist manasik", style = TypographyElderlyBody.copy(fontSize = 15.sp), color = PrimaryDarkNavy)
                    Text(text = "• Relasi pelacakan keluarga di Cloud Firestore", style = TypographyElderlyBody.copy(fontSize = 15.sp), color = PrimaryDarkNavy)
                    Text(text = "• Sesi akun anonim dan antrean koordinat", style = TypographyElderlyBody.copy(fontSize = 15.sp), color = PrimaryDarkNavy)
                    Text(text = "• Berkas cache pembaruan materi OTA", style = TypographyElderlyBody.copy(fontSize = 15.sp), color = PrimaryDarkNavy)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.proceedToStep2() },
                    modifier = Modifier.height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAmberWarning),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text("LANJUTKAN KE KONFIRMASI", style = TypographyButton)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelPurge() },
                    modifier = Modifier.height(HajjDimens.MinTouchTargetSize),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text("BATAL", style = TypographyButton, color = PrimaryDarkNavy)
                }
            }
        )
    }

    // Dialog Langkah 2: Konfirmasi Final Permanen (TC-PRIV-01)
    if (uiState.step is PurgeStep.DialogStep2) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelPurge() },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = StatusRedError,
                    modifier = Modifier.size(HajjDimens.IconLarge)
                )
            },
            title = {
                Text(
                    text = "KONFIRMASI FINAL",
                    style = TypographyElderlyTitle,
                    color = StatusRedError
                )
            },
            text = {
                Text(
                    text = "Apakah Anda benar-benar yakin ingin menghapus seluruh data Anda sekarang? Tindakan ini permanen dan TIDAK DAPAT DIBATALKAN.",
                    style = TypographyElderlyBody,
                    color = MutedTextSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.executeFinalPurge() },
                    modifier = Modifier.height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedError),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text("YA, HAPUS PERMANEN", style = TypographyButton)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelPurge() },
                    modifier = Modifier.height(HajjDimens.MinTouchTargetSize),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text("BATAL", style = TypographyButton, color = PrimaryDarkNavy)
                }
            }
        )
    }

    // Dialog Proses Penghapusan Berjalan
    if (uiState.step is PurgeStep.Purging) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Membersihkan Data...", style = TypographyElderlyTitle) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = StatusRedError, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Menghapus basis data lokal, akun, dan dokumen cloud server...",
                        style = TypographyElderlyBody,
                        color = MutedTextSlate
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Dialog Sukses Pembersihan Tuntas
    if (uiState.step is PurgeStep.Success) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissFeedback() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentHajjGreen,
                    modifier = Modifier.size(HajjDimens.IconLarge)
                )
            },
            title = {
                Text(
                    text = "DATA BERHASIL DIHAPUS",
                    style = TypographyElderlyTitle,
                    color = AccentHajjGreen
                )
            },
            text = {
                Text(
                    text = uiState.feedbackMessage ?: "Seluruh data Anda telah berhasil dibersihkan secara tuntas.",
                    style = TypographyElderlyBody,
                    color = MutedTextSlate
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissFeedback() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text("SELESAI", style = TypographyButton)
                }
            }
        )
    }
}
