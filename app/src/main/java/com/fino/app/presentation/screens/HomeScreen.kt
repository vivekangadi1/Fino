package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.HomeViewModel
import com.fino.app.presentation.viewmodel.SmsScanViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onAddTransaction: () -> Unit,
    onNavigateToUpcomingBills: () -> Unit,
    onAddRecurringBill: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    smsScanViewModel: SmsScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val smsScanState by smsScanViewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("home") }
    var showScanResultDialog by remember { mutableStateOf(false) }

    // Show dialog when scan completes
    LaunchedEffect(smsScanState.lastResult) {
        if (smsScanState.lastResult != null && !smsScanState.isScanning) {
            showScanResultDialog = true
        }
    }

    // Scan result dialog
    if (showScanResultDialog && smsScanState.lastResult != null) {
        AlertDialog(
            onDismissRequest = { showScanResultDialog = false },
            title = { Text("SMS Scan Complete") },
            text = {
                val result = smsScanState.lastResult!!
                Column {
                    Text("Scanned ${result.totalSmsScanned} SMS messages")
                    Text("Found ${result.transactionsFound} transactions")
                    Text("Saved ${result.transactionsSaved} new transactions")
                    if (result.duplicatesSkipped > 0) {
                        Text("Skipped ${result.duplicatesSkipped} duplicates")
                    }
                    if (result.errors > 0) {
                        Text("${result.errors} errors occurred", color = ExpenseRed)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showScanResultDialog = false
                    viewModel.refresh() // Refresh home data
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Error dialog
    if (smsScanState.error != null) {
        AlertDialog(
            onDismissRequest = { smsScanViewModel.clearError() },
            title = { Text("Scan Error") },
            text = { Text(smsScanState.error!!) },
            confirmButton = {
                TextButton(onClick = { smsScanViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            // Gradient FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FinoGradients.Primary)
                    .clickable { onAddTransaction() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            FinoBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                    when (route) {
                        "cards" -> onNavigateToCards()
                        "analytics" -> onNavigateToAnalytics()
                        "rewards" -> onNavigateToRewards()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Gradient Header with Balance
            item {
                ModernHeader(totalBalance = uiState.totalBalance)
            }

            // Quick Actions
            item {
                QuickActionsRow(
                    onAddTransaction = onAddTransaction,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onScanSms = { smsScanViewModel.scanCurrentMonth() },
                    isScanning = smsScanState.isScanning
                )
            }

            // Stats Cards
            item {
                ModernStatsSection(
                    monthlySpent = uiState.monthlySpent,
                    monthlyIncome = uiState.monthlyIncome,
                    monthlySaved = uiState.monthlySaved
                )
            }

            // Upcoming Bills Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                UpcomingBillsSection(
                    summary = uiState.upcomingBillsSummary,
                    nextBills = uiState.nextBills,
                    hasUrgentBills = uiState.hasUrgentBills,
                    onViewAll = onNavigateToUpcomingBills,
                    onAddBill = onAddRecurringBill
                )
            }

            // Recent Transactions
            item {
                ModernTransactionsSection(
                    transactions = uiState.recentTransactions,
                    onSeeAll = onNavigateToAnalytics
                )
            }

            // Budget Health
            item {
                ModernBudgetCard()
            }

            // XP Progress Teaser
            item {
                XpTeaserCard(
                    currentLevel = uiState.currentLevel,
                    levelName = uiState.levelName,
                    xpProgress = uiState.xpProgress,
                    totalXp = uiState.totalXp,
                    onClick = onNavigateToRewards
                )
            }
        }
    }
}

@Composable
private fun ModernHeader(totalBalance: Double) {
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
        Column {
            // Greeting Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getGreeting(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("👋", fontSize = 24.sp)
                    }
                }

                // Profile / Settings
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = "Profile",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Total Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(FinoGradients.PrimaryDiagonal)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedCounter(
                        targetValue = totalBalance.toInt(),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                        prefix = "₹",
                        formatAsRupees = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = Success, size = 6.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Synced just now",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

@Composable
private fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onScanSms: () -> Unit,
    isScanning: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionPill(
            icon = Icons.Outlined.Add,
            label = "Add",
            onClick = onAddTransaction,
            modifier = Modifier.weight(1f)
        )
        QuickActionPill(
            icon = if (isScanning) Icons.Outlined.Sync else Icons.Outlined.Sms,
            label = if (isScanning) "Scanning..." else "Scan SMS",
            onClick = { if (!isScanning) onScanSms() },
            modifier = Modifier.weight(1f),
            isLoading = isScanning
        )
        QuickActionPill(
            icon = Icons.Outlined.Analytics,
            label = "Stats",
            onClick = onNavigateToAnalytics,
            modifier = Modifier.weight(1f)
        )
        QuickActionPill(
            icon = Icons.Outlined.MoreHoriz,
            label = "More",
            onClick = { },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Primary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                icon,
                contentDescription = label,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

// Savings gradient
private val SavingsGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4A90D9), Color(0xFF357ABD))
)

@Composable
private fun ModernStatsSection(
    monthlySpent: Double,
    monthlyIncome: Double,
    monthlySaved: Double
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "This Month",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Expense Card
            StatCard(
                icon = Icons.Default.TrendingDown,
                label = "Spent",
                amount = monthlySpent,
                gradient = FinoGradients.Expense,
                modifier = Modifier.weight(1f)
            )

            // Income Card
            StatCard(
                icon = Icons.Default.TrendingUp,
                label = "Income",
                amount = monthlyIncome,
                gradient = FinoGradients.Income,
                modifier = Modifier.weight(1f)
            )

            // Saved Card
            StatCard(
                icon = Icons.Outlined.Savings,
                label = "Saved",
                amount = monthlySaved,
                gradient = SavingsGradient,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    amount: Double,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            AnimatedCounter(
                targetValue = amount.toInt(),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                prefix = "₹",
                formatAsRupees = true
            )
        }
    }
}

@Composable
private fun ModernTransactionsSection(
    transactions: List<Transaction>,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSeeAll() }
            ) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary
                )
                BouncingArrow(color = Primary)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            // Empty State
            SlideInCard(delay = 100) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedEmptyState(
                        emoji = "📭",
                        title = "No transactions yet",
                        subtitle = "Add your first transaction to get started"
                    )
                }
            }
        } else {
            // Transaction List
            transactions.forEachIndexed { index, transaction ->
                SlideInCard(delay = 100 + (index * 50)) {
                    TransactionRow(transaction = transaction)
                }
                if (index < transactions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// Blue color for Savings
private val SavingsBlue = Color(0xFF4A90D9)

@Composable
private fun TransactionRow(transaction: Transaction) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, HH:mm") }

    val (bgColor, iconColor, icon, prefix) = when (transaction.type) {
        TransactionType.DEBIT -> Quad(
            ExpenseRed.copy(alpha = 0.2f),
            ExpenseRed,
            Icons.Default.TrendingDown,
            "-"
        )
        TransactionType.CREDIT -> Quad(
            IncomeGreen.copy(alpha = 0.2f),
            IncomeGreen,
            Icons.Default.TrendingUp,
            "+"
        )
        TransactionType.SAVINGS -> Quad(
            SavingsBlue.copy(alpha = 0.2f),
            SavingsBlue,
            Icons.Outlined.Savings,
            ""
        )
    }

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
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
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
                text = "$prefix₹${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = iconColor
            )
        }
    }
}

// Helper data class for destructuring
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun ModernBudgetCard() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Budget Health",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        SlideInCard(delay = 200) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DarkSurfaceVariant, DarkSurfaceHigh)
                        )
                    )
                    .clickable { }
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon with glow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set up your first budget",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Track spending by category",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun XpTeaserCard(
    currentLevel: Int,
    levelName: String,
    xpProgress: Float,
    totalXp: Int,
    onClick: () -> Unit
) {
    val nextLevelXp = currentLevel * 100

    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        SlideInCard(delay = 300) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(FinoGradients.Gold)
                    .clickable(onClick = onClick)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Level $currentLevel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SparkleEffect()
                        }
                        Text(
                            text = levelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedGradientProgress(
                            progress = xpProgress,
                            gradient = Brush.linearGradient(
                                colors = listOf(DarkBackground.copy(alpha = 0.3f), DarkBackground.copy(alpha = 0.5f))
                            ),
                            backgroundColor = DarkBackground.copy(alpha = 0.2f),
                            height = 6.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalXp / $nextLevelXp XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkBackground.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Trophy icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DarkBackground.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏆", fontSize = 28.sp)
                    }
                }
            }
        }
    }
}
