package com.fino.app.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.fino.app.presentation.theme.tokens.*

/**
 * In the quiet-luxury redesign, gradients collapse to calm solid fills.
 * The Brush API is preserved so existing call sites compile unchanged.
 */
object FinoGradients {
    private fun solid(color: Color): Brush = Brush.linearGradient(listOf(color, color))

    val Primary: Brush = solid(AccentLight)
    val PrimaryDiagonal: Brush = solid(AccentLight)
    val PrimaryRadial: Brush = solid(AccentSoftLight)

    val Secondary: Brush = solid(Ink2Light)
    val SecondaryDiagonal: Brush = solid(Ink2Light)

    val Expense: Brush = solid(NegativeLight)
    val Income: Brush = solid(AccentLight)

    val Gold: Brush = solid(InkLight)
    val Fire: Brush = solid(WarnLight)
    val LevelUp: Brush = solid(AccentLight)

    val CardBlue: Brush = solid(Paper2Light)
    val CardGold: Brush = solid(CardTintLight)
    val CardPlatinum: Brush = solid(Paper3Light)
    val CardDark: Brush = solid(Paper2Dark)

    val Header: Brush = solid(PaperLight)
    val ScreenBackground: Brush = solid(PaperLight)
    val PremiumDark: Brush = solid(PaperDark)

    val Rainbow: Brush = solid(AccentLight)
    val Shimmer: Brush = Brush.linearGradient(listOf(Paper2Light, Paper3Light, Paper2Light))
    val PurpleGlow: Brush = Brush.radialGradient(listOf(AccentSoftLight, Color.Transparent))
    val TealGlow: Brush = Brush.radialGradient(listOf(AccentSoftLight, Color.Transparent))

    fun getCategoryGradient(category: String): Brush {
        val color = when (category.lowercase()) {
            "food", "food & dining" -> C3Light
            "transport", "transportation" -> C2Light
            "shopping" -> C4Light
            "health" -> C1Light
            "entertainment" -> C6Light
            "bills", "bills & utilities" -> Ink3Light
            "education" -> C5Light
            "travel" -> C4Light
            "groceries" -> C1Light
            else -> AccentLight
        }
        return solid(color)
    }

    fun getLevelGradient(level: Int): Brush = solid(AccentLight)
}
