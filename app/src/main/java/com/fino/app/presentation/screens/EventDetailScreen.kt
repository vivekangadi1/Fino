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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.EventSubCategorySummary
import com.fino.app.domain.model.EventVendorSummary
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.components.EventBudgetOverview
import com.fino.app.presentation.components.EventBudgetProgress
import com.fino.app.presentation.components.PendingPaymentsSection
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.components.SubCategoryCard
import com.fino.app.presentation.components.VendorCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.EventDetailViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    @Suppress("UNUSED_PARAMETER") eventId: Long, // eventId is retrieved via SavedStateHandle in ViewModel
    onNavigateBack: () -> Unit,
    onEditEvent: () -> Unit,
    onAddExpense: () -> Unit,
    onAddSubCategory: () -> Unit = {},
    onEditSubCategory: (Long) -> Unit = {},
    onAddVendor: () -> Unit = {},
    onEditVendor: (Long) -> Unit = {},
    onPaymentClick: (Long) -> Unit = {},
    onManageFamilyMembers: () -> Unit = {},
    onTransactionClick: (Long) -> Unit = {},
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Navigate back after successful delete
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    // Complete event dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Event") },
            text = { Text("Mark this event as completed? You can still view its transactions.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.completeEvent()
                        showCompleteDialog = false
                    }
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete event dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Event", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete this event? This action cannot be undone. Transactions will be unlinked but not deleted.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Primary,
                contentColor = TextPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense"
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.event?.name ?: "Event Details",
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
                    IconButton(onClick = onEditEvent) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextPrimary
                        )
                    }
                    if (uiState.event?.isActive == true) {
                        IconButton(onClick = { showCompleteDialog = true }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Complete",
                                tint = IncomeGreen
                            )
                        }
                    }
                    // More options menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = TextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Sub-Category", color = TextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    onAddSubCategory()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Vendor", color = TextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    onAddVendor()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Family Members", color = TextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    onManageFamilyMembers()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            )
                            HorizontalDivider(color = DarkSurfaceHigh)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Event",
                                        color = ExpenseRed
                                    )
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = ExpenseRed
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else {
                uiState.event?.let { event ->
                    // Event header
                    item {
                        EventHeader(
                            emoji = event.emoji,
                            name = event.name,
                            typeName = uiState.eventTypeName,
                            startDate = event.startDate.toString(),
                            endDate = event.endDate?.toString() ?: "Ongoing"
                        )
                    }

                    // Enhanced Budget Overview (with quoted, paid, pending)
                    if (uiState.totalBudget > 0 || uiState.totalQuoted > 0 || uiState.totalPaid > 0) {
                        item {
                            SlideInCard(delay = 100) {
                                EventBudgetOverview(
                                    totalBudget = uiState.totalBudget,
                                    totalQuoted = uiState.totalQuoted,
                                    totalPaid = uiState.totalPaid,
                                    totalPending = uiState.totalPending,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Fallback to simple budget progress
                        uiState.budgetStatus?.let { budgetStatus ->
                            item {
                                SlideInCard(delay = 100) {
                                    EventBudgetProgress(
                                        budgetStatus = budgetStatus,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Sub-Categories Section
                    if (uiState.subCategorySummaries.isNotEmpty()) {
                        item {
                            SubCategoriesSection(
                                summaries = uiState.subCategorySummaries,
                                onSubCategoryClick = { onEditSubCategory(it.subCategory.id) },
                                onAddClick = onAddSubCategory
                            )
                        }
                    }

                    // Vendors Section
                    if (uiState.vendorSummaries.isNotEmpty()) {
                        item {
                            VendorsSection(
                                summaries = uiState.vendorSummaries,
                                onVendorClick = { onEditVendor(it.vendor.id) },
                                onAddClick = onAddVendor
                            )
                        }
                    }

                    // Pending Payments Section
                    if (uiState.pendingPayments.isNotEmpty()) {
                        item {
                            PendingPaymentsSection(
                                payments = uiState.pendingPayments,
                                vendorNames = uiState.vendorNames,
                                subCategoryNames = uiState.subCategoryNames,
                                onPaymentClick = { onPaymentClick(it.id) },
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Category breakdown
                    if (uiState.categoryBreakdown.isNotEmpty()) {
                        item {
                            CategoryBreakdownSection(
                                categories = uiState.categoryBreakdown
                            )
                        }
                    }

                    // Daily spending
                    if (uiState.dailySpending.isNotEmpty()) {
                        item {
                            DailySpendingSection(
                                dailySpending = uiState.dailySpending
                            )
                        }
                    }

                    // Recent transactions
                    item {
                        TransactionsHeader(
                            count = uiState.transactions.size
                        )
                    }

                    if (uiState.transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
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
                                    Text("📊", fontSize = 48.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No transactions yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.transactions, key = { it.id }) { transaction ->
                            SlideInCard(delay = 200) {
                                TransactionRow(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction.id) },
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
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
private fun EventHeader(
    emoji: String,
    name: String,
    typeName: String,
    startDate: String,
    endDate: String,
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event emoji
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

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$startDate - $endDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(
    categories: List<com.fino.app.domain.model.CategorySpending>,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val categoryColors = listOf(Primary, Secondary, Info, Warning, IncomeGreen, ExpenseRed)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        categories.forEachIndexed { index, category ->
            SlideInCard(delay = 150) {
                CategoryRow(
                    emoji = "\uD83D\uDCB0",
                    name = category.categoryName,
                    amount = currencyFormatter.format(category.amount),
                    percentage = category.percentage,
                    color = categoryColors[index % categoryColors.size]
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryRow(
    emoji: String,
    name: String,
    amount: String,
    percentage: Float,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkSurfaceHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DailySpendingSection(
    dailySpending: List<com.fino.app.domain.model.DailySpending>,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Daily Spending",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        val maxAmount = dailySpending.maxOfOrNull { it.amount } ?: 1.0

        dailySpending.forEach { day ->
            SlideInCard(delay = 150) {
                DailySpendingBar(
                    date = day.date.format(dateFormatter),
                    amount = currencyFormatter.format(day.amount),
                    percentage = (day.amount / maxAmount).toFloat(),
                    transactionCount = day.transactionCount
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DailySpendingBar(
    date: String,
    amount: String,
    percentage: Float,
    transactionCount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "$transactionCount txn${if (transactionCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkSurfaceHigh)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(FinoGradients.Expense)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TransactionsHeader(
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
            text = "$count total",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, HH:mm") }
    val (iconColor, prefix) = when (transaction.type) {
        TransactionType.DEBIT -> ExpenseRed to "-"
        TransactionType.CREDIT -> IncomeGreen to "+"
        TransactionType.SAVINGS -> androidx.compose.ui.graphics.Color(0xFF4A90D9) to ""
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
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                text = "$prefix₹${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
        }
    }
}

@Composable
private fun SubCategoriesSection(
    summaries: List<EventSubCategorySummary>,
    onSubCategoryClick: (EventSubCategorySummary) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sub-Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Sub-Category",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        summaries.forEach { summary ->
            SlideInCard(delay = 150) {
                SubCategoryCard(
                    summary = summary,
                    onClick = { onSubCategoryClick(summary) },
                    onEditClick = { onSubCategoryClick(summary) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun VendorsSection(
    summaries: List<EventVendorSummary>,
    onVendorClick: (EventVendorSummary) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vendors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            IconButton(
                onClick = onAddClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Vendor",
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        summaries.forEach { summary ->
            SlideInCard(delay = 150) {
                VendorCard(
                    summary = summary,
                    onClick = { onVendorClick(summary) },
                    onEditClick = { onVendorClick(summary) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
