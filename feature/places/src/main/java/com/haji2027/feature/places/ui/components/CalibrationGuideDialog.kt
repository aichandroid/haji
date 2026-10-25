package com.haji2027.feature.places.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.SurfaceWhite

/**
 * Modal dialog panduan kalibrasi sensor kompas magnetometer dengan gerakan angka 8 (FR-PLACE-006).
 */
@Composable
fun CalibrationGuideDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(StatusAmberWarning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = StatusAmberWarning,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kalibrasi Kompas",
                    color = PrimaryDarkNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sensor kompas ponsel Anda mendeteksi gangguan medan magnet (misalnya tiang besi, eskalator, atau beton tebal).",
                    color = MutedTextSlate,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .background(BackgroundSoftSky)
                        .border(1.5.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Langkah Mudah Kalibrasi:",
                            color = PrimaryDarkNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Pegang ponsel tegak di depan dada Anda.",
                            color = PrimaryDarkNavy,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Ayunkan ponsel perlahan membentuk angka 8 (simbol ∞) di udara sebanyak 3 kali.",
                            color = PrimaryDarkNavy,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. Lencana akurasi akan otomatis berubah menjadi hijau jika sensor sudah kembali normal.",
                            color = PrimaryDarkNavy,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentHajjGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SAYA SUDAH MENGERTI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite
                )
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
    )
}
