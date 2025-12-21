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
import com.fino.app.presentation.viewmodel.CategoryTransactionsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (Long) -> Unit = {},
    viewModel: CategoryTransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
