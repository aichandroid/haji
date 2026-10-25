package com.haji2027.feature.settings.battery

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.designsystem.theme.TypographyButton
import com.haji2027.core.designsystem.theme.TypographyElderlyBody
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle

/**
 * Kartu panduan konfigurasi baterai OEM (Xiaomi, Oppo, Vivo, Samsung, dll).
 * Mengimplementasikan FR-ONB-004 dan NFR-BAT-001 dengan target sentuh lansia >= 56dp.
 */
@Composable
fun BatteryWhitelistCard(
    modifier: Modifier = Modifier,
    manufacturer: String = Build.MANUFACTURER,
    brand: String = Build.BRAND
) {
    val context = LocalContext.current
    val vendor = remember(manufacturer, brand) {
        OemVendor.fromManufacturer(manufacturer, brand)
    }

    // Periksa status pengecualian optimasi baterai sistem
    var isOptimizedIgnored by remember {
        mutableStateOf(OemBatteryWhitelistHelper.isIgnoringBatteryOptimizations(context))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HajjDimens.CardCornerRadius))
            .border(1.dp, BorderSubtle, RoundedCornerShape(HajjDimens.CardCornerRadius)),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HajjDimens.SpaceMedium)
        ) {
            // Header Bar & Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isOptimizedIgnored) AccentHajjGreen.copy(alpha = 0.15f) else StatusAmberWarning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOptimizedIgnored) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = if (isOptimizedIgnored) AccentHajjGreen else StatusAmberWarning,
                        modifier = Modifier.size(HajjDimens.IconMedium)
                    )
                }

                Spacer(modifier = Modifier.width(HajjDimens.SpaceMedium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "KETAHANAN BATERAI & LACAK",
                        style = TypographyElderlyTitle.copy(fontSize = 18.sp),
                        color = PrimaryDarkNavy
                    )
                    Text(
                        text = "Ponsel Terdeteksi: ${vendor.displayName}",
                        style = TypographyElderlyBody.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        color = PrimaryTrustBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceSmall))

            // Status Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isOptimizedIgnored) AccentHajjGreen.copy(alpha = 0.1f) else StatusAmberWarning.copy(alpha = 0.12f))
                    .border(
                        1.dp,
                        if (isOptimizedIgnored) AccentHajjGreen.copy(alpha = 0.3f) else StatusAmberWarning.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOptimizedIgnored) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isOptimizedIgnored) AccentHajjGreen else StatusAmberWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOptimizedIgnored) {
                            "Status: AKTIF (Aplikasi bebas pembatasan latar belakang)"
                        } else {
                            "Status: PERLU DIATUR (Beresiko dimatikan otomatis oleh sistem)"
                        },
                        style = TypographyElderlyBody.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOptimizedIgnored) AccentHajjGreen else StatusAmberWarning
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceSmall))

            // Edukasi Singkat Ramah Lansia
            Text(
                text = "Sistem ponsel sering mematikan pelacakan lokasi keluarga saat layar mati untuk menghemat daya. Ikuti langkah di bawah ini agar pelacakan darurat tetap berjalan lancar:",
                style = TypographyElderlyBody.copy(fontSize = 14.sp),
                color = MutedTextSlate
            )

            Spacer(modifier = Modifier.height(HajjDimens.SpaceSmall))

            // Kotak Langkah-demi-Langkah Spesifik Vendor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryDarkNavy.copy(alpha = 0.04f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                vendor.instructions.forEach { stepText ->
                    Text(
                        text = stepText,
                        style = TypographyElderlyBody.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                        color = PrimaryDarkNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(HajjDimens.SpaceMedium))

            // Tombol Aksi Raksasa Lansia (>= 56dp)
            Button(
                onClick = {
                    OemBatteryWhitelistHelper.openBatteryOptimization(context, vendor)
                    // Refresh status setelah kembali
                    isOptimizedIgnored = OemBatteryWhitelistHelper.isIgnoringBatteryOptimizations(context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HajjDimens.MinTouchTargetSize),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTrustBlue),
                shape = RoundedCornerShape(HajjDimens.CardCornerRadius)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(HajjDimens.IconSmall)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BUKA PENGATURAN BATERAI PONSEL",
                    style = TypographyButton
                )
            }
        }
    }
}
