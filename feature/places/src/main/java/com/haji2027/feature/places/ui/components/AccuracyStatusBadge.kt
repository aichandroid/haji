package com.haji2027.feature.places.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haji2027.core.designsystem.theme.CompassAccuracyFair
import com.haji2027.core.designsystem.theme.CompassAccuracyGood
import com.haji2027.core.designsystem.theme.CompassAccuracyPoor
import com.haji2027.core.designsystem.theme.StatusAmberWarning
import com.haji2027.feature.places.domain.NavigationAccuracyStatus

/**
 * Komponen lencana penunjuk kejujuran status sensor & akurasi GPS (FR-PLACE-005).
 */
@Composable
fun AccuracyStatusBadge(
    status: NavigationAccuracyStatus,
    gpsAccuracyMeters: Float? = null,
    onCalibrationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (status) {
        NavigationAccuracyStatus.HIGH -> {
            val accText = if (gpsAccuracyMeters != null) " (${gpsAccuracyMeters.toInt()}m)" else ""
            Quadruple(
                CompassAccuracyGood.copy(alpha = 0.15f),
                CompassAccuracyGood,
                Icons.Default.GpsFixed,
                "AKURASI GPS BAIK$accText"
            )
        }
        NavigationAccuracyStatus.MODERATE -> {
            val accText = if (gpsAccuracyMeters != null) " (${gpsAccuracyMeters.toInt()}m)" else ""
            Quadruple(
                CompassAccuracyFair.copy(alpha = 0.15f),
                CompassAccuracyFair,
                Icons.Default.GpsFixed,
                "AKURASI SEDANG$accText"
            )
        }
        NavigationAccuracyStatus.DEGRADED -> {
            val accText = if (gpsAccuracyMeters != null) " (>30m)" else ""
            Quadruple(
                CompassAccuracyPoor.copy(alpha = 0.15f),
                CompassAccuracyPoor,
                Icons.Default.GpsNotFixed,
                "AKURASI RENDAH / DALAM GEDUNG$accText"
            )
        }
        NavigationAccuracyStatus.CALIBRATION_NEEDED -> {
            Quadruple(
                StatusAmberWarning.copy(alpha = 0.18f),
                StatusAmberWarning,
                Icons.Default.Warning,
                "SENSOR PERLU KALIBRASI (KETUK DI SINI)"
            )
        }
    }

    val clickableModifier = if (status == NavigationAccuracyStatus.CALIBRATION_NEEDED) {
        Modifier.clickable { onCalibrationClick() }
    } else Modifier

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.5.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .then(clickableModifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
