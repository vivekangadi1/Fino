package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.UpcomingBill
import com.fino.app.presentation.components.FinoBottomNavBar
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.IconBtn
import com.fino.app.presentation.components.primitives.Mini
import com.fino.app.presentation.components.primitives.ReviewItem
import com.fino.app.presentation.components.primitives.UpcomingItem
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle
import com.fino.app.presentation.viewmodel.HomeViewModel
import com.fino.app.util.AmountFormatter
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max

private val headerDateFormatter = DateTimeFormatter.ofPattern("EEEE \u00B7 MMM d")

@Composable
fun HomeScreen(
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToActivity: () -> Unit = {},
    onNavigateToRewards: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToUpcomingBills: () -> Unit,
    onAddRecurringBill: () -> Unit,
    onEditRecurringBill: (Long) -> Unit = {},
    onEditTransaction: (Long) -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToEventDetail: (Long) -> Unit = {},
    onNavigateToReviewUncategorized: () -> Unit = {},
    onNavigateToPeriodTransactions: (Long, Long, String) -> Unit = { _, _, _ -> },
    onNavigateToTypeTransactions: (String, String) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = FinoColors.paper(),
        bottomBar = {
            FinoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                    when (route) {
                        "activity" -> onNavigateToActivity()
                        "insights" -> onNavigateToAnalytics()
                        "cards" -> onNavigateToCards()
                    }
                },
                onAddClick = onAddTransaction
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { HomeTopBar(onNavigateToSettings = onNavigateToSettings) }
            item {
                HomeHero(
                    monthlySpent = state.monthlySpent,
                    budget = BUDGET_PLACEHOLDER
                )
            }
            item {
                HomeMiniGrid(
                    monthlySpent = state.monthlySpent,
                    monthlySaved = state.monthlySaved
                )
            }
            item {
                HomeUpNext(
                    bills = state.nextBills.take(3),
                    onSeeAll = onNavigateToUpcomingBills,
                    onBillClick = { bill ->
                        if (bill.sourceId > 0) onEditRecurringBill(bill.sourceId)
                        else onNavigateToUpcomingBills()
                    }
                )
            }
            item {
                HomeNeedsAGlance(
                    count = state.uncategorizedCount,
                    items = state.uncategorizedTransactions.take(2),
                    onReviewAll = onNavigateToReviewUncategorized,
                    onItemClick = onEditTransaction
                )
            }
            item {
                HomeAccounts(
                    totalBalance = state.totalBalance,
                    accounts = state.accounts,
                    onManage = onNavigateToCards
                )
            }
        }
    }
}

// Hard-coded monthly budget placeholder until budget feature exists.
private const val BUDGET_PLACEHOLDER = 120_000.0

@Composable
private fun HomeTopBar(onNavigateToSettings: () -> Unit = {}) {
    val today = LocalDate.now()
    val dateLabel = today.format(headerDateFormatter)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 18.dp, top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Eyebrow(text = dateLabel)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Good ${timeOfDay()}",
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconBtn(icon = Icons.Outlined.Search, contentDescription = "Search", onClick = {})
            IconBtn(
                icon = Icons.Outlined.Settings,
                contentDescription = "Settings",
                onClick = onNavigateToSettings
            )
            IconBtn(
                icon = Icons.Outlined.NotificationsNone,
                contentDescription = "Notifications",
                onClick = {},
                showDot = true
            )
        }
    }
}

private fun timeOfDay(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}

@Composable
private fun HomeHero(monthlySpent: Double, budget: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp, bottom = 22.dp)
    ) {
        Eyebrow(text = "Spent this month")
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "\u20B9${formatAmount(monthlySpent)}",
                fontFamily = Newsreader,
                fontSize = 54.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1.62).sp,
                color = FinoColors.ink(),
                style = NumericStyle
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "of \u20B9${formatAmount(budget)}",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = FinoColors.ink3(),
                style = NumericStyle,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        BudgetBar(spent = monthlySpent, budget = budget)
        Spacer(Modifier.height(10.dp))
        val usedPct = ((monthlySpent / budget) * 100).coerceIn(0.0, 100.0).toInt()
        val daysLeft = max(
            0,
            YearMonth.now().lengthOfMonth() - LocalDate.now().dayOfMonth
        )
        val remaining = (budget - monthlySpent).coerceAtLeast(0.0)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${usedPct}% used \u00B7 $daysLeft days left",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3()
            )
            Text(
                text = "\u20B9${formatAmount(remaining)} left",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink2(),
                style = NumericStyle
            )
        }
    }
}

@Composable
private fun BudgetBar(spent: Double, budget: Double) {
    val chart = FinoColors.chart()
    val ratio = (spent / budget).coerceIn(0.0, 1.0).toFloat()
    val weights = listOf(0.40f, 0.26f, 0.20f, 0.14f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(FinoColors.paper3())
    ) {
        if (ratio > 0f) {
            weights.forEachIndexed { i, w ->
                val frac = (w * ratio).coerceAtLeast(0.0001f)
                if (i > 0) Spacer(Modifier.width(1.dp))
                Box(
                    modifier = Modifier
                        .weight(frac)
                        .height(6.dp)
                        .background(chart[i])
                )
            }
        }
        val rest = (1f - ratio).coerceAtLeast(0.0001f)
        Box(modifier = Modifier.weight(rest))
    }
}

