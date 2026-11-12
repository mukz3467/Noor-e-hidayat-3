package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppTheme

private val EmeraldGoldColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = Color(0xFF664D03),
    background = EmeraldBackground,
    surface = EmeraldSurface,
    onBackground = EmeraldOnSurface,
    onSurface = EmeraldOnSurface,
    surfaceVariant = Color(0xFFE2EBE5),
    onSurfaceVariant = Color(0xFF2C3E35)
)

private val SoftLightColorScheme = lightColorScheme(
    primary = SoftLightPrimary,
    onPrimary = SoftLightOnPrimary,
    primaryContainer = SoftLightPrimaryContainer,
    onPrimaryContainer = SoftLightOnPrimaryContainer,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    background = SoftLightBackground,
    surface = SoftLightSurface,
    onBackground = SoftLightOnSurface,
    onSurface = SoftLightOnSurface,
    surfaceVariant = Color(0xFFE6FFFA),
    onSurfaceVariant = Color(0xFF234E52)
)

private val DarkReadingColorScheme = darkColorScheme(
    primary = DarkReadingPrimary,
    onPrimary = DarkReadingOnPrimary,
    primaryContainer = DarkReadingPrimaryContainer,
    onPrimaryContainer = DarkReadingOnPrimaryContainer,
    secondary = DarkReadingGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    background = DarkReadingBackground,
    surface = DarkReadingSurface,
    onBackground = DarkReadingOnSurface,
    onSurface = DarkReadingOnSurface,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val WarmCreamColorScheme = lightColorScheme(
    primary = WarmCreamPrimary,
    onPrimary = WarmCreamOnPrimary,
    primaryContainer = WarmCreamPrimaryContainer,
    onPrimaryContainer = WarmCreamOnPrimaryContainer,
    secondary = WarmCreamAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDE68A),
    onSecondaryContainer = Color(0xFF78350F),
    background = WarmCreamBackground,
    surface = WarmCreamSurface,
    onBackground = WarmCreamOnSurface,
    onSurface = WarmCreamOnSurface,
    surfaceVariant = Color(0xFFF5E6CA),
    onSurfaceVariant = Color(0xFF5D4037)
)

@Composable
fun NooreHidayatTheme(
    selectedTheme: AppTheme = AppTheme.EMERALD_GOLD,
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = when (selectedTheme) {
        AppTheme.EMERALD_GOLD -> EmeraldGoldColorScheme
        AppTheme.SOFT_LIGHT -> SoftLightColorScheme
        AppTheme.DARK_READING -> DarkReadingColorScheme
        AppTheme.WARM_CREAM -> WarmCreamColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
