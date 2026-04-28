package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors

enum class PillVariant { Solid, Default, Ghost }

/**
 * 100dp-radius pill chip with solid / default / ghost variants.
 * Solid: ink bg, paper text. Default: card bg, ink text, line-2 border. Ghost: transparent, no border.
 */
@Composable
fun Pill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillVariant = PillVariant.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bg = when (variant) {
        PillVariant.Solid -> FinoColors.ink()
        PillVariant.Default -> FinoColors.card()
        PillVariant.Ghost -> Color.Transparent
    }
    val fg = when (variant) {
        PillVariant.Solid -> FinoColors.paper()
        PillVariant.Default -> FinoColors.ink2()
        PillVariant.Ghost -> FinoColors.ink3()
    }
    val borderMod = when (variant) {
        PillVariant.Solid -> Modifier
        PillVariant.Default -> Modifier.border(1.dp, FinoColors.line2(), RoundedCornerShape(100.dp))
        PillVariant.Ghost -> Modifier
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .then(borderMod)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}
