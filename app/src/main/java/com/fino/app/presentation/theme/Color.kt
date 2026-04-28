package com.fino.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import com.fino.app.presentation.theme.tokens.*

// ===========================================
// FINO — QUIET LUXURY FINTECH
// ===========================================
// Legacy names are preserved and re-mapped to the new tokens so the 21 inherited screens
// keep compiling. The ergonomic way to consume colors on new screens is via
// MaterialTheme.colorScheme and the FinoColors composable accessors below.

// --- Default (light-leaning) legacy aliases ---
// These top-level vals are referenced throughout the codebase. They resolve to
// the LIGHT tokens; screens that honor MaterialTheme.colorScheme will pick up
// the correct value for the active theme regardless.

val DarkBackground = PaperDark
val DarkSurface = Paper2Dark
val DarkSurfaceVariant = Paper3Dark
val DarkSurfaceHigh = Paper3Dark

val PrimaryStart = AccentLight
val PrimaryEnd = AccentInkLight
val Primary = AccentLight

val SecondaryStart = Ink3Light
val SecondaryEnd = Ink2Light
val Secondary = Ink3Light

val Accent = InkLight
val AccentPink = WarnLight
val AccentCyan = AccentInkLight

val TextPrimary = InkLight
val TextSecondary = Ink3Light
val TextTertiary = Ink4Light
val TextOnGradient = CardLight

val Success = AccentLight
val Warning = WarnLight
val Error = NegativeLight
val Info = AccentLight

val ExpenseRed = NegativeLight
val IncomeGreen = AccentLight
val ExpenseRedStart = NegativeLight
val ExpenseRedEnd = NegativeLight
val IncomeGreenStart = AccentLight
val IncomeGreenEnd = AccentLight

val CategoryFood = C3Light
val CategoryTransport = C2Light
val CategoryShopping = C4Light
val CategoryHealth = C1Light
val CategoryEntertainment = C6Light
val CategoryBills = Ink3Light
val CategoryEducation = C5Light
val CategoryTravel = C4Light
val CategoryGroceries = C1Light
val CategoryPersonal = C2Light
val CategoryOther = Ink4Light

val BudgetSafe = AccentLight
val BudgetWarning = WarnLight
val BudgetDanger = NegativeLight

val XpGold = Ink2Light
val XpGoldStart = Ink2Light
val XpGoldEnd = InkLight
val StreakFire = WarnLight
val StreakFireStart = WarnLight
val StreakFireEnd = NegativeLight

val AchievementLocked = Ink5Light
val AchievementBronze = Ink3Light
val AchievementSilver = Ink4Light
val AchievementGold = Ink2Light
val AchievementPlatinum = Ink2Light

val Level1 = AccentLight
val Level2 = AccentLight
val Level3 = AccentLight
val Level4 = AccentLight
val Level5 = AccentLight
val Level6 = AccentLight
val Level7 = AccentLight
val Level8 = AccentLight

val CardBlueStart = Paper2Light
val CardBlueEnd = Paper3Light
val CardGoldStart = Paper2Light
val CardGoldEnd = Paper3Light
val CardPlatinumStart = Paper2Light
val CardPlatinumEnd = Paper3Light

val Overlay = Color(0x80141310)
val GlowPurple = AccentSoftLight
val GlowTeal = AccentSoftLight

val Divider = LineLight
val Border = Line2Light

// --- Legacy-prefixed aliases ---
val FinoPrimary = Primary
val FinoSecondary = Secondary
val FinoAccent = Accent
val FinoLightBackground = PaperLight
val FinoLightSurface = CardLight
val FinoLightOnBackground = InkLight
val FinoLightOnSurface = InkLight
val FinoDarkBackground = DarkBackground
val FinoDarkSurface = DarkSurface
val FinoDarkOnBackground = InkDark
val FinoDarkOnSurface = InkDark
val FinoOnPrimary = CardLight
val FinoOnSecondary = CardLight
val FinoSuccess = Success
val FinoWarning = Warning
val FinoError = Error
val FinoDebit = ExpenseRed
val FinoCredit = IncomeGreen

