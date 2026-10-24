package com.haji2027.feature.guide.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.model.DifferencesNote

/**
 * Kotak informasi catatan ikhtilaf fikih yang netral dan menenangkan jemaah.
 */
@Composable
fun IkhtilafWarningBox(
    note: DifferencesNote,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = StatusAmberWarning.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = StatusAmberWarning.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(HajjDimens.SpaceMedium),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Catatan Ikhtilaf",
                tint = StatusAmberWarning,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "Catatan Ikhtilaf Fikih:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StatusAmberWarning
            )
        }

        Text(
            text = note.summary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = PrimaryDarkNavy,
            lineHeight = 22.sp
        )

        if (note.opinions.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (opinion in note.opinions) {
                    Text(
                        text = "• ${opinion.sourceRef}: ${opinion.opinion}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryDarkNavy.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
