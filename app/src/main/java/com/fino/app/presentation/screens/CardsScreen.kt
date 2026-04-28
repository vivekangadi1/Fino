package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.CreditCard
import com.fino.app.presentation.components.FinoBottomNavBar
import com.fino.app.presentation.components.primitives.CreditCardItem
import com.fino.app.presentation.components.primitives.EMIRow
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.HairlineDivider
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle
import com.fino.app.presentation.viewmodel.CardsViewModel
import com.fino.app.presentation.viewmodel.EMIRowData
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToActivity: () -> Unit = {},
    onNavigateToRewards: () -> Unit,
    onAddCard: () -> Unit = {},
    onEditCard: (Long) -> Unit = {},
    onNavigateToEMITracker: () -> Unit = {},
    onAddTransaction: () -> Unit = {},
    viewModel: CardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("cards") }

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
                        "insights" -> onNavigateToAnalytics()
                    }
                },
                onAddClick = onAddTransaction
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { CardsTopBar(onAddCard = onAddCard) }
            item { TotalDueBlock(state = uiState) }
            item {
                CardStack(
                    cards = uiState.cards,
                    isLoading = uiState.isLoading,
                    onEditCard = onEditCard
                )
            }
            item {
                ActiveEMISection(
                    emis = uiState.activeEMIs,
                    total = uiState.totalEMICount,
                    onSeeAll = onNavigateToEMITracker
                )
            }
        }
    }
}

@Composable
private fun CardsTopBar(onAddCard: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 18.dp, top = 8.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Cards & loans",
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onAddCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add card",
                tint = FinoColors.ink2(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TotalDueBlock(state: com.fino.app.presentation.viewmodel.CardsUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
    ) {
        val nextDue = state.cards
            .filter { it.previousDueDate != null || it.dueDateDay != null }
            .minByOrNull { nextDueDaysLeft(it) }
        val cardCount = state.cards.size
        val outstandingLabel = "Outstanding · $cardCount card${if (cardCount == 1) "" else "s"}"
        Eyebrow(text = outstandingLabel)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "₹${formatIndianLoose(state.totalOutstanding)}",
            fontFamily = Newsreader,
            fontSize = 44.sp,
            lineHeight = 46.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-1.32).sp,
            color = FinoColors.ink(),
            style = NumericStyle
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = nextDueSubtitle(nextDue),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = FinoColors.ink3()
        )
    }
}

private fun nextDueDaysLeft(card: CreditCard): Long {
    val today = LocalDate.now()
    val due = card.effectiveDueDate
        ?: card.dueDateDay?.let { day ->
            val month = if (today.dayOfMonth > day) today.plusMonths(1) else today
            val clamped = day.coerceAtMost(month.lengthOfMonth())
            month.withDayOfMonth(clamped)
        }
    return if (due != null) ChronoUnit.DAYS.between(today, due).coerceAtLeast(0L) else Long.MAX_VALUE
}

private fun nextDueSubtitle(card: CreditCard?): String {
    if (card == null) return "No upcoming bill"
    val days = nextDueDaysLeft(card)
    val name = listOfNotNull(card.bankName, card.cardName).joinToString(" ")
    return when {
        days == Long.MAX_VALUE -> "No upcoming bill"
        days == 0L -> "Due today · $name"
        days == 1L -> "Due tomorrow · $name"
        else -> "Next due in $days days · $name"
    }
}

@Composable
private fun CardStack(
    cards: List<CreditCard>,
    isLoading: Boolean,
    onEditCard: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FinoColors.accentColor())
                }
            }
            cards.isEmpty() -> EmptyCardsState()
            else -> {
                cards.forEach { card ->
                    CardRow(card = card, onClick = { onEditCard(card.id) })
                }
            }
        }
    }
}

@Composable
private fun CardRow(card: CreditCard, onClick: () -> Unit) {
    val outstanding = card.currentUnbilled + card.previousDue
    val limit = card.creditLimit ?: 0.0
    val utilization = if (limit > 0) (outstanding / limit).toFloat() else 0f
    val daysLeft = nextDueDaysLeft(card)
    val warn = utilization > 0.7f || (daysLeft in 0..5)
    val bankAndName = listOfNotNull(card.bankName, card.cardName).joinToString(" · ")
    val dueAmount = "₹${formatIndianLoose(outstanding)}"
    val dueMeta = buildString {
        append("Due ")
        append(
            card.effectiveDueDate?.let { formatShortDate(it) }
                ?: card.dueDateDay?.let { "${it}th" }
                ?: "—"
        )
        card.minimumDue?.takeIf { it > 0 }?.let {
            append(" · min ₹${formatIndianLoose(it)}")
        }
    }
    val limitLabel = if (limit > 0) "of ₹${formatIndianLoose(limit)}" else "no limit set"
    CreditCardItem(
        bankAndName = bankAndName,
        last4 = "•••• ${card.lastFourDigits}",
        due = dueAmount,
        dueMeta = dueMeta,
        utilizationPercent = utilization.coerceIn(0f, 1f),
        totalLimitLabel = limitLabel,
        isWarn = warn,
        onClick = onClick
    )
}

@Composable
private fun EmptyCardsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(FinoColors.cardTint())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No cards yet",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap + to add your first credit card",
            fontSize = 12.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun ActiveEMISection(
    emis: List<EMIRowData>,
    total: Int,
    onSeeAll: () -> Unit
) {
    if (total == 0) return
    val activeCount = emis.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Eyebrow(text = "Active EMIs")
            Row(
                modifier = Modifier.clickable(onClick = onSeeAll),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$activeCount of $total",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = FinoColors.ink3()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (emis.isEmpty()) {
            Text(
                text = "No active EMIs",
                fontSize = 12.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else {
            emis.forEachIndexed { index, row ->
                if (index == 0) HairlineDivider()
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    EMIRow(
                        name = row.name,
                        sub = row.sub,
                        monthlyAmount = "₹${formatIndianLoose(row.monthlyAmount)}",
                        progress = row.progress,
                        remainingLabel = "₹${formatShortAmount(row.remainingAmount)} left"
                    )
                }
                if (index < emis.lastIndex) HairlineDivider()
            }
        }
    }
}

private fun formatIndianLoose(amount: Double): String {
    if (amount <= 0) return "0"
    return String.format("%,.0f", amount)
}

private fun formatShortAmount(amount: Double): String {
    if (amount <= 0) return "0"
    return when {
        amount >= 100_000 -> String.format("%.1fL", amount / 100_000)
        amount >= 1_000 -> String.format("%,.0f", amount)
        else -> String.format("%.0f", amount)
    }
}

private fun formatShortDate(date: LocalDate): String {
    val month = date.month.getDisplayName(
        java.time.format.TextStyle.SHORT,
        java.util.Locale.ENGLISH
    )
    return "$month ${date.dayOfMonth}"
}