/**
 * Theme-aware color accessors. New screens should prefer these over the
 * top-level aliases above.
 */
object FinoColors {
    // Legacy static properties — retained for call-site compatibility. These point at
    // the LIGHT tokens. For theme-reactive reads, use the @Composable accessors.
    val background: Color get() = PaperLight
    val surface: Color get() = CardLight
    val surfaceVariant: Color get() = Paper2Light
    val primary: Color get() = AccentLight
    val secondary: Color get() = Ink2Light
    val accent: Color get() = InkLight
    val textPrimary: Color get() = InkLight
    val textSecondary: Color get() = Ink3Light
    val success: Color get() = AccentLight
    val warning: Color get() = WarnLight
    val error: Color get() = NegativeLight
    val expense: Color get() = NegativeLight
    val income: Color get() = AccentLight

    // --- Theme-reactive accessors — prefer these in new composables. ---
    @Composable @ReadOnlyComposable fun paper(): Color =
        if (isSystemInDarkTheme()) PaperDark else PaperLight
    @Composable @ReadOnlyComposable fun paper2(): Color =
        if (isSystemInDarkTheme()) Paper2Dark else Paper2Light
    @Composable @ReadOnlyComposable fun paper3(): Color =
        if (isSystemInDarkTheme()) Paper3Dark else Paper3Light
    @Composable @ReadOnlyComposable fun ink(): Color =
        if (isSystemInDarkTheme()) InkDark else InkLight
    @Composable @ReadOnlyComposable fun ink2(): Color =
        if (isSystemInDarkTheme()) Ink2Dark else Ink2Light
    @Composable @ReadOnlyComposable fun ink3(): Color =
        if (isSystemInDarkTheme()) Ink3Dark else Ink3Light
    @Composable @ReadOnlyComposable fun ink4(): Color =
        if (isSystemInDarkTheme()) Ink4Dark else Ink4Light
    @Composable @ReadOnlyComposable fun ink5(): Color =
        if (isSystemInDarkTheme()) Ink5Dark else Ink5Light
    @Composable @ReadOnlyComposable fun card(): Color =
        if (isSystemInDarkTheme()) CardDark else CardLight
    @Composable @ReadOnlyComposable fun cardTint(): Color =
        if (isSystemInDarkTheme()) CardTintDark else CardTintLight
    @Composable @ReadOnlyComposable fun line(): Color =
        if (isSystemInDarkTheme()) LineDark else LineLight
    @Composable @ReadOnlyComposable fun line2(): Color =
        if (isSystemInDarkTheme()) Line2Dark else Line2Light
    @Composable @ReadOnlyComposable fun accentColor(): Color =
        if (isSystemInDarkTheme()) AccentDark else AccentLight
    @Composable @ReadOnlyComposable fun accentSoft(): Color =
        if (isSystemInDarkTheme()) AccentSoftDark else AccentSoftLight
    @Composable @ReadOnlyComposable fun accentInk(): Color =
        if (isSystemInDarkTheme()) AccentInkDark else AccentInkLight
    @Composable @ReadOnlyComposable fun warn(): Color =
        if (isSystemInDarkTheme()) WarnDark else WarnLight
    @Composable @ReadOnlyComposable fun warnSoft(): Color =
        if (isSystemInDarkTheme()) WarnSoftDark else WarnSoftLight
    @Composable @ReadOnlyComposable fun positive(): Color =
        if (isSystemInDarkTheme()) PositiveDark else PositiveLight
    @Composable @ReadOnlyComposable fun negative(): Color =
        if (isSystemInDarkTheme()) NegativeDark else NegativeLight

    // Chart palette — theme-reactive.
    @Composable @ReadOnlyComposable fun chart(): List<Color> =
        if (isSystemInDarkTheme()) {
            listOf(C1Dark, C2Dark, C3Dark, C4Dark, C5Dark, C6Dark)
        } else {
            listOf(C1Light, C2Light, C3Light, C4Light, C5Light, C6Light)
        }
}
