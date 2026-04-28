package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.components.AnimatedEmptyState
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.CategoryMonthlyBar
import com.fino.app.presentation.viewmodel.CategoryTopMerchantRow
import com.fino.app.presentation.viewmodel.CategoryTransactionsUiState
import com.fino.app.presentation.viewmodel.CategoryTransactionsViewModel
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (Long) -> Unit = {},
    onOpenMerchant: (String) -> Unit = {},
    viewModel: CategoryTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.categoryEmoji.isNotEmpty()) {
                                Text(
                                    text = uiState.categoryEmoji,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Text(
                                text = uiState.categoryName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${uiState.transactionCount} transactions • ₹${String.format("%,.0f", uiState.totalAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedEmptyState(
                            emoji = "📭",
                            title = "No transactions",
                            subtitle = "No transactions found for this category"
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.monthlyBudget != null) {
                            item {
                                BudgetProgressCard(uiState)
                            }
                        }
                        if (uiState.monthlyBars.isNotEmpty()) {
                            item {
                                MonthlyTrendCard(bars = uiState.monthlyBars)
                            }
                        }
                        if (uiState.topMerchants.isNotEmpty()) {
                            item {
                                TopMerchantsHeader()
                            }
                            items(
                                items = uiState.topMerchants,
                                key = { "tm_${it.merchantKey}" }
                            ) { row ->
                                TopMerchantRow(
                                    row = row,
                                    onClick = { onOpenMerchant(row.merchantKey) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recent transactions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                        items(
                            items = uiState.transactions,
                            key = { it.id }
                        ) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Blue color for Savings
private val SavingsBlue = Color(0xFF4A90D9)

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm") }

    data class TransactionStyle(
        val bgColor: Color,
        val iconColor: Color,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val prefix: String
    )

    val style = when (transaction.type) {
        TransactionType.DEBIT -> TransactionStyle(
            bgColor = ExpenseRed.copy(alpha = 0.2f),
            iconColor = ExpenseRed,
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            prefix = "-"
        )
        TransactionType.CREDIT -> TransactionStyle(
            bgColor = IncomeGreen.copy(alpha = 0.2f),
            iconColor = IncomeGreen,
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            prefix = "+"
        )
        TransactionType.SAVINGS -> TransactionStyle(
            bgColor = SavingsBlue.copy(alpha = 0.2f),
            iconColor = SavingsBlue,
            icon = Icons.Outlined.Savings,
            prefix = ""
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(style.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchantName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = transaction.transactionDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Text(
                text = "${style.prefix}₹${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = style.iconColor
            )
        }
    }
}

@Composable
private fun BudgetProgressCard(state: CategoryTransactionsUiState) {
    val limit = state.monthlyBudget ?: return
    val pct = state.budgetPercent.coerceAtLeast(0f)
    val barColor = if (state.isOverBudget) ExpenseRed else IncomeGreen
    val remaining = (limit - state.thisMonthSpent).coerceAtLeast(0.0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This month",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            val label = if (state.isOverBudget) "Over budget" else "${(pct * 100).toInt()}% used"
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (state.isOverBudget) ExpenseRed else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${AmountFormatter.format(state.thisMonthSpent)} of ${AmountFormatter.format(limit)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { pct.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = DarkSurfaceHigh
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (state.isOverBudget)
                "Over by ${AmountFormatter.format(state.thisMonthSpent - limit)}"
            else
                "${AmountFormatter.format(remaining)} left",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.isOverBudget) ExpenseRed else TextSecondary
        )
    }
}

@Composable
private fun MonthlyTrendCard(bars: List<CategoryMonthlyBar>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = "6-month trend",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val h = (bar.normalized.coerceAtLeast(0.05f) * 60).dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(h)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Primary.copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TopMerchantsHeader() {
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Top merchants",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary
    )
}

@Composable
private fun TopMerchantRow(row: CategoryTopMerchantRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = if (row.count == 1) "1 visit" else "${row.count} visits",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Text(
            text = AmountFormatter.format(row.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
