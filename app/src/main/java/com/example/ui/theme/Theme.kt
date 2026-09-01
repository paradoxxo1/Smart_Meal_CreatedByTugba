package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Custom Color Token Class to support both Light and Dark dynamic styling cleanly
data class AppCustomColors(
    val cosmicBackground: Color,
    val cosmicSurface: Color,
    val cosmicSurfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val LocalAppCustomColors = staticCompositionLocalOf {
    AppCustomColors(
        cosmicBackground = DarkCosmicBackground,
        cosmicSurface = DarkCosmicSurface,
        cosmicSurfaceVariant = DarkCosmicSurfaceVariant,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textMuted = DarkTextMuted,
        isDark = true
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    secondary = LavenderSecondary,
    tertiary = RoseTertiary,
    background = DarkCosmicBackground,
    surface = DarkCosmicSurface,
    surfaceVariant = DarkCosmicSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F172A),
    onTertiary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorCrimson
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = LavenderSecondary,
    tertiary = Color(0xFF0284C7),
    background = LightCosmicBackground,
    surface = LightCosmicSurface,
    surfaceVariant = LightCosmicSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color(0xFF1E293B),
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = ErrorCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appCustomColors = if (darkTheme) {
        AppCustomColors(
            cosmicBackground = DarkCosmicBackground,
            cosmicSurface = DarkCosmicSurface,
            cosmicSurfaceVariant = DarkCosmicSurfaceVariant,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            isDark = true
        )
    } else {
        AppCustomColors(
            cosmicBackground = LightCosmicBackground,
            cosmicSurface = LightCosmicSurface,
            cosmicSurfaceVariant = LightCosmicSurfaceVariant,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalAppCustomColors provides appCustomColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
