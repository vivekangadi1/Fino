package com.fino.app.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.ExportFormat
import com.fino.app.domain.model.ExportResult
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AnalyticsPeriod
import com.fino.app.presentation.viewmodel.AnalyticsViewModel
import com.fino.app.presentation.viewmodel.CategorySpending
import kotlinx.coroutines.launch
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onNavigateToComparison: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("analytics") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPeriodPicker by remember { mutableStateOf(false) }

    // Load heavy analytics data lazily and staggered to prevent frame drops
    LaunchedEffect(Unit) {
        // Stagger loading to prevent all heavy operations at once
        kotlinx.coroutines.delay(500) // Let initial UI render first
        viewModel.loadTrendData()
        kotlinx.coroutines.delay(300)
        viewModel.loadYearOverYearData()
        kotlinx.coroutines.delay(300)
        viewModel.loadPaymentMethodTrend()
        kotlinx.coroutines.delay(300)
        viewModel.loadSpendingHeatmapData()
    }

    // Handle export and share
    fun handleExport(format: ExportFormat) {
        scope.launch {
            when (val result = viewModel.exportCurrentPeriod(format)) {
                is ExportResult.Success -> {
                    // Share the exported file
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = if (format == ExportFormat.CSV) "text/csv" else "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, result.fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share ${format.name} Export"))

                    snackbarHostState.showSnackbar(
                        message = "Exported successfully: ${result.fileName}",
                        duration = SnackbarDuration.Short
                    )
                }
                is ExportResult.Error -> {
                    snackbarHostState.showSnackbar(
                        message = "Export failed: ${result.error}",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            FinoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "cards" -> onNavigateToCards()
                        "rewards" -> onNavigateToRewards()
                    }
                }
            )
        }
    ) { paddingValues ->
        // Period Picker Dialog
        if (showPeriodPicker) {
            PeriodPickerDialog(
                currentSelection = YearMonth.from(uiState.selectedDate),
                monthlySpending = uiState.spendingHeatmapData,
                onDismiss = { showPeriodPicker = false },
                onPeriodSelected = { yearMonth ->
                    viewModel.updateSelectedDate(yearMonth.atDay(1))
                }
            )
        }

        SwipeableContent(
            currentKey = uiState.periodLabel,
            onSwipeLeft = {
                if (uiState.canNavigateForward) {
                    viewModel.navigateToNextPeriod()
                }
            },
            onSwipeRight = { viewModel.navigateToPreviousPeriod() }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header
                item {
                    AnalyticsHeader(
                        onExportCsv = { handleExport(ExportFormat.CSV) },
                        onExportPdf = { handleExport(ExportFormat.PDF) }
                    )
                }

                // Period Selector
                item {
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.setPeriod(it) }
                    )
                }

                // Period Navigation Header
                item {
                    PeriodNavigationHeader(
                        periodLabel = uiState.periodLabel,
                        canNavigateBackward = uiState.canNavigateBackward,
                        canNavigateForward = uiState.canNavigateForward,
                        onNavigateBackward = { viewModel.navigateToPreviousPeriod() },
                        onNavigateForward = { viewModel.navigateToNextPeriod() },
                        onNavigateToCurrent = { viewModel.navigateToCurrentPeriod() },
                        onOpenPeriodPicker = { showPeriodPicker = true },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // Period Jump Shortcuts
                item {
                    PeriodJumpChips(
                        onJumpToLastMonth = { viewModel.jumpToLastMonth() },
                        onJumpTo3MonthsAgo = { viewModel.jumpTo3MonthsAgo() },
                        onJumpToLastYear = { viewModel.jumpToLastYear() },
                        onJumpToSameMonthLastYear = { viewModel.jumpToSameMonthLastYear() },
                        onCompareMonths = onNavigateToComparison
                    )
                }

                // Summary Cards
                item {
                    SummaryCardsRow(
                        totalSpent = uiState.totalSpent,
                        transactionCount = uiState.transactionCount
                    )
                }

                // Trend Chart
                uiState.spendingTrend?.let { trend ->
                    item {
                        TrendLineChart(
                            trendData = trend,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // Year-over-Year Chart
                uiState.yearOverYearComparison?.let { yoyComparison ->
                    item {
                        YoYChart(
                            yoyComparison = yoyComparison,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // Payment Method Trend Chart
                uiState.paymentMethodTrend?.let { paymentMethodTrend ->
                    item {
                        PaymentMethodTrendChart(
                            paymentMethodTrend = paymentMethodTrend,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // Chart Placeholder
                item {
                    ChartSection(categoryBreakdown = uiState.categoryBreakdown)
                }

                // Category Breakdown
                item {
                    CategoryBreakdownSection(categoryBreakdown = uiState.categoryBreakdown)
                }

                // Payment Method Breakdown
                item {
                    PaymentMethodSection(paymentMethodBreakdown = uiState.paymentMethodBreakdown)
                }

                // Insights
                item {
                    InsightsSection()
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(
    onExportCsv: () -> Unit = {},
    onExportPdf: () -> Unit = {}
) {
    var showExportMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurface, DarkBackground)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Understand your spending habits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Export button with dropdown menu
            Box {
                IconButton(
                    onClick = { showExportMenu = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Export",
                        tint = TextPrimary
                    )
                }

                DropdownMenu(
                    expanded = showExportMenu,
                    onDismissRequest = { showExportMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Export as CSV", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportCsv()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Export as PDF", color = TextPrimary)
                            }
                        },
                        onClick = {
                            showExportMenu = false
                            onExportPdf()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    val periods = listOf(
        AnalyticsPeriod.WEEK to "Week",
        AnalyticsPeriod.MONTH to "Month",
        AnalyticsPeriod.YEAR to "Year"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        periods.forEach { (period, label) ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Primary else Color.Transparent
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PeriodNavigationHeader(
    periodLabel: String,
    canNavigateBackward: Boolean,
    canNavigateForward: Boolean,
    onNavigateBackward: () -> Unit,
    onNavigateForward: () -> Unit,
    onNavigateToCurrent: () -> Unit,
    onOpenPeriodPicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SlideInCard(delay = 50, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                IconButton(
                    onClick = onNavigateBackward,
                    enabled = canNavigateBackward,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous period",
                        tint = if (canNavigateBackward) TextPrimary else TextTertiary
                    )
                }

                // Period label (clickable to open period picker)
                TextButton(
                    onClick = onOpenPeriodPicker,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = periodLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Open period picker",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Next button
                IconButton(
                    onClick = onNavigateForward,
                    enabled = canNavigateForward,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next period",
                        tint = if (canNavigateForward) TextPrimary else TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCardsRow(
    totalSpent: Double,
    transactionCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Spent
        SlideInCard(delay = 100, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FinoGradients.Expense)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedCounter(
                        targetValue = totalSpent.toInt(),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        prefix = "₹",
                        formatAsRupees = true
                    )
                }
            }
        }

        // Transactions
        SlideInCard(delay = 150, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AnimatedCounter(
                        targetValue = transactionCount,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartSection(categoryBreakdown: List<CategorySpending>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Spending by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        SlideInCard(delay = 200) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (categoryBreakdown.isEmpty()) {
                    // Empty state
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No data to display",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Start tracking expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                } else {
                    // Show category breakdown legend
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simple donut representation
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        categoryBreakdown.flatMap {
                                            listOf(it.color, it.color)
                                        }.ifEmpty { listOf(DarkSurfaceHigh, DarkSurfaceHigh) }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${categoryBreakdown.size}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Categories",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Legend
                        Column(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryBreakdown.take(4).forEach { category ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(category.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${category.emoji} ${category.categoryName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                            if (categoryBreakdown.size > 4) {
                                Text(
                                    text = "+${categoryBreakdown.size - 4} more",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(categoryBreakdown: List<CategorySpending>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "This Period",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (categoryBreakdown.isEmpty()) {
            // Show placeholder categories when no data
            val placeholders = listOf(
                CategoryItem("🍔", "Food & Dining", CategoryFood, 0f),
                CategoryItem("🚗", "Transport", CategoryTransport, 0f),
                CategoryItem("🛍️", "Shopping", CategoryShopping, 0f),
                CategoryItem("🎬", "Entertainment", CategoryEntertainment, 0f),
                CategoryItem("📱", "Bills", CategoryBills, 0f)
            )

            placeholders.forEachIndexed { index, category ->
                SlideInCard(delay = 300 + (index * 50)) {
                    CategoryRow(category)
                }
                if (index < placeholders.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Show real category data
            categoryBreakdown.forEachIndexed { index, spending ->
                SlideInCard(delay = 300 + (index * 50)) {
                    SpendingCategoryRow(spending)
                }
                if (index < categoryBreakdown.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Progress bar
                AnimatedGradientProgress(
                    progress = category.percentage,
                    gradient = Brush.linearGradient(
                        listOf(category.color, category.color.copy(alpha = 0.7f))
                    ),
                    backgroundColor = DarkSurfaceHigh,
                    height = 4.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "₹0",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun SpendingCategoryRow(spending: CategorySpending) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(spending.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = spending.emoji,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = spending.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(spending.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Progress bar
                AnimatedGradientProgress(
                    progress = spending.percentage,
                    gradient = Brush.linearGradient(
                        listOf(spending.color, spending.color.copy(alpha = 0.7f))
                    ),
                    backgroundColor = DarkSurfaceHigh,
                    height = 4.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "₹${spending.amount.toInt()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun InsightsSection() {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SparkleEffect()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        SlideInCard(delay = 500) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Secondary.copy(alpha = 0.15f),
                                Primary.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pro Tip",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Accent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant SMS access to automatically track your expenses and get personalized insights!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BouncyButton(
                        onClick = { },
                        gradient = FinoGradients.Secondary
                    ) {
                        Text(
                            text = "Enable SMS Access",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryItem(
    val emoji: String,
    val name: String,
    val color: Color,
    val percentage: Float
)
