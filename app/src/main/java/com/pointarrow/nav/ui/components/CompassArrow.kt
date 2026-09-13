package com.pointarrow.nav.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CompassArrow(
    targetAngle: Float,
    isTargetReached: Boolean,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 290.dp,
    arrowColor: Color = Color(0xFF00E676),
    accentColor: Color = Color(0xFF1B5E20)
) {
    val animatedRotation = remember { Animatable(targetAngle) }

    LaunchedEffect(targetAngle) {
        var diff = (targetAngle - (animatedRotation.value % 360f)) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f

        val target = animatedRotation.value + diff
        animatedRotation.animateTo(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Canvas(modifier = modifier.size(arrowSize)) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = this.size.minDimension * 0.43f

        rotate(animatedRotation.value, pivot = Offset(centerX, centerY)) {
            val primaryNeedleColor = if (isTargetReached) Color(0xFF00E676) else arrowColor

            // Ліва грань передньої стрілки
            val frontLeft = Path().apply {
                moveTo(centerX, centerY - radius)
                lineTo(centerX - radius * 0.26f, centerY + radius * 0.12f)
                lineTo(centerX, centerY)
                close()
            }
            drawPath(frontLeft, color = primaryNeedleColor)

            // Права грань передньої стрілки (трохи темніша для 3D/рельєфності граней)
            val frontRight = Path().apply {
                moveTo(centerX, centerY - radius)
                lineTo(centerX + radius * 0.26f, centerY + radius * 0.12f)
                lineTo(centerX, centerY)
                close()
            }
            drawPath(frontRight, color = primaryNeedleColor.copy(alpha = 0.82f))

            // Хвостова противага
            val tailLeft = Path().apply {
                moveTo(centerX, centerY)
                lineTo(centerX - radius * 0.19f, centerY + radius * 0.25f)
                lineTo(centerX, centerY + radius * 0.65f)
                close()
            }
            drawPath(tailLeft, color = accentColor)

            val tailRight = Path().apply {
                moveTo(centerX, centerY)
                lineTo(centerX + radius * 0.19f, centerY + radius * 0.25f)
                lineTo(centerX, centerY + radius * 0.65f)
                close()
            }
            drawPath(tailRight, color = accentColor.copy(alpha = 0.70f))

            // Центральна шпилька
            drawCircle(color = Color.Black, radius = radius * 0.10f, center = Offset(centerX, centerY))
            drawCircle(color = primaryNeedleColor, radius = radius * 0.045f, center = Offset(centerX, centerY))
        }
    }
}
