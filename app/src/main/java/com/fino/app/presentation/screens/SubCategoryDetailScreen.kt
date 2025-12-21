package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.PaymentStatus
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.SubCategoryDetailViewModel
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryDetailScreen(
    onNavigateBack: () -> Unit,
    onEditSubCategory: () -> Unit,
    onAddExpense: () -> Unit,
    onTransactionClick: (Long) -> Unit = {},
    viewModel: SubCategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.subCategory?.name ?: "Sub-Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditSubCategory) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Primary,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header with emoji and summary
                item {
                    SubCategoryHeader(
                        emoji = uiState.subCategory?.emoji ?: "",
                        name = uiState.subCategory?.name ?: "",
                        budgetAmount = uiState.subCategory?.budgetAmount,
                        totalPaid = uiState.totalPaid,
                        totalPending = uiState.totalPending,
                        transactionCount = uiState.transactions.size,
                        onSetBudget = { viewModel.showBudgetDialog() }
                    )
                }

                // Vendors section (if any)
                if (uiState.vendors.isNotEmpty()) {
                    item {
                        VendorsSummaryRow(
                            vendorCount = uiState.vendors.size,
                            vendorNames = uiState.vendors.map { it.name }
                        )
                    }
                }

                // Transactions header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${uiState.transactions.size} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Empty state or transactions list
                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyTransactionsState(
                            subCategoryName = uiState.subCategory?.name ?: "this sub-category"
                        )
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { transaction ->
                        SubCategoryTransactionRow(
                            transaction = transaction,
                            vendorName = transaction.eventVendorId?.let { uiState.vendorNames[it] },
                            onClick = { onTransactionClick(transaction.id) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Budget Dialog
        if (uiState.showBudgetDialog) {
            SetBudgetDialog(
                currentBudget = uiState.budgetInput,
                onBudgetChange = { viewModel.setBudgetInput(it) },
                onSave = { viewModel.saveBudget() },
                onDismiss = { viewModel.hideBudgetDialog() },
                isSaving = uiState.isSaving,
                error = uiState.error
            )
        }
    }
}

@Composable
private fun SubCategoryHeader(
    emoji: String,
    name: String,
    budgetAmount: Double?,
    totalPaid: Double,
    totalPending: Double,
    transactionCount: Int,
    onSetBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurface, DarkBackground)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryStart.copy(alpha = 0.3f), PrimaryEnd.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 36.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$transactionCount transaction${if (transactionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Budget and spending summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp)
            ) {
                Column {
                    // Budget row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            if (budgetAmount != null && budgetAmount > 0) {
                                Text(
                                    text = AmountFormatter.format(budgetAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            } else {
                                Text(
                                    text = "Not set",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextTertiary
                                )
                            }
                        }

                        TextButton(onClick = onSetBudget) {
                            Icon(
                                if (budgetAmount != null && budgetAmount > 0) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (budgetAmount != null && budgetAmount > 0) "Edit" else "Set Budget",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    if (budgetAmount != null && budgetAmount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar
                        val progress = (totalPaid / budgetAmount).toFloat().coerceIn(0f, 1f)
                        val isOverBudget = totalPaid > budgetAmount

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isOverBudget) ExpenseRed else IncomeGreen,
                            trackColor = DarkSurfaceHigh
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${String.format("%.1f", (totalPaid / budgetAmount) * 100)}% used",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverBudget) ExpenseRed else TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkSurfaceHigh)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Paid and Pending row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Paid",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = AmountFormatter.format(totalPaid),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(DarkSurfaceHigh)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = AmountFormatter.format(totalPending),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPending > 0) Warning else TextTertiary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(DarkSurfaceHigh)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = AmountFormatter.format(totalPaid + totalPending),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorsSummaryRow(
    vendorCount: Int,
    vendorNames: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$vendorCount Vendor${if (vendorCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = vendorNames.take(3).joinToString(", ") +
                            if (vendorNames.size > 3) " +${vendorNames.size - 3} more" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SubCategoryTransactionRow(
    transaction: Transaction,
    vendorName: String?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, HH:mm") }
    val (amountColor, prefix) = when (transaction.type) {
        TransactionType.DEBIT -> ExpenseRed to "-"
        TransactionType.CREDIT -> IncomeGreen to "+"
        TransactionType.SAVINGS -> Info to ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Merchant/Vendor name
                Text(
                    text = vendorName ?: transaction.merchantName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                // Date
                Text(
                    text = transaction.transactionDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Paid by and payment status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (transaction.paidBy != null) {
                        Text(
                            text = "by ${transaction.paidBy}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Payment status badge
                    val (statusColor, statusText) = when (transaction.paymentStatus) {
                        PaymentStatus.PAID -> IncomeGreen to "Paid"
                        PaymentStatus.PENDING -> Warning to "Pending"
                        PaymentStatus.PARTIAL -> Info to "Partial"
                        PaymentStatus.OVERDUE -> ExpenseRed to "Overdue"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }

                // Notes if any
                if (!transaction.expenseNotes.isNullOrBlank()) {
                    Text(
                        text = transaction.expenseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Amount
            Text(
                text = "$prefix${AmountFormatter.format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
    }
}

@Composable
private fun EmptyTransactionsState(
    subCategoryName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📝", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add expenses to $subCategoryName using the + button",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SetBudgetDialog(
    currentBudget: String,
    onBudgetChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean,
    error: String?
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Set Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter a budget amount for this sub-category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = currentBudget,
                    onValueChange = onBudgetChange,
                    label = { Text("Budget Amount", color = TextSecondary) },
                    prefix = { Text("₹", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary
                    )
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
