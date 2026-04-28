package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.FinoBottomNavBar
import com.fino.app.presentation.components.primitives.Donut
import com.fino.app.presentation.components.primitives.DonutSlice
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.InsightCard
import com.fino.app.presentation.components.primitives.MiniBars
import com.fino.app.presentation.components.primitives.SparkChart
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AnalyticsMetric
import com.fino.app.presentation.viewmodel.AnalyticsPeriod
import com.fino.app.presentation.viewmodel.AnalyticsViewModel
import com.fino.app.presentation.viewmodel.CategorySpending
import com.fino.app.presentation.viewmodel.InsightItem
import com.fino.app.presentation.viewmodel.InsightRoute
import com.fino.app.presentation.viewmodel.TrendBars
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToActivity: () -> Unit = {},
    onNavigateToRewards: () -> Unit = {},
    onNavigateToComparison: () -> Unit = {},
    onCategoryClick: (categoryId: Long, categoryName: String) -> Unit = { _, _ -> },
    onPaymentMethodClick: (method: String, filter: String) -> Unit = { _, _ -> },
    onAddTransaction: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToMerchant: (merchantKey: String) -> Unit = {},
    onNavigateToBill: (creditCardId: Long) -> Unit = {},
    onNavigateToDay: (epochDay: Long) -> Unit = {},
    onNavigateToCompare: () -> Unit = {},
    onNavigateToWeekend: () -> Unit = {},
    onNavigateToNewMerchants: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("insights") }
    var showDateRangePicker by remember { mutableStateOf(false) }

    val onInsightRoute: (InsightRoute) -> Unit = { route ->
        when (route) {
            is InsightRoute.Merchant -> onNavigateToMerchant(route.merchantKey)
            is InsightRoute.Bill -> onNavigateToBill(route.creditCardId)
            is InsightRoute.SubCategory -> onCategoryClick(route.categoryId, route.categoryName)
            is InsightRoute.Day -> onNavigateToDay(route.epochDay)
            InsightRoute.Subscriptions -> onNavigateToSubscriptions()
            InsightRoute.NewMerchants -> onNavigateToNewMerchants()
            InsightRoute.Weekend -> onNavigateToWeekend()
            InsightRoute.Compare -> onNavigateToCompare()
        }
    }

    Scaffold(
        containerColor = FinoColors.paper(),
        bottomBar = {
            FinoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "activity" -> onNavigateToActivity()
                        "cards" -> onNavigateToCards()
                    }
                },
                onAddClick = onAddTransaction
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.categoryBreakdown.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FinoColors.accentColor())
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    InsightsTopBar(
                        metric = uiState.metric,
                        onSelectMetric = { viewModel.setMetric(it) }
                    )
                }
                item {
                    PeriodChipRow(
                        selected = uiState.selectedPeriod,
                        onSelect = { viewModel.setPeriod(it) },
                        onCustomClick = { showDateRangePicker = true }
                    )
                }
                item {
                    Headline(
                        periodLabel = uiState.periodLabel,
                        headlineAmount = uiState.headlineAmount,
                        metric = uiState.metric,
                        trendPercent = uiState.trendPercent,
                        previousPeriodLabel = uiState.previousPeriodLabel
                    )
                }
                item {
                    TrendChart(
                        bars = uiState.trendBars,
                        onBarClick = { idx ->
                            uiState.trendBars?.epochDays?.getOrNull(idx)?.let(onNavigateToDay)
                        }
                    )
                }
                if (uiState.metric == AnalyticsMetric.SPEND && uiState.categoryBreakdown.isNotEmpty()) {
                    item {
                        CategoryBreakdownBlock(
                            categories = uiState.categoryBreakdown,
                            onCategoryClick = onCategoryClick,
                            totalSpent = uiState.totalSpent
                        )
                    }
                }
                item {
                    NoticedBlock(
                        insights = uiState.insightItems,
                        onInsightRoute = onInsightRoute,
                        period = uiState.selectedPeriod
                    )
                }
            }
        }
    }

    if (showDateRangePicker) {
        CustomDateRangeDialog(
            initialStart = uiState.customStart,
            initialEnd = uiState.customEnd,
            onDismiss = { showDateRangePicker = false },
            onConfirm = { start, end ->
                viewModel.setCustomRange(start, end)
                showDateRangePicker = false
            }
        )
    }
}

