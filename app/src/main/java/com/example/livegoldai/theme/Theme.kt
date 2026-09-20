package com.example.livegoldai.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = ObsidianBackground,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldOnContainer,
    secondary = GoldLight,
    onSecondary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderHighlight
)

@Composable
fun LiveGoldAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
