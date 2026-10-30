package com.cartoniq.pantrypal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Sage,
    onPrimary = Slate,
    primaryContainer = SageTint,
    onPrimaryContainer = Slate,
    secondary = Terracotta,
    onSecondary = Color.White,
    tertiary = Honey,
    onTertiary = Slate,
    background = Cream,
    onBackground = Slate,
    surface = SurfaceWhite,
    onSurface = Slate,
    surfaceVariant = LightVariant,
    onSurfaceVariant = MutedText,
    error = Terracotta,
    onError = Color.White,
    outline = Color(0xFFD5D2C8)
)

private val DarkColors = darkColorScheme(
    primary = Sage,
    onPrimary = Slate,
    primaryContainer = DarkSageTint,
    onPrimaryContainer = DarkText,
    secondary = Terracotta,
    onSecondary = Color.White,
    tertiary = Honey,
    onTertiary = Slate,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkVariant,
    onSurfaceVariant = DarkMuted,
    error = Terracotta,
    onError = Color.White,
    outline = Color(0xFF3F4743)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PantryPalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/** Sage that is readable as small text: the plain colour in dark mode, a darker one in light mode. */
@Composable
fun sageText(): Color = if (isSystemInDarkTheme()) Sage else SageText

@Composable
fun terracottaText(): Color = if (isSystemInDarkTheme()) Terracotta else TerracottaText
