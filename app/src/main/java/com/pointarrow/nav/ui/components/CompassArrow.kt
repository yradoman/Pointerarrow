package com.pointarrow.nav.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CompassArrow(
    targetAngle: Float,
    isTargetReached: Boolean,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 280.dp,
    arrowColor: Color = Color(0xFF00E676),
    accentColor: Color = Color(0xFF00C853)
) {
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 100),
        label = "ArrowRotation"
    )

    Canvas(modifier = modifier.size(arrowSize)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = (minOf(w, h) / 2f) - 10f

        // Зовнішнє фонове кільце компаса (AMOLED Dark)
        drawCircle(
            color = Color(0xFF111111),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 3.dp.toPx())
        )

        // 12 секторних маркерів на лімбі
        for (i in 0 until 12) {
            val tickAngle = i * 30f
            val isCardinal = i % 3 == 0
            val tickLength = if (isCardinal) 14.dp.toPx() else 8.dp.toPx()
            val tickColor = if (isCardinal) Color(0xFF444444) else Color(0xFF222222)
            val strokeW = if (isCardinal) 2.5.dp.toPx() else 1.5.dp.toPx()

            rotate(tickAngle, pivot = Offset(cx, cy)) {
                drawLine(
                    color = tickColor,
                    start = Offset(cx, cy - radius),
                    end = Offset(cx, cy - radius + tickLength),
                    strokeWidth = strokeW
                )
            }
        }

        // Центральна стрілка
        rotate(degrees = animatedAngle, pivot = Offset(cx, cy)) {
            val arrowLength = radius * 0.88f
            val halfBaseWidth = radius * 0.38f
            val notchDepth = radius * 0.22f

            val tipY = cy - arrowLength
            val leftX = cx - halfBaseWidth
            val rightX = cx + halfBaseWidth
            val baseY = cy + (arrowLength * 0.55f)
            val notchY = baseY - notchDepth

            // Ліва грань (NeonGreen)
            val leftPath = Path().apply {
                moveTo(cx, tipY)
                lineTo(leftX, baseY)
                lineTo(cx, notchY)
                close()
            }
            drawPath(
                path = leftPath,
                color = if (isTargetReached) Color(0xFF76FF03) else arrowColor,
                style = Fill
            )

            // Права грань (більш темний відтінок для 2D-об'єму)
            val rightPath = Path().apply {
                moveTo(cx, tipY)
                lineTo(rightX, baseY)
                lineTo(cx, notchY)
                close()
            }
            drawPath(
                path = rightPath,
                color = if (isTargetReached) Color(0xFF64DD17) else accentColor,
                style = Fill
            )

            // Центральна розділювальна лінія для чіткості
            drawLine(
                color = Color(0xFF000000),
                start = Offset(cx, tipY),
                end = Offset(cx, notchY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Центральна точка кріплення
        drawCircle(
            color = Color(0xFF000000),
            radius = 6.dp.toPx(),
            center = Offset(cx, cy)
        )
        drawCircle(
            color = arrowColor,
            radius = 3.dp.toPx(),
            center = Offset(cx, cy)
        )
    }
}
