package com.tgm.tgmc.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val TgmcDarkColorScheme = darkColorScheme(
    primary          = Cyan400,
    onPrimary        = Navy900,
    primaryContainer = Navy700,
    onPrimaryContainer = Cyan200,

    secondary        = Indigo400,
    onSecondary      = Color.White,
    secondaryContainer = Navy600,
    onSecondaryContainer = Indigo200,

    tertiary         = SuccessGreen,
    onTertiary       = Navy900,

    background       = Navy900,
    onBackground     = TextPrimary,

    surface          = Surface900,
    onSurface        = TextPrimary,
    surfaceVariant   = Surface800,
    onSurfaceVariant = TextSecondary,

    surfaceTint      = Cyan400,
    inverseSurface   = Surface050,
    inverseOnSurface = Navy900,

    error            = ErrorRed,
    onError          = Color.White,
    errorContainer   = Color(0xFF4A0E1A),
    onErrorContainer = Color(0xFFFFB3C1),

    outline          = Surface600,
    outlineVariant   = Surface700,

    scrim            = Color(0x99000000),
)

// Light scheme kept for completeness; dark-first is the default
val TgmcLightColorScheme = lightColorScheme(
    primary          = Navy600,
    onPrimary        = Color.White,
    primaryContainer = Cyan100,
    onPrimaryContainer = Navy800,

    secondary        = Indigo400,
    onSecondary      = Color.White,

    background       = Surface050,
    onBackground     = Navy900,

    surface          = Color.White,
    onSurface        = Navy900,

    error            = ErrorRed,
    onError          = Color.White,
)
