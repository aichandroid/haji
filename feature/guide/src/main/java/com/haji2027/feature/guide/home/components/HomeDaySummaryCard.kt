package com.haji2027.feature.guide.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.model.HajjDay

/**
 * Kartu Rangkuman Ibadah Hari Ini dengan bilah progres manasik.
 */
@Composable
fun HomeDaySummaryCard(
    day: HajjDay?,
    isPeakHajj: Boolean,
    totalSteps: Int,
    completedSteps: Int,
    progressPercentage: Int,
    progressFraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        shape = RoundedCornerShape(HajjDimens.CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceWhite
        ),
        border = BorderStroke(2.dp, PrimaryTrustBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HajjDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceSmall)
        ) {
            // Header: Judul Hari & Badge Puncak Haji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day?.title ?: "Persiapan Ibadah Haji",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDarkNavy,
                    modifier = Modifier.weight(1f),
                    lineHeight = 28.sp
                )

                if (isPeakHajj) {
                    Text(
                        text = "PUNCAK HAJI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusAmberWarning,
                        modifier = Modifier
                            .background(
                                color = StatusAmberWarning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Lokasi
            day?.location?.let { loc ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Lokasi",
                        tint = PrimaryTrustBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Lokasi: $loc",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedTextSlate
                    )
                }
            }

            // Status Progres Checklist
            Text(
                text = "Progres Ibadah Hari Ini: $completedSteps dari $totalSteps Langkah Selesai",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryDarkNavy,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Bilah Progres
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HajjDimens.SpaceSmall)
            ) {
                LinearProgressIndicator(
                    progress = { progressFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp)),
                    color = AccentHajjGreen,
                    trackColor = BorderSubtle
                )

                Text(
                    text = "$progressPercentage%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentHajjGreen
                )
            }
        }
    }
}
