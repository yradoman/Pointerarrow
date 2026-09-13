package com.pointarrow.nav.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = PureBlack,
    surface = DarkSurface,
    onSurface = TextPrimary,
    background = PureBlack,
    onBackground = TextPrimary
)

@Composable
fun PointArrowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
