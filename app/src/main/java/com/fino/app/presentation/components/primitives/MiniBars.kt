package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import kotlin.math.max

/**
 * 28×64 mini bar chart with opacity gradient (0.5 at index 0 → 1.0 at last).
 * Used as the right-side accessory on Insight cards.
 */
@Composable
fun MiniBars(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val accent = FinoColors.accentColor()
    val maxV = max(values.maxOrNull() ?: 1f, 1f)
    Canvas(
        modifier = modifier
            .width(64.dp)
            .height(28.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        val count = values.size
        val gap = 2.dp.toPx()
        val totalGap = gap * (count - 1)
        val barW = (size.width - totalGap) / count
        values.forEachIndexed { i, v ->
            val h = (v / maxV) * size.height
            val x = i * (barW + gap)
            val y = size.height - h
            val alpha = 0.5f + 0.5f * (i.toFloat() / (count - 1).coerceAtLeast(1).toFloat())
            drawRect(
                color = accent.copy(alpha = alpha),
                topLeft = Offset(x, y),
                size = Size(barW, h),
                style = Fill
            )
        }
    }
}
