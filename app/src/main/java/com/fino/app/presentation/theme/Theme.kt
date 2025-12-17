package com.fino.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Fino Dark Color Scheme - CRED Inspired
 * Premium dark theme with vibrant purple/teal accents
 */
private val FinoDarkColorScheme = darkColorScheme(
    // Primary
    primary = Primary,
    onPrimary = TextPrimary,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,

    // Secondary
    secondary = Secondary,
    onSecondary = TextPrimary,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = TextPrimary,

    // Tertiary
    tertiary = Accent,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurfaceHigh,
    onTertiaryContainer = Accent,

    // Background
    background = DarkBackground,
    onBackground = TextPrimary,

    // Surface
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    // Error
    error = Error,
    onError = TextPrimary,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,

    // Outline
    outline = Border,
    outlineVariant = Divider,

    // Inverse
    inverseSurface = TextPrimary,
    inverseOnSurface = DarkBackground,
    inversePrimary = Primary,

    // Scrim
    scrim = Overlay
)

/**
 * Fino Light Color Scheme (fallback)
 * Minimal light theme for accessibility
 */
private val FinoLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.1f),
    onPrimaryContainer = Primary,

    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Secondary.copy(alpha = 0.1f),
    onSecondaryContainer = Secondary,

    tertiary = Accent,
    onTertiary = DarkBackground,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),

    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0F0F5),
    onSurfaceVariant = Color(0xFF49454F),

    error = Error,
    onError = Color.White
)

/**
 * Custom shapes for Fino UI
 * More rounded corners for playful look
 */
val FinoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Fino Theme Composable
 * Default to dark theme for premium feel
 */
@Composable
fun FinoTheme(
    darkTheme: Boolean = true, // Default to dark theme
    dynamicColor: Boolean = false, // Disable dynamic to keep brand colors
    content: @Composable () -> Unit
) {
    // Always use dark theme for consistent premium look
    // Can be changed later for user preference
    val colorScheme = if (darkTheme) FinoDarkColorScheme else FinoLightColorScheme

    // Update status bar color
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Dark status bar for dark theme
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
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

/**
 * Extension to get Fino-specific colors easily
 */
object FinoColors {
    val background = DarkBackground
    val surface = DarkSurface
    val surfaceVariant = DarkSurfaceVariant
    val primary = Primary
    val secondary = Secondary
    val accent = Accent
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val success = Success
    val warning = Warning
    val error = Error
    val expense = ExpenseRed
    val income = IncomeGreen
}
