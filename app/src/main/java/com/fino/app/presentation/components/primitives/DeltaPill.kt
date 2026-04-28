package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import kotlin.math.abs

enum class DeltaTone { Positive, Negative, Neutral }

/**
 * Small pill showing a delta (percent change vs. a previous period).
 * `invertForExpense = true` flips the tone so that a decrease in spend reads green.
 */
@Composable
fun DeltaPill(
    percent: Double,
    modifier: Modifier = Modifier,
    label: String? = null,
    invertForExpense: Boolean = false
) {
    val isUp = percent >= 0
    val tone = when {
        abs(percent) < 0.5 -> DeltaTone.Neutral
        isUp != invertForExpense -> DeltaTone.Positive
        else -> DeltaTone.Negative
    }
    val arrow = if (isUp) "\u2191" else "\u2193"
    val base = "$arrow ${String.format("%.0f", abs(percent))}%"
    val text = if (label.isNullOrBlank()) base else "$base $label"

    val (bg, fg) = when (tone) {
        DeltaTone.Positive -> FinoColors.accentSoft() to FinoColors.accentInk()
        DeltaTone.Negative -> FinoColors.warnSoft() to FinoColors.negative()
        DeltaTone.Neutral -> FinoColors.paper2() to FinoColors.ink3()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
            color = fg
        )
    }
}
