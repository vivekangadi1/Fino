package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@Composable
fun MerchantDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: MerchantDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    DetailScaffold(
        title = state.displayName.ifBlank { "Merchant" },
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MerchantHero(state.displayName, state.periodTotal, state.transactionCount) }
            item { MonthlyBarsSection(state.monthlyBars) }
            if (state.variants.size > 1) {
                item { Eyebrow(text = "Variants") }
                items(state.variants) { variant ->
                    VariantRow(variant)
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Eyebrow(text = "Recent transactions")
            }
            if (state.recentTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No transactions for this merchant yet.",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            } else {
                items(state.recentTransactions) { txn ->
                    TransactionRow(txn)
                }
            }
        }
    }
}

@Composable
private fun MerchantHero(name: String, total: Double, count: Int) {
    Column {
        Eyebrow(text = "Total spent")
        Spacer(Modifier.height(8.dp))
        Text(
            text = AmountFormatter.formatCompact(total),
            style = SerifHero.copy(color = FinoColors.ink(), fontSize = 40.sp, lineHeight = 46.sp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$count transaction${if (count == 1) "" else "s"} at $name",
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun MonthlyBarsSection(bars: List<com.fino.app.presentation.screens.insights.MerchantMonthlyBar>) {
    Column {
        Eyebrow(text = "6 month trend")
        Spacer(Modifier.height(10.dp))
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
                            .background(FinoColors.accentColor())
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(bar.label, fontSize = 10.sp, color = FinoColors.ink3())
                }
            }
        }
    }
}

@Composable
private fun VariantRow(row: com.fino.app.presentation.screens.insights.MerchantVariantRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.rawName,
            fontSize = 13.sp,
            color = FinoColors.ink(),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${row.count}×  ${AmountFormatter.format(row.amount)}",
            fontSize = 12.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun TransactionRow(txn: Transaction) {
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
            Spacer(Modifier.size(2.dp))
            Text(
                text = txn.transactionDate.toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                fontSize = 11.sp,
                color = FinoColors.ink3()
            )
        }
        Text(
            text = AmountFormatter.format(txn.amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.ink()
        )
    }
}
