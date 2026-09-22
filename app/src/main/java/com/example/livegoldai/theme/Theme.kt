package com.example.livegoldai.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: String
) {
    ROYAL_OBSIDIAN(
        title = "Royal Obsidian Gold",
        subtitle = "Pitch black OLED contrast with 24K bullion gold accents",
        badge = "👑 SIGNATURE LUXURY",
        icon = "👑"
    ),
    CYBER_NEON(
        title = "Cyber Tokyo Matrix",
        subtitle = "Ultra high-contrast neon cyan & emerald trading terminal",
        badge = "⚡ HIGH FREQUENCY",
        icon = "⚡"
    ),
    SWISS_BANK(
        title = "Swiss Bullion Navy",
        subtitle = "Deep institutional sapphire midnight & champagne accents",
        badge = "🏦 INSTITUTIONAL",
        icon = "🏛️"
    ),
    EMERALD_ALPHA(
        title = "Emerald Alpha Hedge",
        subtitle = "Prestige deep jade forest with glowing mint buy triggers",
        badge = "💎 WEALTH HEDGE",
        icon = "💎"
    )
}

data class AppThemeColors(
    val themeMode: ThemeMode,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceCard: Color,
    val border: Color,
    val borderHighlight: Color,
    val primaryGold: Color,
    val lightGold: Color,
    val darkGold: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val signalBuy: Color,
    val signalBuyBg: Color,
    val signalSell: Color,
    val signalSellBg: Color,
    val signalWait: Color,
    val signalWaitBg: Color
)

val RoyalObsidianPalette = AppThemeColors(
    themeMode = ThemeMode.ROYAL_OBSIDIAN,
    background = Color(0xFF0A0D14),
    surface = Color(0xFF131822),
    surfaceElevated = Color(0xFF1B2230),
    surfaceCard = Color(0xFF161C27),
    border = Color(0xFF262E3F),
    borderHighlight = Color(0xFF3B4861),
    primaryGold = Color(0xFFF5B800),
    lightGold = Color(0xFFFFD54F),
    darkGold = Color(0xFFC49300),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    signalBuy = Color(0xFF00E676),
    signalBuyBg = Color(0xFF0A331E),
    signalSell = Color(0xFFFF3B30),
    signalSellBg = Color(0xFF381214),
    signalWait = Color(0xFFFFB300),
    signalWaitBg = Color(0xFF33270A)
)

val CyberNeonPalette = AppThemeColors(
    themeMode = ThemeMode.CYBER_NEON,
    background = Color(0xFF050B14),
    surface = Color(0xFF0B1728),
    surfaceElevated = Color(0xFF10223A),
    surfaceCard = Color(0xFF0D1D33),
    border = Color(0xFF1B3A5E),
    borderHighlight = Color(0xFF00E5FF),
    primaryGold = Color(0xFF00E5FF), // Electric Cyan
    lightGold = Color(0xFF80F3FF),
    darkGold = Color(0xFF009AB0),
    textPrimary = Color(0xFFF0FDF4),
    textSecondary = Color(0xFF93C5FD),
    textMuted = Color(0xFF477199),
    signalBuy = Color(0xFF00FF9D),
    signalBuyBg = Color(0xFF033320),
    signalSell = Color(0xFFFF1744),
    signalSellBg = Color(0xFF3B0713),
    signalWait = Color(0xFFFFD600),
    signalWaitBg = Color(0xFF332900)
)

val SwissBankPalette = AppThemeColors(
    themeMode = ThemeMode.SWISS_BANK,
    background = Color(0xFF0B111E),
    surface = Color(0xFF131D31),
    surfaceElevated = Color(0xFF1B2844),
    surfaceCard = Color(0xFF152238),
    border = Color(0xFF2B3C5E),
    borderHighlight = Color(0xFFD4AF37),
    primaryGold = Color(0xFFE5C07B), // Champagne Bullion
    lightGold = Color(0xFFF4DEAB),
    darkGold = Color(0xFFB58B35),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFA5B4CB),
    textMuted = Color(0xFF677794),
    signalBuy = Color(0xFF22C55E),
    signalBuyBg = Color(0xFF0F311C),
    signalSell = Color(0xFFEF4444),
    signalSellBg = Color(0xFF3B1214),
    signalWait = Color(0xFFF59E0B),
    signalWaitBg = Color(0xFF382306)
)

val EmeraldAlphaPalette = AppThemeColors(
    themeMode = ThemeMode.EMERALD_ALPHA,
    background = Color(0xFF06130E),
    surface = Color(0xFF0C2018),
    surfaceElevated = Color(0xFF122C22),
    surfaceCard = Color(0xFF0E251C),
    border = Color(0xFF1F4A38),
    borderHighlight = Color(0xFF10B981),
    primaryGold = Color(0xFF10B981), // Emerald Alpha
    lightGold = Color(0xFF6EE7B7),
    darkGold = Color(0xFF047857),
    textPrimary = Color(0xFFF0FDF4),
    textSecondary = Color(0xFF86EFAC),
    textMuted = Color(0xFF4D7C66),
    signalBuy = Color(0xFF10B981),
    signalBuyBg = Color(0xFF063321),
    signalSell = Color(0xFFF43F5E),
    signalSellBg = Color(0xFF3D0C17),
    signalWait = Color(0xFFEAB308),
    signalWaitBg = Color(0xFF332605)
)

fun getPaletteForMode(mode: ThemeMode): AppThemeColors {
    return when (mode) {
        ThemeMode.ROYAL_OBSIDIAN -> RoyalObsidianPalette
        ThemeMode.CYBER_NEON -> CyberNeonPalette
        ThemeMode.SWISS_BANK -> SwissBankPalette
        ThemeMode.EMERALD_ALPHA -> EmeraldAlphaPalette
    }
}

val LocalAppColors = staticCompositionLocalOf { RoyalObsidianPalette }

@Composable
fun LiveGoldAITheme(
    themeMode: ThemeMode = ThemeMode.ROYAL_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val palette = getPaletteForMode(themeMode)

    val colorScheme = darkColorScheme(
        primary = palette.primaryGold,
        onPrimary = palette.background,
        primaryContainer = palette.surfaceElevated,
        onPrimaryContainer = palette.lightGold,
        secondary = palette.lightGold,
        onSecondary = palette.background,
        background = palette.background,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceCard,
        onSurfaceVariant = palette.textSecondary,
        outline = palette.border,
        outlineVariant = palette.borderHighlight
    )

    CompositionLocalProvider(LocalAppColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

