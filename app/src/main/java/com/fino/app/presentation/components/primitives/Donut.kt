package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.Newsreader
import kotlin.math.atan2
import kotlin.math.hypot

data class DonutSlice(val value: Float, val color: Color)

/**
 * 120dp donut chart. Segments in order of provided slices.
 * Center: value (18sp Newsreader num) / label (9sp ink-3 uppercase 1.1sp tracking).
 */
@Composable
fun Donut(
    slices: List<DonutSlice>,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp,
    onSliceClick: ((index: Int) -> Unit)? = null
) {
    val strokeGap = FinoColors.paper()
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val canvasModifier = if (onSliceClick != null) {
            Modifier
                .size(size)
                .pointerInput(slices) {
                    detectTapGestures { offset ->
                        val diameter = this.size.width.coerceAtMost(this.size.height).toFloat()
                        val strokeW = diameter * 0.2f
                        val outerR = diameter / 2f
                        val innerR = outerR - strokeW
                        val centerX = this.size.width / 2f
                        val centerY = this.size.height / 2f
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        if (dist < innerR || dist > outerR) return@detectTapGestures

                        val rawDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        val angle = ((rawDeg + 90f + 360f) % 360f)

                        var acc = 0f
                        var hitIndex = -1
                        for ((i, slice) in slices.withIndex()) {
                            val sweep = (slice.value / total) * 360f
                            if (angle >= acc && angle < acc + sweep) {
                                hitIndex = i
                                break
                            }
                            acc += sweep
                        }
                        if (hitIndex < 0 && slices.isNotEmpty()) hitIndex = slices.lastIndex
                        if (hitIndex >= 0) onSliceClick(hitIndex)
                    }
                }
        } else {
            Modifier.size(size)
        }
        Canvas(modifier = canvasModifier) {
            val diameter = this.size.minDimension
            val strokeW = diameter * 0.2f
            val radius = (diameter - strokeW) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeW)
                )
                // Draw separator
                if (slices.size > 1) {
                    val gapW = 2.dp.toPx()
                    drawArc(
                        color = strokeGap,
                        startAngle = startAngle + sweep - 0.5f,
                        sweepAngle = 1f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeW + gapW)
                    )
                }
                startAngle += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerValue,
                fontFamily = Newsreader,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = centerLabel.uppercase(),
                fontSize = 9.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.1.sp,
                color = FinoColors.ink3()
            )
        }
    }
}
