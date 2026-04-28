package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@Composable
fun DayDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DayDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val title = state.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    DetailScaffold(
        title = title,
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DayHero(state) }
            if (state.stacks.isNotEmpty()) {
                item { StackedBar(state.stacks) }
                item { StackLegend(state.stacks) }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Eyebrow(text = "Transactions")
            }
            if (state.transactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions on this day.",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            } else {
                items(state.transactions) { txn -> DayTransactionRow(txn) }
            }
        }
    }
}

@Composable
private fun DayHero(state: DayDetailUiState) {
    Column {
        Eyebrow(text = "Spent")
        Spacer(Modifier.height(8.dp))
        Text(
            text = AmountFormatter.formatCompact(state.totalSpent),
            style = SerifHero.copy(color = FinoColors.ink(), fontSize = 40.sp, lineHeight = 46.sp)
        )
        if (state.totalIncome > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+ ${AmountFormatter.format(state.totalIncome)} received",
                fontSize = 13.sp,
                color = FinoColors.positive()
            )
        }
    }
}

@Composable
private fun StackedBar(stacks: List<DayCategoryStack>) {
    val palette = FinoColors.chart()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(100.dp))
    ) {
        stacks.forEachIndexed { i, s ->
            Box(
                modifier = Modifier
                    .weight(s.fraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(palette.getOrElse(i) { FinoColors.ink5() })
            )
        }
    }
}

@Composable
private fun StackLegend(stacks: List<DayCategoryStack>) {
    val palette = FinoColors.chart()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        stacks.take(6).forEachIndexed { i, s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.getOrElse(i) { FinoColors.ink5() })
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = s.categoryName,
                    fontSize = 12.sp,
                    color = FinoColors.ink(),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(s.fraction * 100).toInt()}% · ${AmountFormatter.format(s.amount)}",
                    fontSize = 11.sp,
                    color = FinoColors.ink3()
                )
            }
        }
    }
}

@Composable
private fun DayTransactionRow(txn: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = txn.merchantName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            val timeStr = txn.transactionDate.format(DateTimeFormatter.ofPattern("h:mm a"))
            Text(
                text = timeStr + (txn.paymentMethod?.let { " · $it" } ?: ""),
                fontSize = 11.sp,
                color = FinoColors.ink3()
            )
        }
        val amountStr = if (txn.type == TransactionType.CREDIT) "+ ${AmountFormatter.format(txn.amount)}"
        else AmountFormatter.format(txn.amount)
        Text(
            text = amountStr,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (txn.type == TransactionType.CREDIT) FinoColors.positive() else FinoColors.ink()
        )
    }
}
