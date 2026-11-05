package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioDarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryColor,
    onTertiary = OnTertiaryColor,
    background = RadioBlack,
    onBackground = Color(0xFFF0F6FC),
    surface = RadioDarkSlate,
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = RadioCharcoal,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = Color(0xFF30363D)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // For a realistic illuminated radio chassis, we consistently use the optimized illuminated dark palette
    MaterialTheme(
        colorScheme = RadioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
