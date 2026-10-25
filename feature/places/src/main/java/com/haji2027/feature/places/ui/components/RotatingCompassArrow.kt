package com.haji2027.feature.places.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haji2027.core.designsystem.theme.AccentHajjGreen
import com.haji2027.core.designsystem.theme.BorderSubtle
import com.haji2027.core.designsystem.theme.HajjDimens
import com.haji2027.core.designsystem.theme.PrimaryDarkNavy
import com.haji2027.core.designsystem.theme.SurfaceWhite
import com.haji2027.feature.places.domain.BearingCalculator

/**
 * Jarum panah navigasi spasial raksasa ramah lansia.
 * Berotasi halus mengarah ke target tempat tersimpan dengan kompensasi sudut sirkular.
 */
@Composable
fun RotatingCompassArrow(
    targetRelativeAngleDegrees: Float,
    modifier: Modifier = Modifier,
    size: Dp = HajjDimens.CompassSize
) {
    // Akumulator sudut kontinu untuk mencegah lompatan 359° <-> 0° saat beranimasi
    var accumulatedAngle by remember { mutableFloatStateOf(targetRelativeAngleDegrees) }
    var previousTargetAngle by remember { mutableFloatStateOf(targetRelativeAngleDegrees) }

    if (targetRelativeAngleDegrees != previousTargetAngle) {
        val diff = BearingCalculator.shortestAngleDiff(previousTargetAngle, targetRelativeAngleDegrees)
        accumulatedAngle += diff
        previousTargetAngle = targetRelativeAngleDegrees
    }

    val animatedRotation by animateFloatAsState(
        targetValue = accumulatedAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CompassRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SurfaceWhite)
            .border(4.dp, BorderSubtle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Dial lingkaran kompas statis (indikator garis luar)
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) - 16.dp.toPx()

            // Titik penanda 4 arah mata angin pada bezel
            val tickRadius = radius + 6.dp.toPx()
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val startX = (center.x + (radius * Math.sin(rad))).toFloat()
                val startY = (center.y - (radius * Math.cos(rad))).toFloat()
                val endX = (center.x + (tickRadius * Math.sin(rad))).toFloat()
                val endY = (center.y - (tickRadius * Math.cos(rad))).toFloat()

                drawLine(
                    color = if (angle % 90 == 0) PrimaryDarkNavy else BorderSubtle,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (angle % 90 == 0) 4.dp.toPx() else 2.dp.toPx()
                )
            }
        }

        // Jarum panah yang berotasi dinamis
        Canvas(
            modifier = Modifier
                .size(size)
                .rotate(animatedRotation)
        ) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val arrowLength = (size.toPx() / 2f) - 24.dp.toPx()
            val arrowHalfWidth = 24.dp.toPx()
            val tailLength = arrowLength * 0.45f

            // 1. Ujung panah depan (Menunjuk ke target, warna hijau zamrud kontras)
            val frontPath = Path().apply {
                moveTo(center.x, center.y - arrowLength) // Puncak panah runcing
                lineTo(center.x + arrowHalfWidth, center.y) // Sayap kanan
                lineTo(center.x, center.y - 12.dp.toPx()) // Lekuk dalam tengah
                lineTo(center.x - arrowHalfWidth, center.y) // Sayap kiri
                close()
            }
            drawPath(frontPath, color = AccentHajjGreen)

            // Garis bayangan sisi kanan panah depan untuk efek dimensi visual yang jelas
            val frontRightPath = Path().apply {
                moveTo(center.x, center.y - arrowLength)
                lineTo(center.x + arrowHalfWidth, center.y)
                lineTo(center.x, center.y - 12.dp.toPx())
                close()
            }
            drawPath(frontRightPath, color = AccentHajjGreen.copy(alpha = 0.8f))

            // 2. Ekor panah belakang (Abu-abu gelap tegas)
            val tailPath = Path().apply {
                moveTo(center.x, center.y - 12.dp.toPx())
                lineTo(center.x + (arrowHalfWidth * 0.7f), center.y)
                lineTo(center.x, center.y + tailLength) // Ujung ekor
                lineTo(center.x - (arrowHalfWidth * 0.7f), center.y)
                close()
            }
            drawPath(tailPath, color = BorderSubtle)

            // 3. Titik tumpu poros tengah (Center hub)
            drawCircle(
                color = PrimaryDarkNavy,
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = SurfaceWhite,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
