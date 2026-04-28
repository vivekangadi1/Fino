package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@Composable
fun BillDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: BillDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val hasData = state.bill != null || state.card != null
    DetailScaffold(
        title = state.issuerLabel?.let { "$it bill" } ?: "Bill",
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (hasData) {
                item { StatementCard(state) }
                item { ActionButtons(onPay = { viewModel.markPaid() }) }
                if (state.categorySlices.isNotEmpty()) {
                    item { Eyebrow(text = "Category breakdown") }
                    items(state.categorySlices) { slice -> CategoryBar(slice) }
                }
                item { BudgetImpactNote() }
            } else {
                item {
                    Text(
                        text = "Bill not found.",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            }
        }
    }
}

@Composable
private fun StatementCard(state: BillDetailUiState) {
    val label = state.issuerLabel ?: state.card?.let { "${it.bankName} ****${it.lastFourDigits}" }
        ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF14130F))
            .padding(20.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFFCFCBC2),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = AmountFormatter.format(state.totalDue),
            style = SerifHero.copy(color = Color(0xFFF4F1EA), fontSize = 44.sp, lineHeight = 48.sp)
        )
        Spacer(Modifier.height(4.dp))
        state.minimumDue?.let {
            Text(
                text = "Min due ${AmountFormatter.format(it)}",
                fontSize = 11.sp,
                color = Color(0xFFCFCBC2)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CYCLE", fontSize = 9.sp, color = Color(0xFF9A9690), letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                val fmt = DateTimeFormatter.ofPattern("MMM d")
                Text(
                    text = "${state.cycleStart?.format(fmt) ?: "—"} – ${state.cycleEnd?.format(fmt) ?: "—"}",
                    fontSize = 11.sp,
                    color = Color(0xFFD4D1CA)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("DUE", fontSize = 9.sp, color = Color(0xFF9A9690), letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.dueDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "—",
                    fontSize = 11.sp,
                    color = Color(0xFFD4D1CA)
                )
            }
            Column(modifier = Modifier.weight(0.7f)) {
                Text("IN", fontSize = 9.sp, color = Color(0xFF9A9690), letterSpacing = 1.sp)
                Spacer(Modifier.height(2.dp))
                val daysStr = when {
                    state.daysUntilDue < 0 -> "${-state.daysUntilDue}d over"
                    state.daysUntilDue == 0 -> "Today"
                    else -> "${state.daysUntilDue}d"
                }
                Text(
                    text = daysStr,
                    fontSize = 11.sp,
                    color = if (state.daysUntilDue < 3) Color(0xFFE89A7F) else Color(0xFFD4D1CA),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(onPay: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(100.dp))
                .background(FinoColors.ink())
                .clickable { onPay() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mark paid",
                fontSize = 13.sp,
                color = FinoColors.paper(),
                fontWeight = FontWeight.Medium
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(100.dp))
                .border(1.dp, FinoColors.line(), RoundedCornerShape(100.dp))
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Schedule",
                fontSize = 13.sp,
                color = FinoColors.ink(),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryBar(slice: BillCategorySlice) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = slice.categoryName,
                fontSize = 12.sp,
                color = FinoColors.ink(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = AmountFormatter.format(slice.amount),
                fontSize = 12.sp,
                color = FinoColors.ink3()
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(FinoColors.line())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(slice.percentage.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(FinoColors.accentColor())
            )
        }
    }
}

@Composable
private fun BudgetImpactNote() {
    Text(
        text = "Paying this bill on time keeps your budget on track.",
        fontSize = 12.sp,
        color = FinoColors.ink3(),
        modifier = Modifier.padding(top = 8.dp)
    )
}
