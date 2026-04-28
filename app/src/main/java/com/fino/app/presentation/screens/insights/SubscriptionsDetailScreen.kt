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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun SubscriptionsDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    DetailScaffold(
        title = "Subscriptions",
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeroTotals(state.monthlyTotal, state.annualTotal) }
            if (state.dormantCount > 0) {
                item { DormantWarnCard(state.dormantCount, state.dormantSavingsAnnual) }
            }
            item { UpcomingTimeline(state.upcomingChargeDates) }
            item { Eyebrow(text = "All subscriptions") }
            if (state.rows.isEmpty()) {
                item {
                    EmptyRow("No active subscriptions detected yet.")
                }
            } else {
                items(state.rows, key = { it.rule.id }) { row ->
                    SubscriptionListRow(
                        row = row,
                        onCancel = { viewModel.cancelSubscription(row.rule.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroTotals(monthly: Double, annual: Double) {
    Column {
        Eyebrow(text = "Monthly cost")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "₹${AmountFormatter.formatCompact(monthly).removePrefix("₹")}/mo",
            style = SerifHero.copy(color = FinoColors.ink(), fontSize = 40.sp, lineHeight = 46.sp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${AmountFormatter.format(annual)}/yr annualized",
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun DormantWarnCard(count: Int, savingsAnnual: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.warnSoft())
            .padding(14.dp)
    ) {
        Text(
            text = "Save ${AmountFormatter.format(savingsAnnual)}/yr",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.warn()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$count subscription${if (count == 1) "" else "s"} haven't been used in 60+ days.",
            fontSize = 12.sp,
            color = FinoColors.ink2()
        )
    }
}

@Composable
private fun UpcomingTimeline(dates: List<LocalDate>) {
    Column {
        Eyebrow(text = "Next 14 days")
        Spacer(Modifier.height(8.dp))
        if (dates.isEmpty()) {
            Text(
                text = "No upcoming charges",
                fontSize = 12.sp,
                color = FinoColors.ink3()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val today = LocalDate.now()
                dates.take(7).forEach { d ->
                    val days = ChronoUnit.DAYS.between(today, d).toInt().coerceAtLeast(0)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(FinoColors.accentColor())
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (days == 0) "Today" else "+${days}d",
                            fontSize = 10.sp,
                            color = FinoColors.ink3()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionListRow(row: SubscriptionRow, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinoColors.ink()
                )
                if (row.isDormant) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(FinoColors.warnSoft())
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Dormant",
                            fontSize = 9.sp,
                            color = FinoColors.warn(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            val nextStr = row.nextChargeDate?.format(DateTimeFormatter.ofPattern("MMM d"))
                ?: "No upcoming charge"
            Text(
                text = "${row.categoryName} · $nextStr",
                fontSize = 12.sp,
                color = FinoColors.ink3()
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = AmountFormatter.format(row.monthlyAmount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Cancel",
                fontSize = 11.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.clickable { onCancel() }
            )
        }
    }
}

@Composable
private fun EmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text = text, fontSize = 13.sp, color = FinoColors.ink3())
    }
}

