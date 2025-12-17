package com.fino.app.presentation.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradient definitions for Fino app.
 * Used for cards, buttons, backgrounds, and various UI elements.
 */
object FinoGradients {

    // ===========================================
    // PRIMARY GRADIENTS
    // ===========================================

    /** Main purple-blue gradient - used for primary actions */
    val Primary = Brush.linearGradient(
        colors = listOf(PrimaryStart, PrimaryEnd)
    )

    /** Primary gradient with angle */
    val PrimaryDiagonal = Brush.linearGradient(
        colors = listOf(PrimaryStart, PrimaryEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Radial primary gradient for circular elements */
    val PrimaryRadial = Brush.radialGradient(
        colors = listOf(PrimaryStart, PrimaryEnd)
    )

    // ===========================================
    // SECONDARY GRADIENTS
    // ===========================================

    /** Teal-green gradient - used for positive/income elements */
    val Secondary = Brush.linearGradient(
        colors = listOf(SecondaryStart, SecondaryEnd)
    )

    val SecondaryDiagonal = Brush.linearGradient(
        colors = listOf(SecondaryStart, SecondaryEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // ===========================================
    // TRANSACTION GRADIENTS
    // ===========================================

    /** Red gradient for expenses */
    val Expense = Brush.linearGradient(
        colors = listOf(ExpenseRedStart, ExpenseRedEnd)
    )

    /** Green gradient for income */
    val Income = Brush.linearGradient(
        colors = listOf(IncomeGreenStart, IncomeGreenEnd)
    )

    // ===========================================
    // GAMIFICATION GRADIENTS
    // ===========================================

    /** Gold gradient for XP and achievements */
    val Gold = Brush.linearGradient(
        colors = listOf(XpGoldStart, XpGoldEnd)
    )

    /** Fire gradient for streaks */
    val Fire = Brush.linearGradient(
        colors = listOf(StreakFireStart, StreakFireEnd)
    )

    /** Level up celebration gradient */
    val LevelUp = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD700),
            Color(0xFFFF8C00),
            Color(0xFFFF6B6B)
        )
    )

    // ===========================================
    // CARD GRADIENTS
    // ===========================================

    /** Blue card gradient */
    val CardBlue = Brush.linearGradient(
        colors = listOf(CardBlueStart, CardBlueEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Gold card gradient */
    val CardGold = Brush.linearGradient(
        colors = listOf(CardGoldStart, CardGoldEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Platinum card gradient */
    val CardPlatinum = Brush.linearGradient(
        colors = listOf(CardPlatinumStart, CardPlatinumEnd),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    /** Dark card gradient (for dark surfaces) */
    val CardDark = Brush.linearGradient(
        colors = listOf(DarkSurfaceVariant, DarkSurfaceHigh)
    )

    // ===========================================
    // BACKGROUND GRADIENTS
    // ===========================================

    /** Header background gradient */
    val Header = Brush.verticalGradient(
        colors = listOf(DarkSurface, DarkBackground)
    )

    /** Screen background with subtle gradient */
    val ScreenBackground = Brush.verticalGradient(
        colors = listOf(
            DarkSurface.copy(alpha = 0.5f),
            DarkBackground
        )
    )

    /** Premium dark gradient */
    val PremiumDark = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    )

    // ===========================================
    // SPECIAL GRADIENTS
    // ===========================================

    /** Rainbow gradient for celebrations */
    val Rainbow = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF6B6B),
            Color(0xFFFFD93D),
            Color(0xFF6BCB77),
            Color(0xFF4D96FF),
            Color(0xFF9B59B6)
        )
    )

    /** Shimmer gradient for loading states */
    val Shimmer = Brush.linearGradient(
        colors = listOf(
            DarkSurfaceVariant.copy(alpha = 0.6f),
            DarkSurfaceHigh.copy(alpha = 0.2f),
            DarkSurfaceVariant.copy(alpha = 0.6f)
        )
    )

    /** Glow effect gradient */
    val PurpleGlow = Brush.radialGradient(
        colors = listOf(
            GlowPurple,
            Color.Transparent
        )
    )

    /** Teal glow effect */
    val TealGlow = Brush.radialGradient(
        colors = listOf(
            GlowTeal,
            Color.Transparent
        )
    )

    // ===========================================
    // CATEGORY GRADIENTS
    // ===========================================

    fun getCategoryGradient(category: String): Brush {
        return when (category.lowercase()) {
            "food", "food & dining" -> Brush.linearGradient(
                listOf(Color(0xFFFF7043), Color(0xFFFF5722))
            )
            "transport", "transportation" -> Brush.linearGradient(
                listOf(Color(0xFF5C9DFF), Color(0xFF2979FF))
            )
            "shopping" -> Brush.linearGradient(
                listOf(Color(0xFFE040FB), Color(0xFFAA00FF))
            )
            "health" -> Brush.linearGradient(
                listOf(Color(0xFF00E676), Color(0xFF00C853))
            )
            "entertainment" -> Brush.linearGradient(
                listOf(Color(0xFFFFD740), Color(0xFFFFAB00))
            )
            "bills", "bills & utilities" -> Brush.linearGradient(
                listOf(Color(0xFF78909C), Color(0xFF546E7A))
            )
            "education" -> Brush.linearGradient(
                listOf(Color(0xFF18FFFF), Color(0xFF00E5FF))
            )
            "travel" -> Brush.linearGradient(
                listOf(Color(0xFFFF80AB), Color(0xFFFF4081))
            )
            "groceries" -> Brush.linearGradient(
                listOf(Color(0xFF69F0AE), Color(0xFF00E676))
            )
            else -> Primary
        }
    }

    // ===========================================
    // LEVEL GRADIENTS
    // ===========================================

    fun getLevelGradient(level: Int): Brush {
        return when (level) {
            1 -> Brush.linearGradient(listOf(Level1, Color(0xFF2196F3)))
            2 -> Brush.linearGradient(listOf(Level2, Color(0xFF00BFA5)))
            3 -> Brush.linearGradient(listOf(Level3, Color(0xFF00E676)))
            4 -> Brush.linearGradient(listOf(Level4, Color(0xFFFFAB00)))
            5 -> Brush.linearGradient(listOf(Level5, Color(0xFFFF6D00)))
            6 -> Brush.linearGradient(listOf(Level6, Color(0xFFDD2C00)))
            7 -> Brush.linearGradient(listOf(Level7, Color(0xFFAA00FF)))
            8 -> Brush.linearGradient(listOf(Level8, Color(0xFFFF8C00)))
            else -> Primary
        }
    }
}
