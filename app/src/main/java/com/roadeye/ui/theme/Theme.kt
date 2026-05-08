package com.roadeye.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────────
// RoadEye Government-Grade Color Palette
// ─────────────────────────────────────────────

// Primary – Andhra Pradesh government blue
val RoadEyeBlue = Color(0xFF1A3A6B)
val RoadEyeBlueDark = Color(0xFF0D2247)
val RoadEyeBlueLight = Color(0xFF2E5FAA)
val RoadEyeBlueContainer = Color(0xFFD6E4FF)

// Secondary – Saffron/Orange (National colors)
val RoadEyeSaffron = Color(0xFFFF8C00)
val RoadEyeSaffronLight = Color(0xFFFFB347)
val RoadEyeSaffronContainer = Color(0xFFFFE0B2)

// Tertiary – Green (progress/success)
val RoadEyeGreen = Color(0xFF2E7D32)
val RoadEyeGreenLight = Color(0xFF4CAF50)
val RoadEyeGreenContainer = Color(0xFFC8E6C9)

// Severity Colors
val SeverityHigh = Color(0xFFD32F2F)
val SeverityMedium = Color(0xFFF57C00)
val SeverityLow = Color(0xFF388E3C)

// Status Colors
val StatusSubmitted = Color(0xFF1565C0)
val StatusInProgress = Color(0xFFF57C00)
val StatusResolved = Color(0xFF2E7D32)

// Road Health Meter
val HealthGood = Color(0xFF4CAF50)
val HealthMedium = Color(0xFFFFC107)
val HealthDangerous = Color(0xFFF44336)

// Neutral
val BackgroundLight = Color(0xFFF4F6FB)
val BackgroundDark = Color(0xFF0F1923)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1A2533)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E2D3D)

private val LightColorScheme = lightColorScheme(
    primary = RoadEyeBlue,
    onPrimary = Color.White,
    primaryContainer = RoadEyeBlueContainer,
    onPrimaryContainer = RoadEyeBlueDark,
    secondary = RoadEyeSaffron,
    onSecondary = Color.White,
    secondaryContainer = RoadEyeSaffronContainer,
    onSecondaryContainer = Color(0xFF4A2800),
    tertiary = RoadEyeGreen,
    onTertiary = Color.White,
    tertiaryContainer = RoadEyeGreenContainer,
    onTertiaryContainer = Color(0xFF002200),
    background = BackgroundLight,
    onBackground = Color(0xFF1A1C22),
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C22),
    surfaceVariant = Color(0xFFE8EEF8),
    onSurfaceVariant = Color(0xFF44495A),
    outline = Color(0xFF74798B),
    error = SeverityHigh,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9CBCFF),
    onPrimary = Color(0xFF002D6C),
    primaryContainer = RoadEyeBlue,
    onPrimaryContainer = RoadEyeBlueContainer,
    secondary = RoadEyeSaffronLight,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = RoadEyeSaffronContainer,
    tertiary = Color(0xFF84D98C),
    onTertiary = Color(0xFF003910),
    tertiaryContainer = Color(0xFF005320),
    onTertiaryContainer = RoadEyeGreenContainer,
    background = BackgroundDark,
    onBackground = Color(0xFFE2E4EE),
    surface = SurfaceDark,
    onSurface = Color(0xFFE2E4EE),
    surfaceVariant = Color(0xFF1E2D3D),
    onSurfaceVariant = Color(0xFFC4C8D8),
    outline = Color(0xFF8E93A5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun RoadEyeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RoadEyeTypography,
        shapes = RoadEyeShapes,
        content = content
    )
}
