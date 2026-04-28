package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter

@Composable
fun CompareDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: CompareDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    DetailScaffold(
        title = "Compare",
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CompareHero(state) }
            item { PeriodLegend(state.currentLabel, state.previousLabel) }
            item {
                Spacer(Modifier.height(4.dp))
                Eyebrow(text = "Top category changes")
            }
            if (state.rows.isEmpty()) {
                item {
                    Text(
                        text = "Not enough data to compare yet.",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            } else {
                items(state.rows) { row ->
                    DumbbellRow(row = row, maxAmount = state.maxAmount)
                }
            }
        }
    }
}

@Composable
private fun CompareHero(state: CompareDetailUiState) {
    Column {
        Eyebrow(text = "${state.currentLabel} vs ${state.previousLabel}")
        Spacer(Modifier.height(8.dp))
        val deltaPrefix = if (state.totalDelta >= 0) "+" else "−"
        val color = if (state.totalDelta >= 0) FinoColors.warn() else FinoColors.positive()
        Text(
            text = "$deltaPrefix${AmountFormatter.formatCompact(kotlin.math.abs(state.totalDelta))}",
            style = SerifHero.copy(color = color, fontSize = 40.sp, lineHeight = 46.sp)
        )
        Spacer(Modifier.height(4.dp))
        val pctStr = if (kotlin.math.abs(state.totalDeltaPercent) < 0.1f) "flat"
        else "${if (state.totalDeltaPercent >= 0) "+" else ""}${state.totalDeltaPercent.toInt()}% vs ${state.previousLabel}"
        Text(
            text = pctStr,
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun PeriodLegend(currentLabel: String, previousLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendDot(color = FinoColors.ink3(), label = previousLabel)
        LegendDot(color = FinoColors.accentColor(), label = currentLabel)
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.size(6.dp))
        Text(text = label, fontSize = 11.sp, color = FinoColors.ink3())
    }
}

@Composable
private fun DumbbellRow(row: CompareCategoryRow, maxAmount: Double) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                modifier = Modifier.weight(1f)
            )
            val sign = when {
                row.delta > 0 -> "+"
                row.delta < 0 -> "−"
                else -> ""
            }
            val deltaColor = when {
                row.delta > 0 -> FinoColors.warn()
                row.delta < 0 -> FinoColors.positive()
                else -> FinoColors.ink3()
            }
            Text(
                text = "$sign${AmountFormatter.format(kotlin.math.abs(row.delta))}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = deltaColor
            )
        }
        Spacer(Modifier.height(6.dp))
        Dumbbell(
            prevAmount = row.previousAmount,
            curAmount = row.currentAmount,
            maxAmount = maxAmount.coerceAtLeast(1.0)
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = AmountFormatter.format(row.previousAmount),
                fontSize = 11.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = AmountFormatter.format(row.currentAmount),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = FinoColors.ink()
            )
        }
    }
}

@Composable
private fun Dumbbell(prevAmount: Double, curAmount: Double, maxAmount: Double) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val trackWidth = maxWidth
        val prevFrac = (prevAmount / maxAmount).toFloat().coerceIn(0f, 1f)
        val curFrac = (curAmount / maxAmount).toFloat().coerceIn(0f, 1f)
        val leftFrac = minOf(prevFrac, curFrac)
        val rightFrac = maxOf(prevFrac, curFrac)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = 6.dp)
                .background(FinoColors.line())
        )
        Box(
            modifier = Modifier
                .size(width = trackWidth * (rightFrac - leftFrac), height = 2.dp)
                .offset(x = trackWidth * leftFrac, y = 6.dp)
                .background(FinoColors.ink3())
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(x = trackWidth * prevFrac - 5.dp, y = 2.dp)
                .clip(CircleShape)
                .background(FinoColors.ink3())
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = trackWidth * curFrac - 6.dp, y = 1.dp)
                .clip(CircleShape)
                .background(FinoColors.accentColor())
        )
    }
}
