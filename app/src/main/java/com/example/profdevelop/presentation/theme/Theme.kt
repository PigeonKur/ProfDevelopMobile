package com.example.profdevelop.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = BrandSurface,
    secondary = BrandWarm,
    background = BrandBackground,
    onBackground = BrandText,
    surface = BrandSurface,
    onSurface = BrandText,
    surfaceVariant = BrandSurfaceAlt,
    outline = BrandOutline,
    error = BrandDanger
)

private val DarkColors = darkColorScheme(
    primary = BrandGreenSoft,
    onPrimary = BrandText,
    secondary = BrandWarm,
    background = BrandText,
    onBackground = BrandBackground,
    surface = BrandGreenDark,
    onSurface = BrandBackground,
    outline = BrandMuted,
    error = BrandDanger
)

@Composable
fun ProfDevelopTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
