package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun WeekendDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeekendDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    DetailScaffold(
        title = "Weekend spending",
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WeekendHero(state) }
            item { KpiTiles(state) }
            item {
                Spacer(Modifier.height(4.dp))
                Eyebrow(text = "Day of week")
            }
            item { DayOfWeekBars(state.dayBars) }
            if (state.topCategoryMultipliers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Eyebrow(text = "Heavier on weekends")
                }
                items(state.topCategoryMultipliers) { row ->
                    CategoryMultiplierItem(row)
                }
            }
        }
    }
}

@Composable
private fun WeekendHero(state: WeekendDetailUiState) {
    Column {
        Eyebrow(text = state.periodLabel.ifBlank { "Weekends" })
        Spacer(Modifier.height(8.dp))
        val ratioStr = if (state.ratio > 0) "%.1f×".format(state.ratio) else "—"
        Text(
            text = ratioStr,
            style = SerifHero.copy(color = FinoColors.ink(), fontSize = 40.sp, lineHeight = 46.sp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Sat–Sun daily avg vs Mon–Fri daily avg.",
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun KpiTiles(state: WeekendDetailUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KpiTile(
            modifier = Modifier.weight(1f),
            label = "Weekday avg/day",
            value = AmountFormatter.format(state.weekdayAvg)
        )
        KpiTile(
            modifier = Modifier.weight(1f),
            label = "Weekend avg/day",
            value = AmountFormatter.format(state.weekendAvg),
            accent = true
        )
    }
}

@Composable
private fun KpiTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = FinoColors.ink3(),
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (accent) FinoColors.accentColor() else FinoColors.ink()
        )
    }
}

@Composable
private fun DayOfWeekBars(bars: List<DayOfWeekBar>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        bars.forEach { bar ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val heightDp = (bar.normalized.coerceAtLeast(0.04f) * 80).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightDp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (bar.isWeekend) FinoColors.accentColor() else FinoColors.ink5()
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = bar.shortLabel,
                    fontSize = 10.sp,
                    color = if (bar.isWeekend) FinoColors.ink() else FinoColors.ink3(),
                    fontWeight = if (bar.isWeekend) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CategoryMultiplierItem(row: CategoryMultiplierRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Wkday ${AmountFormatter.format(row.weekdayAvg)} · Wkend ${AmountFormatter.format(row.weekendAvg)}",
                fontSize = 11.sp,
                color = FinoColors.ink3()
            )
        }
        Text(
            text = "%.1f×".format(row.multiplier),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.accentColor()
        )
    }
}
