package com.pointpointer.nav.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CompassArrow(
    targetAngle: Float,
    isTargetReached: Boolean,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 290.dp,
    arrowColor: Color = Color(0xFF00E5FF),
    accentColor: Color = Color(0xFF7C4DFF)
) {
    val animatedRotation = remember { Animatable(targetAngle) }

    LaunchedEffect(targetAngle) {
        val currentAngle = animatedRotation.value
        val delta = ((targetAngle - currentAngle + 540f) % 360f) - 180f
        val newTarget = currentAngle + delta

        animatedRotation.animateTo(
            targetValue = newTarget,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    Box(
        modifier = modifier.size(arrowSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCompassDial(
                outerCircleColor = Color.White.copy(alpha = 0.12f),
                tickColor = Color.White.copy(alpha = 0.25f),
                subTickColor = Color.White.copy(alpha = 0.08f)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = animatedRotation.value
                }
        ) {
            val centerOffset = this.center
            val radius = this.size.minDimension * 0.43f

            val primaryNeedleColor = if (isTargetReached) Color(0xFF00E676) else arrowColor

            val frontLeft = Path().apply {
                moveTo(centerOffset.x, centerOffset.y - radius)
                lineTo(centerOffset.x - radius * 0.26f, centerOffset.y + radius * 0.12f)
                lineTo(centerOffset.x, centerOffset.y - radius * 0.05f)
                close()
            }
            drawPath(frontLeft, color = primaryNeedleColor)

            val frontRight = Path().apply {
                moveTo(centerOffset.x, centerOffset.y - radius)
                lineTo(centerOffset.x + radius * 0.26f, centerOffset.y + radius * 0.12f)
                lineTo(centerOffset.x, centerOffset.y - radius * 0.05f)
                close()
            }
            drawPath(frontRight, color = primaryNeedleColor.copy(alpha = 0.78f))

            val tailLeft = Path().apply {
                moveTo(centerOffset.x, centerOffset.y - radius * 0.05f)
                lineTo(centerOffset.x - radius * 0.20f, centerOffset.y + radius * 0.18f)
                lineTo(centerOffset.x, centerOffset.y + radius * 0.65f)
                close()
            }
            drawPath(tailLeft, color = accentColor.copy(alpha = 0.5f))

            val tailRight = Path().apply {
                moveTo(centerOffset.x, centerOffset.y - radius * 0.05f)
                lineTo(centerOffset.x + radius * 0.20f, centerOffset.y + radius * 0.18f)
                lineTo(centerOffset.x, centerOffset.y + radius * 0.65f)
                close()
            }
            drawPath(tailRight, color = accentColor.copy(alpha = 0.32f))

            drawCircle(
                color = Color.Black,
                radius = radius * 0.09f,
                center = centerOffset
            )
            drawCircle(
                color = primaryNeedleColor,
                radius = radius * 0.05f,
                center = centerOffset
            )
        }
    }
}

private fun DrawScope.drawCompassDial(
    outerCircleColor: Color,
    tickColor: Color,
    subTickColor: Color
) {
    val centerOffset = this.center
    val radius = this.size.minDimension / 2f
    val majorTickLength = radius * 0.075f
    val minorTickLength = radius * 0.038f

    drawCircle(
        color = outerCircleColor,
        radius = radius - 2.dp.toPx(),
        style = Stroke(width = 1.5.dp.toPx())
    )

    for (deg in 0 until 360 step 10) {
        val angleRad = Math.toRadians(deg.toDouble())
        val isMajor = deg % 30 == 0
        val tickLen = if (isMajor) majorTickLength else minorTickLength
        val color = if (isMajor) tickColor else subTickColor

        val startX = (centerOffset.x + (radius - tickLen) * Math.sin(angleRad)).toFloat()
        val startY = (centerOffset.y - (radius - tickLen) * Math.cos(angleRad)).toFloat()
        val endX = (centerOffset.x + radius * Math.sin(angleRad)).toFloat()
        val endY = (centerOffset.y - radius * Math.cos(angleRad)).toFloat()

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