@Composable
private fun HomeMiniGrid(monthlySpent: Double, monthlySaved: Double) {
    val daysInMonth = LocalDate.now().dayOfMonth.coerceAtLeast(1)
    val dailyAvg = monthlySpent / daysInMonth
    val forecast = dailyAvg * YearMonth.now().lengthOfMonth()
    val forecastOnTrack = forecast <= BUDGET_PLACEHOLDER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Mini(
            label = "Daily avg",
            value = "\u20B9${formatCompact(dailyAvg)}",
            delta = "\u2212",
            modifier = Modifier.weight(1f)
        )
        Mini(
            label = "Forecast",
            value = "\u20B9${formatCompact(forecast)}",
            delta = if (forecastOnTrack) "On track" else "Over",
            modifier = Modifier.weight(1f)
        )
        Mini(
            label = "Saved",
            value = "\u20B9${formatCompact(monthlySaved)}",
            delta = "+",
            positive = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeUpNext(
    bills: List<UpcomingBill>,
    onSeeAll: () -> Unit,
    onBillClick: (UpcomingBill) -> Unit
) {
    SectionHeader(
        title = "Up next",
        action = "See all",
        onAction = onSeeAll
    )
    if (bills.isEmpty()) {
        EmptyRow(text = "No upcoming bills")
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            bills.forEach { bill ->
                HorizontalLine()
                val daysToDue = ChronoUnit.DAYS.between(LocalDate.now(), bill.dueDate)
                val meta = buildBillMeta(bill, daysToDue)
                UpcomingItem(
                    name = bill.displayName.ifBlank { bill.merchantName },
                    meta = meta,
                    amount = "\u20B9${formatAmount(bill.amount)}",
                    isDue = daysToDue in 0..2,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    onClick = { onBillClick(bill) }
                )
            }
        }
    }
}

private fun buildBillMeta(bill: UpcomingBill, daysToDue: Long): String {
    val phrase = when {
        daysToDue < 0 -> "Overdue"
        daysToDue == 0L -> "Due today"
        daysToDue == 1L -> "Due tomorrow"
        else -> "in $daysToDue days"
    }
    val channel = when (bill.source) {
        com.fino.app.domain.model.BillSource.CREDIT_CARD -> "Card${bill.creditCardLastFour?.let { " \u00B7 $it" } ?: ""}"
        com.fino.app.domain.model.BillSource.RECURRING_RULE -> "Subscription"
        com.fino.app.domain.model.BillSource.PATTERN_SUGGESTION -> "Suggested"
    }
    return "$channel \u00B7 $phrase"
}

@Composable
private fun HomeNeedsAGlance(
    count: Int,
    items: List<Transaction>,
    onReviewAll: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    if (count == 0) return
    SectionHeader(
        title = "Needs a glance",
        badge = count.toString(),
        action = "Review all",
        onAction = onReviewAll
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { txn ->
            HorizontalLine()
            val time = txn.transactionDate.format(
                DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.ENGLISH)
            )
            val source = listOfNotNull(
                txn.bankName,
                txn.paymentMethod
            ).joinToString(" \u00B7 ").ifBlank { "SMS" }
            ReviewItem(
                modifier = Modifier.padding(horizontal = 24.dp),
                merchant = txn.merchantNormalized ?: txn.merchantName.ifBlank { "Unknown" },
                amount = "\u2212\u20B9${formatAmount(txn.amount)}",
                meta = "$time \u00B7 $source",
                hint = if (txn.needsReview) "New merchant. Pick a category." else "Looks unclassified",
                action = "Categorize \u2192",
                isUnmatched = true,
                onHintClick = { onItemClick(txn.id) }
            )
        }
    }
}

@Composable
private fun HomeAccounts(
    totalBalance: Double,
    accounts: List<com.fino.app.presentation.viewmodel.AccountRow>,
    onManage: () -> Unit
) {
    SectionHeader(
        title = "Accounts",
        action = "Manage",
        onAction = onManage
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalLine()
        AccountTile(
            name = "Total balance",
            type = "Across all accounts",
            balance = "\u20B9${formatAmount(totalBalance)}",
            warn = totalBalance < 0
        )
        accounts.forEach { row ->
            HorizontalLine()
            AccountTile(
                name = row.displayName,
                type = row.type,
                balance = "\u20B9${formatAmount(row.balance)}",
                warn = row.balance < 0
            )
        }
    }
}

@Composable
private fun AccountTile(
    name: String,
    type: String,
    balance: String,
    warn: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FinoColors.paper2())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.CreditCard,
                contentDescription = null,
                tint = FinoColors.ink2(),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = type,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3()
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = balance,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (warn) FinoColors.negative() else FinoColors.ink(),
            style = NumericStyle
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    badge: String? = null,
    onAction: (() -> Unit)? = null
) {
    Spacer(Modifier.height(32.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.13).sp,
                color = FinoColors.ink()
            )
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(FinoColors.accentColor())
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinoColors.paper()
                    )
                }
            }
        }
        if (action != null) {
            val actionMod = if (onAction != null) Modifier.clickable { onAction() } else Modifier
            Text(
                text = action,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                modifier = actionMod
            )
        }
    }
}

@Composable
private fun HorizontalLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(1.dp)
            .background(FinoColors.line())
    )
}

@Composable
private fun EmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

private fun formatAmount(value: Double): String =
    AmountFormatter.format(value)

private fun formatCompact(value: Double): String {
    val abs = kotlin.math.abs(value)
    return when {
        abs >= 100_000.0 -> String.format(Locale.ENGLISH, "%.1fL", value / 100_000.0)
        abs >= 1_000.0 -> String.format(Locale.ENGLISH, "%.1fk", value / 1_000.0)
        else -> String.format(Locale.ENGLISH, "%,.0f", value)
    }
}
