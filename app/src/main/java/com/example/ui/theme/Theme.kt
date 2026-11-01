package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class NovaThemeMode {
    OBSIDIAN_BLACK,
    COSMIC_NEBULA,
    CYBER_PURPLE
}

private val ObsidianColorScheme = darkColorScheme(
    primary = NovaPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = NovaPurpleDeep,
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = NovaCyanAccent,
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004F54),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = NovaBlueAccent,
    background = Color(0xFF05070B),
    onBackground = NovaTextPrimary,
    surface = Color(0xFF0C0F17),
    onSurface = NovaTextPrimary,
    surfaceVariant = Color(0xFF141926),
    onSurfaceVariant = NovaTextSecondary,
    outline = NovaSurfaceBorder,
    error = NovaError
)

private val CosmicNebulaColorScheme = darkColorScheme(
    primary = NovaPurpleSecondary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = NovaBlueAccent,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = NovaCyanAccent,
    background = Color(0xFF080B14),
    onBackground = NovaTextPrimary,
    surface = Color(0xFF101524),
    onSurface = NovaTextPrimary,
    surfaceVariant = Color(0xFF1A2138),
    onSurfaceVariant = NovaTextSecondary,
    outline = Color(0xFF2C375A),
    error = NovaError
)

private val CyberPurpleColorScheme = darkColorScheme(
    primary = Color(0xFFC084FC),
    onPrimary = Color(0xFF2E1065),
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF082F49),
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFFA855F7),
    background = Color(0xFF0A0714),
    onBackground = NovaTextPrimary,
    surface = Color(0xFF140E24),
    onSurface = NovaTextPrimary,
    surfaceVariant = Color(0xFF201738),
    onSurfaceVariant = NovaTextSecondary,
    outline = Color(0xFF3E2D69),
    error = NovaError
)

@Composable
fun NovaTheme(
    themeMode: NovaThemeMode = NovaThemeMode.OBSIDIAN_BLACK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        NovaThemeMode.OBSIDIAN_BLACK -> ObsidianColorScheme
        NovaThemeMode.COSMIC_NEBULA -> CosmicNebulaColorScheme
        NovaThemeMode.CYBER_PURPLE -> CyberPurpleColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
