package com.fino.app.presentation.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle
import com.fino.app.presentation.viewmodel.ActivityFilter
import com.fino.app.presentation.viewmodel.ActivityUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthDay = DateTimeFormatter.ofPattern("MMM d")
private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

@Composable
fun TransactionsTabBody(
    state: ActivityUiState,
    onFilterSelect: (ActivityFilter) -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { MonthHero(state = state) }
        item {
            TxFilterChips(
                current = state.filter,
                onSelect = onFilterSelect
            )
        }

        if (state.grouped.isEmpty() && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No activity yet",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            }
        }

        state.grouped.forEach { (date, txns) ->
            val dayDebits = txns.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)) {
                    TxDayHeader(date = date, dayTotal = dayDebits)
                }
            }
            items(txns, key = { it.id }) { txn ->
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    DesignTxRow(
                        txn = txn,
                        categoryName = state.categoryNames[txn.categoryId]?.first,
                        eventName = txn.eventId?.let { state.eventNames[it] },
                        onClick = { onTransactionClick(txn.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHero(state: ActivityUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp)
    ) {
        Text(
            text = "THIS MONTH",
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.ink3()
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "₹${formatIndian(state.monthlyOutgoing)}",
                fontFamily = Newsreader,
                fontSize = 44.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-1.32).sp,
                color = FinoColors.ink(),
                style = NumericStyle
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "${state.monthlyTransactionCount} ${if (state.monthlyTransactionCount == 1) "transaction" else "transactions"}",
                fontSize = 12.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ ₹${formatIndian(state.monthlyIncome)}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.positive(),
                    style = NumericStyle
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "in",
                    fontSize = 11.5.sp,
                    color = FinoColors.ink3()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "− ₹${formatIndian(state.monthlyOutgoing)}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.negative(),
                    style = NumericStyle
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "out",
                    fontSize = 11.5.sp,
                    color = FinoColors.ink3()
                )
            }
        }
    }
}

@Composable
private fun TxFilterChips(
    current: ActivityFilter,
    onSelect: (ActivityFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ChipItem("All", current == ActivityFilter.ALL) { onSelect(ActivityFilter.ALL) }
        ChipItem("Outgoing", current == ActivityFilter.OUTGOING) { onSelect(ActivityFilter.OUTGOING) }
        ChipItem("Incoming", current == ActivityFilter.INCOMING) { onSelect(ActivityFilter.INCOMING) }
        ChipItem(
            label = "Needs review",
            selected = current == ActivityFilter.NEEDS_REVIEW,
            dotColor = FinoColors.warn(),
            onClick = { onSelect(ActivityFilter.NEEDS_REVIEW) }
        )
        ChipItem("Cards", current == ActivityFilter.CARDS) { onSelect(ActivityFilter.CARDS) }
    }
}

@Composable
private fun ChipItem(
    label: String,
    selected: Boolean,
    dotColor: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    val bg = if (selected) FinoColors.ink() else FinoColors.card()
    val fg = if (selected) FinoColors.paper() else FinoColors.ink2()
    val borderColor = if (selected) FinoColors.ink() else FinoColors.line()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (dotColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
private fun TxDayHeader(date: LocalDate, dayTotal: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .border(
                width = 0.dp,
                color = androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(0.dp)
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = labelFor(date),
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.ink3()
        )
        Text(
            text = "₹${formatIndian(dayTotal)}",
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = FinoColors.ink3(),
            style = NumericStyle
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FinoColors.line2())
    )
}

@Composable
private fun DesignTxRow(
    txn: Transaction,
    categoryName: String?,
    eventName: String?,
    onClick: () -> Unit
) {
    val isIncome = txn.type == TransactionType.CREDIT
    val sign = if (isIncome) "+" else "−"
    val amountText = "$sign ₹${formatIndian(txn.amount)}"
    val amountColor = if (isIncome) FinoColors.positive() else FinoColors.ink()
    val displayName = (txn.merchantNormalized ?: txn.merchantName).ifBlank { "Unknown" }
    val catText = categoryName ?: "Uncategorized"
    val timeText = txn.transactionDate.toLocalTime().format(timeFmt)
    val sourceLabel = sourceTagFor(txn)
    val showReview = txn.needsReview

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = catText,
                    fontSize = 12.sp,
                    color = FinoColors.ink3()
                )
                Text(
                    text = "·",
                    fontSize = 12.sp,
                    color = FinoColors.ink3()
                )
                Text(
                    text = timeText,
                    fontSize = 12.sp,
                    color = FinoColors.ink3()
                )
                if (!eventName.isNullOrBlank()) {
                    Text(
                        text = "·",
                        fontSize = 12.sp,
                        color = FinoColors.ink3()
                    )
                    Text(
                        text = "▪ $eventName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = FinoColors.accentInk()
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amountText,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = amountColor,
                style = NumericStyle
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (showReview) "NEEDS REVIEW" else sourceLabel,
                fontFamily = JetBrainsMono,
                fontSize = 10.5.sp,
                letterSpacing = 0.4.sp,
                color = if (showReview) FinoColors.warn() else FinoColors.ink3()
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FinoColors.line2())
    )
}

private fun sourceTagFor(txn: Transaction): String {
    val cardLabel = txn.cardLastFour?.let { last4 ->
        val bank = txn.bankName ?: "CARD"
        "${bank.uppercase()} $last4"
    }
    return (cardLabel ?: txn.paymentMethod?.uppercase() ?: txn.bankName?.uppercase() ?: "").ifBlank { "—" }
}

private fun labelFor(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "TODAY · ${date.format(monthDay).uppercase()}"
        today.minusDays(1) -> "YESTERDAY · ${date.format(monthDay).uppercase()}"
        else -> date.format(monthDay).uppercase()
    }
}

private fun formatIndian(amount: Double): String {
    if (amount <= 0) return "0"
    return java.text.DecimalFormat("#,##,##0").format(amount)
}
