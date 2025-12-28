package com.fino.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.EMI
import com.fino.app.domain.model.EMIStatus
import com.fino.app.domain.model.Loan
import com.fino.app.domain.model.LoanStatus
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.CardWithEMIs
import com.fino.app.presentation.viewmodel.EMITrackerViewModel
import com.fino.app.presentation.viewmodel.LoanGroup
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EMITrackerScreen(
    onNavigateBack: () -> Unit,
    onAddEMI: () -> Unit,
    onEditEMI: (Long) -> Unit = {},
    onAddLoan: () -> Unit,
    onEditLoan: (Long) -> Unit = {},
    viewModel: EMITrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EMI & Loan Tracker",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FinoGradients.Primary)
                    .clickable {
                        if (uiState.selectedTab == 0) onAddEMI() else onAddLoan()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Summary Card
            item {
                SummarySection(
                    totalMonthlyEMI = uiState.totalMonthlyEMI,
                    totalMonthlyLoanEMI = uiState.totalMonthlyLoanEMI,
                    totalMonthlyObligations = uiState.totalMonthlyObligations
                )
            }

            // Tab Selector
            item {
                TabSelector(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    emiCount = uiState.activeEMIs.size,
                    loanCount = uiState.activeLoans.size
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> {
                        // EMIs Tab
                        if (uiState.activeEMIs.isEmpty() && uiState.completedEMIs.isEmpty()) {
                            item {
                                EmptyState(
                                    emoji = "📊",
                                    title = "No EMIs Yet",
                                    subtitle = "Add your credit card EMIs to track monthly payments"
                                )
                            }
                        } else {
                            // Active EMIs grouped by card
                            if (uiState.cardsWithEMIs.isNotEmpty()) {
                                item {
                                    SectionHeader("Credit Card EMIs")
                                }
                                items(uiState.cardsWithEMIs) { cardWithEMIs ->
                                    CardEMIsSection(
                                        cardWithEMIs = cardWithEMIs,
                                        onEditEMI = onEditEMI,
                                        onDeleteEMI = { showDeleteDialog = it }
                                    )
                                }
                            }

                            // Standalone EMIs
                            if (uiState.standaloneEMIs.isNotEmpty()) {
                                item {
                                    SectionHeader("Other EMIs")
                                }
                                items(uiState.standaloneEMIs) { emi ->
                                    EMIItemCard(
                                        emi = emi,
                                        onEdit = { onEditEMI(emi.id) },
                                        onDelete = { showDeleteDialog = emi }
                                    )
                                }
                            }

                            // Completed EMIs
                            if (uiState.completedEMIs.isNotEmpty()) {
                                item {
                                    SectionHeader("Completed")
                                }
                                items(uiState.completedEMIs) { emi ->
                                    EMIItemCard(
                                        emi = emi,
                                        onEdit = { onEditEMI(emi.id) },
                                        onDelete = { showDeleteDialog = emi },
                                        isCompleted = true
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Loans Tab
                        if (uiState.activeLoans.isEmpty() && uiState.completedLoans.isEmpty()) {
                            item {
                                EmptyState(
                                    emoji = "🏦",
                                    title = "No Loans Yet",
                                    subtitle = "Add your bank loans to track payments and progress"
                                )
                            }
                        } else {
                            // Active Loans grouped by type
                            if (uiState.loanGroups.isNotEmpty()) {
                                uiState.loanGroups.forEach { loanGroup ->
                                    item {
                                        SectionHeader("${loanGroup.type.emoji} ${loanGroup.type.displayName}s")
                                    }
                                    items(loanGroup.loans) { loan ->
                                        LoanItemCard(
                                            loan = loan,
                                            onEdit = { onEditLoan(loan.id) },
                                            onDelete = { showDeleteDialog = loan }
                                        )
                                    }
                                }
                            }

                            // Completed Loans
                            if (uiState.completedLoans.isNotEmpty()) {
                                item {
                                    SectionHeader("Completed / Closed")
                                }
                                items(uiState.completedLoans) { loan ->
                                    LoanItemCard(
                                        loan = loan,
                                        onEdit = { onEditLoan(loan.id) },
                                        onDelete = { showDeleteDialog = loan },
                                        isCompleted = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (item) {
                            is EMI -> viewModel.deleteEMI(item)
                            is Loan -> viewModel.deleteLoan(item)
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text(
                    text = "Delete ${if (item is EMI) "EMI" else "Loan"}?",
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone.",
                    color = TextSecondary
                )
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun SummarySection(
    totalMonthlyEMI: Double,
    totalMonthlyLoanEMI: Double,
    totalMonthlyObligations: Double
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SlideInCard(delay = 100) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.2f),
                                Secondary.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Monthly Obligations",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AmountFormatter.formatWithRupeeSymbol(totalMonthlyObligations),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Card EMIs",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(
                                text = AmountFormatter.formatWithRupeeSymbol(totalMonthlyEMI),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Loan EMIs",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Text(
                                text = AmountFormatter.formatWithRupeeSymbol(totalMonthlyLoanEMI),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
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
private fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    emiCount: Int,
    loanCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabButton(
            text = "EMIs",
            count = emiCount,
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Loans",
            count = loanCount,
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Primary else TextSecondary
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Primary else TextTertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) DarkBackground else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String
) {
    SlideInCard(delay = 150) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = emoji, fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CardEMIsSection(
    cardWithEMIs: CardWithEMIs,
    onEditEMI: (Long) -> Unit,
    onDeleteEMI: (EMI) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        SlideInCard(delay = 150) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cardWithEMIs.card.bankName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${cardWithEMIs.emis.size} EMIs • ${AmountFormatter.formatWithRupeeSymbol(cardWithEMIs.totalMonthlyAmount)}/mo",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                cardWithEMIs.emis.forEach { emi ->
                    EMIItemCard(
                        emi = emi,
                        onEdit = { onEditEMI(emi.id) },
                        onDelete = { onDeleteEMI(emi) },
                        compact = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EMIItemCard(
    emi: EMI,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isCompleted: Boolean = false,
    compact: Boolean = false
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") }
    val progress = emi.paidCount.toFloat() / emi.tenure.toFloat()
    val remainingMonths = emi.tenure - emi.paidCount

    SlideInCard(delay = 200) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 0.dp else 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isCompleted) DarkSurface else DarkSurfaceVariant)
                .clickable(onClick = onEdit)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = emi.description,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) TextSecondary else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IncomeGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Completed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                        emi.merchantName?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${emi.paidCount}/${emi.tenure} EMIs paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (!isCompleted) "$remainingMonths left" else "Done",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) IncomeGreen else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompleted) IncomeGreen else Primary,
                        trackColor = DarkSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Monthly EMI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = AmountFormatter.formatWithRupeeSymbol(emi.monthlyAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) TextSecondary else TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Original Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = AmountFormatter.formatWithRupeeSymbol(emi.originalAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ends ${emi.endDate.format(dateFormatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanItemCard(
    loan: Loan,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isCompleted: Boolean = false
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM yyyy") }
    val progress = loan.paidCount.toFloat() / loan.tenure.toFloat()
    val remainingMonths = loan.tenure - loan.paidCount

    SlideInCard(delay = 200) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isCompleted) DarkSurface else DarkSurfaceVariant)
                .clickable(onClick = onEdit)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = loan.description,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) TextSecondary else TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isCompleted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IncomeGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (loan.status) {
                                            LoanStatus.COMPLETED -> "Completed"
                                            LoanStatus.CLOSED -> "Closed"
                                            else -> "Done"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${loan.bankName} • ${loan.type.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${loan.paidCount}/${loan.tenure} EMIs paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (!isCompleted) "$remainingMonths left" else "Done",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) IncomeGreen else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isCompleted) IncomeGreen else Primary,
                        trackColor = DarkSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Monthly EMI",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = AmountFormatter.formatWithRupeeSymbol(loan.monthlyEMI),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCompleted) TextSecondary else TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Interest Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = "${loan.interestRate}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Principal",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = AmountFormatter.formatCompact(loan.principalAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                // Outstanding principal
                loan.outstandingPrincipal?.let { outstanding ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Outstanding Principal",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            text = AmountFormatter.formatWithRupeeSymbol(outstanding),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Warning
                        )
                    }
                }

                if (!isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextTertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ends ${loan.endDate.format(dateFormatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
