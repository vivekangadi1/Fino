package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import kotlin.math.max

/**
 * 80dp-tall 30-bar chart. Today's index is highlighted in accent; past = ink-5; future = ink-5 at 0.4 opacity.
 * Below: labels "Apr 1" / "Apr 15" / "Apr 30" (or any 3 caller-supplied labels).
 */
@Composable
fun SparkChart(
    values: List<Float>,
    todayIndex: Int,
    leftLabel: String,
    midLabel: String,
    rightLabel: String,
    modifier: Modifier = Modifier,
    onBarClick: ((index: Int) -> Unit)? = null
) {
    val accent = FinoColors.accentColor()
    val muted = FinoColors.ink5()
    val maxV = max(values.maxOrNull() ?: 1f, 1f)
    Column(modifier = modifier.fillMaxWidth()) {
        val canvasMod = if (onBarClick != null) {
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .pointerInput(values.size) {
                    detectTapGestures { offset ->
                        val count = values.size
                        if (count == 0) return@detectTapGestures
                        val gap = 2.dp.toPx()
                        val totalGap = gap * (count - 1)
                        val barW = (this.size.width - totalGap) / count
                        val slotW = barW + gap
                        val idx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                        onBarClick(idx)
                    }
                }
        } else {
            Modifier
                .fillMaxWidth()
                .height(80.dp)
        }
        Canvas(modifier = canvasMod) {
            val count = values.size
            val gap = 2.dp.toPx()
            val totalGap = gap * (count - 1)
            val barW = (size.width - totalGap) / count
            values.forEachIndexed { i, v ->
                val h = (v / maxV) * size.height
                val x = i * (barW + gap)
                val y = size.height - h
                val isToday = i == todayIndex
                val isFuture = i > todayIndex
                val color = when {
                    isToday -> accent
                    isFuture -> muted.copy(alpha = 0.4f)
                    else -> muted
                }
                drawBar(x, y, barW, h, color)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(leftLabel, fontSize = 10.sp, color = FinoColors.ink4())
            Text(midLabel, fontSize = 10.sp, color = FinoColors.ink4())
            Text(rightLabel, fontSize = 10.sp, color = FinoColors.ink4())
        }
    }
}

private fun DrawScope.drawBar(x: Float, y: Float, w: Float, h: Float, color: androidx.compose.ui.graphics.Color) {
    drawRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        style = Fill
    )
}
