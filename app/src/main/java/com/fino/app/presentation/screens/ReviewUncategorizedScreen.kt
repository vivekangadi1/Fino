package com.fino.app.presentation.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.presentation.components.CategorizationBottomSheet
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.ReviewUncategorizedViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewUncategorizedScreen(
    onNavigateBack: () -> Unit,
    onManageMappings: () -> Unit = {},
    viewModel: ReviewUncategorizedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.categorizeSuccess) {
        if (uiState.categorizeSuccess) {
            selectedTransaction = null
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Review Transactions")
                        Text(
                            text = "${uiState.uncategorizedTransactions.size} need attention",
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
                actions = {
                    TextButton(onClick = onManageMappings) {
                        Text("Mappings", color = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
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
        } else if (uiState.uncategorizedTransactions.isEmpty()) {
            // All done state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✅", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Caught Up!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No uncategorized transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Info.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Info,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Tap a transaction to assign a category. We'll remember your choices for future transactions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Transactions list
                items(
                    items = uiState.uncategorizedTransactions,
                    key = { it.id }
                ) { transaction ->
                    UncategorizedTransactionCard(
                        transaction = transaction,
                        otherCount = viewModel.getOtherTransactionsCount(transaction.merchantName),
                        onClick = { selectedTransaction = transaction }
                    )
                }
            }
        }
    }

    // Bottom sheet for categorization
    if (selectedTransaction != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedTransaction = null },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            CategorizationBottomSheet(
                transaction = selectedTransaction!!,
                categories = uiState.categories,
                otherTransactionsCount = viewModel.getOtherTransactionsCount(selectedTransaction!!.merchantName),
                onCategorySelected = { categoryId, applyToAll ->
                    viewModel.categorizeTransaction(
                        transaction = selectedTransaction!!,
                        categoryId = categoryId,
                        applyToAllFromMerchant = applyToAll
                    )
                },
                onSkip = {
                    val current = selectedTransaction
                    val transactions = uiState.uncategorizedTransactions
                    val currentIndex = transactions.indexOfFirst { it.id == current?.id }
                    selectedTransaction = if (currentIndex < transactions.size - 1) {
                        transactions[currentIndex + 1]
                    } else {
                        null
                    }
                },
                onDismiss = { selectedTransaction = null }
            )
        }
    }
}

@Composable
private fun UncategorizedTransactionCard(
    transaction: Transaction,
    otherCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, HH:mm") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Warning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.merchantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.transactionDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        transaction.paymentMethod?.let { method ->
                            Text(
                                text = " • $method",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(transaction.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                    if (otherCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+$otherCount more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Categorize button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Primary.copy(alpha = 0.1f))
                    .clickable(onClick = onClick)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tap to categorize",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }
    }
}
