package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors

/**
 * Inline 3-option pill segmented toggle.
 * Outer: paper-2 bg, 100 radius, 3dp padding, 1px line border.
 * Active option: card bg, ink text, shadow-sm. Inactive: transparent, ink-3.
 */
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    equalWeight: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(FinoColors.paper2())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(100.dp))
            .padding(3.dp)
    ) {
        options.forEachIndexed { idx, label ->
            val selected = idx == selectedIndex
            val interactionSource = remember { MutableInteractionSource() }
            val weightMod = if (equalWeight) Modifier.weight(1f) else Modifier
            val segModifier = if (selected) {
                weightMod
                    .shadow(1.dp, RoundedCornerShape(100.dp))
                    .clip(RoundedCornerShape(100.dp))
                    .background(FinoColors.card())
            } else {
                weightMod
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.Transparent)
            }
            Box(
                modifier = segModifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelect(idx) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) FinoColors.ink() else FinoColors.ink3()
                )
            }
        }
    }
}