@Composable
private fun InsightsTopBar(
    metric: AnalyticsMetric,
    onSelectMetric: (AnalyticsMetric) -> Unit
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Insights",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                IconCircle(
                    icon = Icons.Outlined.Tune,
                    onClick = { showFilterMenu = true }
                )
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    modifier = Modifier.background(FinoColors.paper())
                ) {
                    MetricMenuItem(
                        label = "Spend",
                        selected = metric == AnalyticsMetric.SPEND,
                        onClick = {
                            onSelectMetric(AnalyticsMetric.SPEND)
                            showFilterMenu = false
                        }
                    )
                    MetricMenuItem(
                        label = "Net (income − spend)",
                        selected = metric == AnalyticsMetric.NET,
                        onClick = {
                            onSelectMetric(AnalyticsMetric.NET)
                            showFilterMenu = false
                        }
                    )
                }
            }
            IconCircle(icon = Icons.Filled.MoreHoriz, onClick = {})
        }
    }
}

@Composable
private fun MetricMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                fontSize = 13.sp,
                color = if (selected) FinoColors.ink() else FinoColors.ink3(),
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            )
        },
        onClick = onClick,
        trailingIcon = if (selected) {
            {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = FinoColors.ink(),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}

@Composable
private fun IconCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = FinoColors.ink(), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PeriodChipRow(
    selected: AnalyticsPeriod,
    onSelect: (AnalyticsPeriod) -> Unit,
    onCustomClick: () -> Unit
) {
    data class Chip(val label: String, val period: AnalyticsPeriod, val isCustom: Boolean = false)
    val chips = listOf(
        Chip("Week", AnalyticsPeriod.WEEK),
        Chip("Month", AnalyticsPeriod.MONTH),
        Chip("3M", AnalyticsPeriod.THREE_MONTHS),
        Chip("Year", AnalyticsPeriod.YEAR),
        Chip("Custom", AnalyticsPeriod.CUSTOM, isCustom = true)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 24.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { chip ->
            val isActive = chip.period == selected
            val textColor = if (isActive) FinoColors.paper() else FinoColors.ink3()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isActive) FinoColors.ink() else Color.Transparent)
                    .then(
                        if (isActive) Modifier
                        else Modifier.border(1.dp, FinoColors.line(), RoundedCornerShape(100.dp))
                    )
                    .clickable {
                        if (chip.isCustom) onCustomClick()
                        else onSelect(chip.period)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chip.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun Headline(
    periodLabel: String,
    headlineAmount: Double,
    metric: AnalyticsMetric,
    trendPercent: Float?,
    previousPeriodLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
    ) {
        Eyebrow(text = periodLabel)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            val sign = if (metric == AnalyticsMetric.NET && headlineAmount < 0) "−" else ""
            Text(
                text = "$sign₹${formatIndian(kotlin.math.abs(headlineAmount))}",
                style = SerifHero.copy(
                    color = FinoColors.ink(),
                    fontFeatureSettings = "tnum, cv11"
                )
            )
            if (trendPercent != null) {
                Spacer(Modifier.width(12.dp))
                val isGood = if (metric == AnalyticsMetric.SPEND) trendPercent < 0 else trendPercent > 0
                val arrow = if (trendPercent < 0) "↓" else "↑"
                val compare = previousPeriodShortLabel(previousPeriodLabel)
                val suffix = if (compare.isNotBlank()) "vs $compare" else "vs prev"
                Text(
                    text = "$arrow ${kotlin.math.abs(trendPercent.toInt())}% $suffix",
                    fontSize = 12.sp,
                    color = if (isGood) FinoColors.positive() else FinoColors.warn(),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

private fun previousPeriodShortLabel(full: String): String {
    if (full.isBlank()) return ""
    val firstWord = full.substringBefore(' ')
    return firstWord
}

private fun formatIndian(value: Double): String {
    val formatter = java.text.NumberFormat.getInstance(java.util.Locale("en", "IN"))
    return formatter.format(value.toLong())
}

@Composable
private fun TrendChart(
    bars: TrendBars?,
    onBarClick: ((index: Int) -> Unit)? = null
) {
    Box(modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)) {
        if (bars == null || bars.values.all { it <= 0f }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spend recorded for this period",
                    fontSize = 12.sp,
                    color = FinoColors.ink3()
                )
            }
        } else {
            val drillable = bars.epochDays.any { it != null } && onBarClick != null
            SparkChart(
                values = bars.values,
                todayIndex = bars.todayIndex,
                leftLabel = bars.leftLabel,
                midLabel = bars.midLabel,
                rightLabel = bars.rightLabel,
                onBarClick = if (drillable) onBarClick else null
            )
        }
    }
}

@Composable
private fun CategoryBreakdownBlock(
    categories: List<CategorySpending>,
    onCategoryClick: (Long, String) -> Unit,
    totalSpent: Double
) {
    val palette = FinoColors.chart()
    val topCats = categories.take(6)
    val slices = topCats.mapIndexed { i, cat ->
        DonutSlice(
            value = cat.percentage.coerceAtLeast(0.01f),
            color = palette.getOrElse(i) { FinoColors.ink5() }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Eyebrow(text = "By category")
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Donut(
                slices = slices,
                centerValue = "₹${formatCompactK(totalSpent)}",
                centerLabel = "Total",
                onSliceClick = { idx ->
                    topCats.getOrNull(idx)?.let { cat ->
                        onCategoryClick(cat.categoryId, cat.categoryName)
                    }
                }
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                topCats.take(4).forEachIndexed { i, cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryClick(cat.categoryId, cat.categoryName) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(palette.getOrElse(i) { FinoColors.ink5() })
                        )
                        Text(
                            text = cat.categoryName,
                            fontSize = 12.sp,
                            color = FinoColors.ink2(),
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "${(cat.percentage * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = FinoColors.ink3()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticedBlock(
    insights: List<InsightItem>,
    onInsightRoute: (InsightRoute) -> Unit,
    period: AnalyticsPeriod
) {
    val header = when (period) {
        AnalyticsPeriod.WEEK -> "Noticed this week"
        AnalyticsPeriod.MONTH -> "Noticed this month"
        AnalyticsPeriod.THREE_MONTHS -> "Noticed this quarter"
        AnalyticsPeriod.YEAR -> "Noticed this year"
        AnalyticsPeriod.CUSTOM -> "Noticed this range"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Eyebrow(text = header)
        Spacer(Modifier.height(12.dp))
        if (insights.isEmpty()) {
            InsightCard(
                title = "Nothing to flag yet",
                body = "Log a few more transactions and insights will appear here."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                insights.forEach { item ->
                    InsightCard(
                        title = item.title,
                        body = item.body,
                        isWarn = item.isWarn,
                        onClick = item.route?.let { r -> { onInsightRoute(r) } },
                        trailing = item.chartData?.let { data ->
                            { MiniBars(values = data) }
                        }
                    )
                }
            }
        }
    }
}

private fun formatCompactK(value: Double): String {
    return when {
        value >= 10_00_000 -> "${(value / 1_00_000).toInt()}L"
        value >= 1_00_000 -> {
            val lakhs = value / 1_00_000
            if (lakhs >= 10) "${lakhs.toInt()}L" else "%.1fL".format(lakhs)
        }
        value >= 1_000 -> "${(value / 1000).toInt()}k"
        else -> value.toInt().toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeDialog(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val startMillis = initialStart?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
    val endMillis = initialEnd?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startMillis,
        initialSelectedEndDateMillis = endMillis
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    val s = state.selectedStartDateMillis
                    val e = state.selectedEndDateMillis
                    if (s != null && e != null) {
                        val startDate = Instant.ofEpochMilli(s).atZone(zone).toLocalDate()
                        val endDate = Instant.ofEpochMilli(e).atZone(zone).toLocalDate()
                        onConfirm(startDate, endDate)
                    }
                }
            ) {
                Text("Apply", color = FinoColors.ink())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FinoColors.ink3())
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = FinoColors.paper()
        )
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = "Select range",
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    fontSize = 14.sp,
                    color = FinoColors.ink3()
                )
            },
            headline = {
                val startStr = state.selectedStartDateMillis
                    ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                    ?.let { "${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }.take(3)} ${it.dayOfMonth}, ${it.year}" }
                    ?: "Start"
                val endStr = state.selectedEndDateMillis
                    ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                    ?.let { "${it.month.name.lowercase().replaceFirstChar { c -> c.uppercase() }.take(3)} ${it.dayOfMonth}, ${it.year}" }
                    ?: "End"
                Text(
                    text = "$startStr  →  $endStr",
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink()
                )
            },
            showModeToggle = false
        )
    }
}
