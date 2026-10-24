package com.haji2027.feature.guide.detail.components

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
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.MutedTextSlate
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.PrimaryTrustBlue
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.core.model.ManasikActivity
import com.haji2027.core.model.RulingCategory

/**
 * Kartu Pengelompok Kegiatan Manasik beserta status hukum dan langkah-langkahnya.
 */
@Composable
fun ActivityCard(
    activity: ManasikActivity,
    completedStepIds: Set<String>,
    onToggleComplete: (stepId: String, isCompleted: Boolean) -> Unit,
    onNavigateToDoa: (readingId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HajjDimens.SpaceSmall),
        verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceSmall)
    ) {
        // Baris Header Aktivitas: Judul & Badge Hukum
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activity.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDarkNavy,
                modifier = Modifier.weight(1f)
            )

            // Badge Status Hukum
            val (badgeBg, badgeText) = when (activity.rulingCategory) {
                RulingCategory.RUKUN -> StatusAmberWarning to "RUKUN"
                RulingCategory.WAJIB -> PrimaryTrustBlue to "WAJIB"
                RulingCategory.SUNAH -> AccentHajjGreen to "SUNAH"
            }

            Text(
                text = badgeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite,
                modifier = Modifier
                    .background(color = badgeBg, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Catatan Waktu & Lokasi
        val timeOrLoc = listOfNotNull(
            activity.timeWindow?.let { "Waktu: $it" },
            activity.locationNotes?.let { "Tempat: $it" }
        ).joinToString(" • ")

        if (timeOrLoc.isNotBlank()) {
            Text(
                text = timeOrLoc,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MutedTextSlate
            )
        }

        // Daftar Langkah-Langkah di dalam Aktivitas
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HajjDimens.SpaceSmall)
        ) {
            for (step in activity.steps) {
                StepItem(
                    step = step,
                    isCompleted = completedStepIds.contains(step.stepId),
                    onToggleComplete = onToggleComplete,
                    onNavigateToDoa = onNavigateToDoa
                )
            }
        }
    }
}
