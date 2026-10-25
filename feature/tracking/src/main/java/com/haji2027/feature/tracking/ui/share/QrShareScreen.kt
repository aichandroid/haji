package com.haji2027.feature.tracking.ui.share

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.haji2027.feature.tracking.ui.ShareQrState
import com.haji2027.feature.tracking.ui.components.QrCodeCanvas
import java.util.Locale

/**
 * Layar SCR-05: Bagikan Lokasi Saya.
 * Menampilkan QR Code pairing 5 menit dengan hitung mundur besar,
 * ketentuan privasi eksplisit, serta tombol aksi ramah lansia (>= 56dp).
 */
@Composable
fun QrShareScreen(
    shareState: ShareQrState,
    onGenerateNewCode: () -> Unit,
    onClose: () -> Unit,
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
                    onClick = onClose,
                    modifier = Modifier.size(HajjDimens.MinTouchTargetSize)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Batal",
                        tint = PrimaryDarkNavy
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "BAGIKAN LOKASI SAYA",
                    style = TypographyElderlyTitle,
                    color = PrimaryDarkNavy
                )
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Instruksi Ramah Lansia
            Text(
                text = "Mintalah keluarga (suami/istri/anak) untuk MEMINDAI kode QR di bawah ini menggunakan aplikasi mereka:",
                style = TypographyElderlyBody,
                color = MutedTextSlate,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Kontainer QR Code & Timer
            when (shareState) {
                is ShareQrState.Loading -> {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = PrimaryTrustBlue,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Menyiapkan kode QR aman...",
                                style = TypographyElderlyBody,
                                color = MutedTextSlate
                            )
                        }
                    }
                }

                is ShareQrState.Ready -> {
                    QrCodeCanvas(
                        payload = shareState.qrPayload,
                        size = 220.dp
                    )

                    Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

                    val minutes = shareState.remainingSeconds / 60
                    val seconds = shareState.remainingSeconds % 60
                    val timerColor = if (shareState.remainingSeconds <= 60) {
                        StatusRedError
                    } else {
                        PrimaryTrustBlue
                    }

                    Text(
                        text = "KODE KEDALUWARSA DALAM:",
                        style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                        color = MutedTextSlate
                    )

                    Text(
                        text = String.format(Locale.ROOT, "%02d : %02d", minutes, seconds),
                        style = TypographyElderlyTitle.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = timerColor
                    )
                }

                is ShareQrState.Expired -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(2.dp, StatusRedError, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Kedaluwarsa",
                                tint = StatusRedError,
                                modifier = Modifier.size(HajjDimens.IconLarge)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "KODE TELAH KEDALUWARSA",
                                style = TypographyElderlyTitle,
                                color = StatusRedError,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Masa berlaku 5 menit telah habis demi keamanan privasi Anda. Silakan tekan tombol 'BUAT KODE BARU' di bawah.",
                                style = TypographyElderlyBody,
                                color = MutedTextSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ShareQrState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite)
                            .border(2.dp, StatusRedError, RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .padding(HajjDimens.SpaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Gagal Menyiapkan Kode",
                                style = TypographyElderlyTitle,
                                color = StatusRedError
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = shareState.message,
                                style = TypographyElderlyBody,
                                color = MutedTextSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                ShareQrState.Idle -> {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                            .background(SurfaceWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tekan 'Buat Kode Baru'",
                            style = TypographyElderlyBody,
                            color = MutedTextSlate
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Ketentuan Privasi
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
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privasi",
                            tint = PrimaryTrustBlue,
                            modifier = Modifier.size(HajjDimens.IconSmall)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KETENTUAN PRIVASI:",
                            style = TypographyElderlyBody.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryDarkNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Kode ini hanya dapat digunakan SATU KALI.",
                        style = TypographyElderlyBody.copy(fontSize = 16.sp),
                        color = MutedTextSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Hanya orang yang memindai kode ini yang dapat melihat lokasi Anda.",
                        style = TypographyElderlyBody.copy(fontSize = 16.sp),
                        color = MutedTextSlate
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Anda dapat mencabut izin pelacakan kapan saja.",
                        style = TypographyElderlyBody.copy(fontSize = 16.sp),
                        color = MutedTextSlate
                    )
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceLarge))

            // Tombol Aksi Bawah (Minimal 56dp)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(HajjDimens.TouchTargetGap)
            ) {
                Button(
                    onClick = onGenerateNewCode,
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
                        text = "BUAT KODE BARU",
                        style = TypographyButton
                    )
                }

                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(HajjDimens.MinTouchTargetSize),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDarkNavy),
                    shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
                ) {
                    Text(
                        text = "SELESAI / TUTUP",
                        style = TypographyButton
                    )
                }
            }
        }
    }
}
