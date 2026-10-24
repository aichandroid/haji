package com.haji2027.feature.guide.detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.StatusRedError
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.model.ManasikStep

/**
 * Kartu satu langkah manasik dengan tombol doa, catatan ikhtilaf, dan tombol centang selesai.
 */
@Composable
fun StepItem(
    step: ManasikStep,
    isCompleted: Boolean,
    onToggleComplete: (stepId: String, isCompleted: Boolean) -> Unit,
    onNavigateToDoa: (readingId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) AccentHajjGreen.copy(alpha = 0.04f) else SurfaceWhite
        ),
        border = BorderStroke(
            width = if (isCompleted) 1.5.dp else 1.dp,
            color = if (isCompleted) AccentHajjGreen else BorderSubtle
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HajjDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceSmall)
        ) {
            // Header: Nomor Langkah & Instruksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Badge Angka Langkah
                Text(
                    text = "${step.order}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite,
                    modifier = Modifier
                        .background(
                            color = if (isCompleted) AccentHajjGreen else PrimaryTrustBlue,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )

                // Instruksi Manasik
                Text(
                    text = step.instruction,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDarkNavy,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Larangan-larangan (Jika Ada)
            if (step.prohibitions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = StatusRedError.copy(alpha = 0.06f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(HajjDimens.SpaceSmall),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Larangan Terkait:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusRedError
                    )
                    for (prohib in step.prohibitions) {
                        Text(
                            text = "• $prohib",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = PrimaryDarkNavy,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Catatan Perbedaan Fikih (Ikhtilaf)
            step.differencesNote?.let { note ->
                IkhtilafWarningBox(note = note)
            }

            // Tombol Bacaan Doa (Jika Ada Doa Terkait Langkah Ini)
            if (step.readings.isNotEmpty()) {
                for (reading in step.readings) {
                    OutlinedButton(
                        onClick = { onNavigateToDoa(reading.readingId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = HajjDimens.MinTouchTargetSize),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, PrimaryTrustBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PrimaryTrustBlue.copy(alpha = 0.05f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = PrimaryTrustBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "BACA DOA: ${reading.title}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTrustBlue
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Buka Doa",
                                tint = PrimaryTrustBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Tombol Centang Checklist Selesai (Target Sentuh >= 56dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = HajjDimens.MinTouchTargetSize)
                    .clickable(
                        role = Role.Checkbox,
                        onClick = { onToggleComplete(step.stepId, isCompleted) }
                    )
                    .background(
                        color = if (isCompleted) AccentHajjGreen.copy(alpha = 0.1f) else SurfaceWhite,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (isCompleted) "Selesai" else "Belum Selesai",
                    tint = if (isCompleted) AccentHajjGreen else MutedTextSlate,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = if (isCompleted) "Langkah Ini Selesai" else "Tandai Langkah Ini Selesai",
                    fontSize = 16.sp,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCompleted) AccentHajjGreen else PrimaryDarkNavy
                )
            }
        }
    }
}
