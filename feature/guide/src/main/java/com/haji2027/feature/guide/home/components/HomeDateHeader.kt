package com.haji2027.feature.guide.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.StatusAmberWarning

/**
 * Header status tanggal di Tanah Suci (Hijriah & Masehi).
 */
@Composable
fun HomeDateHeader(
    hijriDateFormatted: String,
    gregorianDateFormatted: String,
    appliedOffsetDays: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HajjDimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HARI INI DI TANAH SUCI:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MutedTextSlate,
                letterSpacing = 1.sp
            )

            if (appliedOffsetDays != 0) {
                val offsetSign = if (appliedOffsetDays > 0) "+$appliedOffsetDays" else "$appliedOffsetDays"
                Text(
                    text = "Offset: $offsetSign Hari",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusAmberWarning,
                    modifier = Modifier
                        .background(
                            color = StatusAmberWarning.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = if (hijriDateFormatted.isNotBlank()) {
                "$hijriDateFormatted / $gregorianDateFormatted"
            } else {
                gregorianDateFormatted
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkNavy,
            lineHeight = 26.sp
        )
    }
}
