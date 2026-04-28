package com.fino.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.fino.app.presentation.theme.tokens.*

/**
 * Fino Light Color Scheme — warm paper.
 */
private val FinoLightColorScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = CardLight,
    primaryContainer = AccentSoftLight,
    onPrimaryContainer = AccentInkLight,

    secondary = Ink2Light,
    onSecondary = CardLight,
    secondaryContainer = Paper2Light,
    onSecondaryContainer = InkLight,

    tertiary = WarnLight,
    onTertiary = CardLight,
    tertiaryContainer = WarnSoftLight,
    onTertiaryContainer = InkLight,

    background = PaperLight,
    onBackground = InkLight,

    surface = PaperLight,
    onSurface = InkLight,
    surfaceVariant = Paper2Light,
    onSurfaceVariant = Ink3Light,
    surfaceContainer = CardLight,
    surfaceContainerHigh = CardTintLight,
    surfaceContainerHighest = Paper2Light,
    surfaceContainerLow = PaperLight,
    surfaceContainerLowest = PaperLight,

    error = NegativeLight,
    onError = CardLight,
    errorContainer = WarnSoftLight,
    onErrorContainer = NegativeLight,

    outline = Line2Light,
    outlineVariant = LineLight,

    inverseSurface = InkLight,
    inverseOnSurface = PaperLight,
    inversePrimary = AccentDark,

    scrim = InkLight
)

/**
 * Fino Dark Color Scheme — deep paper.
 */
private val FinoDarkColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = InkLight,
    primaryContainer = AccentSoftDark,
    onPrimaryContainer = AccentInkDark,

    secondary = Ink2Dark,
    onSecondary = PaperDark,
    secondaryContainer = Paper2Dark,
    onSecondaryContainer = InkDark,

    tertiary = WarnDark,
    onTertiary = InkLight,
    tertiaryContainer = WarnSoftDark,
    onTertiaryContainer = InkDark,

    background = PaperDark,
    onBackground = InkDark,

    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = Paper2Dark,
    onSurfaceVariant = Ink3Dark,
    surfaceContainer = CardDark,
    surfaceContainerHigh = CardTintDark,
    surfaceContainerHighest = Paper2Dark,
    surfaceContainerLow = PaperDark,
    surfaceContainerLowest = PaperDark,

    error = NegativeDark,
    onError = InkLight,
    errorContainer = WarnSoftDark,
    onErrorContainer = NegativeDark,

    outline = Line2Dark,
    outlineVariant = LineDark,

    inverseSurface = InkDark,
    inverseOnSurface = PaperDark,
    inversePrimary = AccentLight,

    scrim = InkDark
)

/**
 * Shape scale — quiet luxury uses restrained corners.
 */
val FinoShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Fino Theme composable — now follows the system setting by default.
 */
@Composable
fun FinoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FinoDarkColorScheme else FinoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = FinoShapes,
        typography = FinoTypography,
        content = content
    )
}
