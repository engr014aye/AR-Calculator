package com.munawarini.arcalculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Extended color set exposed through CompositionLocal ───────────────────────

data class ARColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceHigh: Color,
    val glass: Color,
    val glassBorder: Color,
    val numberBtn: Color,
    val operatorBtn: Color,
    val functionBtn: Color,
    val clearBtn: Color,
    val accent: Color,
    val accentSecondary: Color,
    val displayText: Color,
    val resultText: Color,
    val mutedText: Color,
    val equalsGradient: Brush,
    val isLight: Boolean
)

val LocalARColors = staticCompositionLocalOf<ARColors> {
    error("No ARColors provided")
}

private val DarkARColors = ARColors(
    background      = DarkBackground,
    surface         = DarkSurface,
    surfaceVariant  = DarkSurfaceVariant,
    surfaceHigh     = DarkSurfaceHigh,
    glass           = GlassDark,
    glassBorder     = GlassDarkBorder,
    numberBtn       = DarkNumberBtn,
    operatorBtn     = DarkOperatorBtn,
    functionBtn     = DarkFunctionBtn,
    clearBtn        = DarkClearBtn,
    accent          = AccentViolet,
    accentSecondary = AccentCyan,
    displayText     = TextWhite,
    resultText      = TextSecondary,
    mutedText       = TextMuted,
    equalsGradient  = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
    isLight         = false
)

private val LightARColors = ARColors(
    background      = LightBackground,
    surface         = LightSurface,
    surfaceVariant  = LightSurfaceVariant,
    surfaceHigh     = LightSurfaceHigh,
    glass           = GlassLight,
    glassBorder     = GlassLightBorder,
    numberBtn       = LightNumberBtn,
    operatorBtn     = LightOperatorBtn,
    functionBtn     = LightFunctionBtn,
    clearBtn        = LightClearBtn,
    accent          = AccentVioletLight,
    accentSecondary = AccentCyanLight,
    displayText     = TextDark,
    resultText      = TextDarkSecondary,
    mutedText       = TextDarkSecondary,
    equalsGradient  = Brush.linearGradient(listOf(AccentVioletLight, AccentCyanLight)),
    isLight         = true
)

// ── Material 3 Color Schemes ──────────────────────────────────────────────────

private val darkM3Scheme = darkColorScheme(
    primary          = AccentViolet,
    onPrimary        = TextWhite,
    secondary        = AccentCyan,
    onSecondary      = TextWhite,
    background       = DarkBackground,
    onBackground     = TextWhite,
    surface          = DarkSurface,
    onSurface        = TextWhite,
    surfaceVariant   = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error            = ErrorRed,
    onError          = TextWhite
)

private val lightM3Scheme = lightColorScheme(
    primary          = AccentVioletLight,
    onPrimary        = TextWhite,
    secondary        = AccentCyanLight,
    onSecondary      = TextWhite,
    background       = LightBackground,
    onBackground     = TextDark,
    surface          = LightSurface,
    onSurface        = TextDark,
    surfaceVariant   = LightSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    error            = ErrorRed,
    onError          = TextWhite
)

// ── Theme entry point ─────────────────────────────────────────────────────────

@Composable
fun ARCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val m3Scheme: ColorScheme = if (darkTheme) darkM3Scheme else lightM3Scheme
    val arColors: ARColors    = if (darkTheme) DarkARColors  else LightARColors

    CompositionLocalProvider(LocalARColors provides arColors) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography  = ARTypography,
            content     = content
        )
    }
}

/** Shorthand accessor — use instead of MaterialTheme.colorScheme for AR-specific tokens. */
val MaterialTheme.arColors: ARColors
    @Composable get() = LocalARColors.current
