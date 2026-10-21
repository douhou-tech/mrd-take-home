package com.mrd.restaurant.discovery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MrDLightBlue,
    onPrimary = Color.Black,
    secondary = MrDBlueAccent,
    onSecondary = Color.Black,
    tertiary = MrDLightBlue,
    background = Color(0xFF0A0A0A),         // Almost true black
    surface = Color(0xFF000000),            // Standard dark surface
    surfaceVariant = Color(0xFF000000),     // Slightly lighter than surface, great for cards
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF000000),
    surfaceContainer = Color(0xFF000000),       // Default card background in M3
    surfaceContainerHigh = Color(0xFF000000),
    surfaceContainerHighest = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = MrDBlueDark,
    onPrimary = Color.White,
    secondary = MrDBlueAccent,
    onSecondary = Color.White,
    tertiary = MrDBlueDark
)

@Composable
fun TakeHomeDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
