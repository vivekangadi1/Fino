package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.CategorySegment
import com.fino.app.domain.model.EventSettleRow
import com.fino.app.domain.model.EventSummary
import com.fino.app.domain.model.FeaturedEventData
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.components.primitives.SettleRow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle
import com.fino.app.presentation.viewmodel.EventsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit,
    onCreateEvent: () -> Unit,
    onEventClick: (Long) -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            EventsTopBar(onBack = onNavigateBack, onCreate = onCreateEvent)
            EventsBody(
                viewModel = viewModel,
                onCreateEvent = onCreateEvent,
                onEventClick = onEventClick
            )
        }
    }
}

@Composable
fun EventsBody(
    viewModel: EventsViewModel = hiltViewModel(),
    onCreateEvent: () -> Unit,
    onEventClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = FinoColors.card(),
            title = { Text("Something went wrong", color = FinoColors.ink()) },
            text = { Text(uiState.error!!, color = FinoColors.ink2()) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK", color = FinoColors.accentColor())
                }
            }
        )
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = FinoColors.accentColor())
        }
        return
    }

    val featured = uiState.featured
    val remainingActive = uiState.activeEvents.drop(1)
    val archived = uiState.completedEvents

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        if (uiState.activeEvents.isEmpty() && archived.isEmpty()) {
            item { EventsEmptyState(onCreateEvent = onCreateEvent) }
            return@LazyColumn
        }

        if (featured != null) {
            item {
                FeaturedEventCard(
                    featured = featured,
                    onClick = { onEventClick(featured.summary.event.id) }
                )
            }
            item { SettleUpBlock(rows = featured.settleRows) }
        }

        val otherEvents = remainingActive + archived
        if (otherEvents.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 12.dp)) {
                    Eyebrow(text = "Other events")
                }
            }
            items(otherEvents, key = { it.event.id }) { summary ->
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)) {
                    EventRow(summary = summary, onClick = { onEventClick(summary.event.id) })
                }
            }
        }
    }
}

@Composable
private fun EventsTopBar(onBack: () -> Unit, onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 18.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FinoColors.ink2(),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Events",
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onCreate),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Create event",
                tint = FinoColors.ink2(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FeaturedEventCard(
    featured: FeaturedEventData,
    onClick: () -> Unit
) {
    val summary = featured.summary
    val event = summary.event
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val dateRange = buildString {
        append(event.startDate.format(dateFormatter))
        event.endDate?.let { append(" — ${it.format(dateFormatter)}") }
    }
    val participantCount = featured.participantCount
    val headerLabel = if (participantCount > 1) {
        "ACTIVE · $participantCount people"
    } else {
        val txCount = summary.transactionCount
        "ACTIVE · $txCount ${if (txCount == 1) "transaction" else "transactions"}"
    }
    val showYourShare = participantCount > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 0.dp)
            .padding(top = 24.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(FinoColors.ink())
            .clickable(onClick = onClick)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = headerLabel,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 1.1.sp,
                color = FinoColors.paper().copy(alpha = 0.6f)
            )
            val statusTag = if (event.excludeFromMainTotals) "STANDALONE" else "IN BUDGET"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(FinoColors.paper().copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusTag,
                    fontFamily = JetBrainsMono,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    letterSpacing = 0.5.sp,
                    color = FinoColors.paper()
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = event.name,
            fontFamily = Newsreader,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.52).sp,
            color = FinoColors.paper()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$dateRange · ${summary.transactionCount} ${if (summary.transactionCount == 1) "transaction" else "transactions"}",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = FinoColors.paper().copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Spent so far",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = FinoColors.paper().copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "₹${formatAmountLoose(summary.totalSpent)}",
                    fontFamily = Newsreader,
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Normal,
                    color = FinoColors.paper(),
                    style = NumericStyle
                )
            }
            when {
                showYourShare -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Your share",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = FinoColors.paper().copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${formatAmountLoose(featured.yourShare)}",
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinoColors.paper(),
                            style = NumericStyle
                        )
                    }
                }
                event.hasBudget && event.budgetAmount != null -> {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Budget",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = FinoColors.paper().copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${formatAmountLoose(event.budgetAmount)}",
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinoColors.paper(),
                            style = NumericStyle
                        )
                    }
                }
            }
        }

        val segments = featured.categorySegments
        if (segments.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            CategorySpendBar(segments = segments)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildCategoryCaption(segments),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = FinoColors.paper().copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (event.hasBudget && event.budgetAmount != null && event.budgetAmount > 0) {
                    val usedPct = ((summary.totalSpent / event.budgetAmount) * 100).toInt().coerceAtLeast(0)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$usedPct% of ₹${formatBudgetCompact(event.budgetAmount)} budget",
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = FinoColors.paper().copy(alpha = 0.7f),
                        style = NumericStyle
                    )
                }
            }
        } else if (event.hasBudget && event.budgetAmount != null && event.budgetAmount > 0) {
            val pct = (summary.totalSpent / event.budgetAmount).toFloat().coerceIn(0f, 1.5f)
            Spacer(Modifier.height(18.dp))
            EmptyBudgetBar(pct = pct)
            Spacer(Modifier.height(10.dp))
            val usedPct = (pct * 100).toInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$usedPct% used",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = FinoColors.paper().copy(alpha = 0.7f)
                )
                val remainingAmount = event.budgetAmount - summary.totalSpent
                val remainingLabel = if (remainingAmount >= 0) {
                    "₹${formatAmountLoose(remainingAmount)} left"
                } else {
                    "₹${formatAmountLoose(-remainingAmount)} over"
                }
                Text(
                    text = remainingLabel,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = FinoColors.paper().copy(alpha = 0.7f),
                    style = NumericStyle
                )
            }
        }
    }
}

@Composable
private fun CategorySpendBar(segments: List<CategorySegment>) {
    val chart = FinoColors.chart()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(FinoColors.paper().copy(alpha = 0.15f)),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        segments.forEach { seg ->
            val weight = seg.fraction.coerceAtLeast(0.0001f)
            val color = chart.getOrNull(seg.paletteIndex) ?: FinoColors.accentColor()
            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun EmptyBudgetBar(pct: Float) {
    val fillable = pct.coerceIn(0f, 1f)
    val accent = FinoColors.accentColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(FinoColors.paper().copy(alpha = 0.15f))
    ) {
        if (fillable > 0) {
            Box(
                modifier = Modifier
                    .weight(fillable)
                    .fillMaxHeight()
                    .background(accent)
            )
        }
        val remainder = 1f - fillable
        if (remainder > 0) {
            Box(
                modifier = Modifier
                    .weight(remainder)
                    .fillMaxHeight()
                    .background(Color.Transparent)
            )
        }
    }
}

private fun buildCategoryCaption(segments: List<CategorySegment>): String {
    val top = segments.filter { it.categoryName != "Other" }.take(3)
    if (top.isEmpty()) return ""
    return top.joinToString(" · ") { seg ->
        val pct = (seg.fraction * 100).toInt().coerceAtLeast(1)
        "${seg.categoryName} $pct%"
    }
}

private fun formatBudgetCompact(amount: Double): String {
    return when {
        amount >= 10_000_000 -> String.format("%.1fCr", amount / 10_000_000)
        amount >= 100_000 -> String.format("%.1fL", amount / 100_000).removeSuffix(".0L") + if (amount % 100_000 == 0.0) "L" else ""
        amount >= 1_000 -> String.format("%dk", (amount / 1_000).toInt())
        else -> String.format("%.0f", amount)
    }
}

@Composable
private fun SettleUpBlock(rows: List<EventSettleRow>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp)
    ) {
        Eyebrow(text = "Settle up")
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FinoColors.card())
                .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (rows.isEmpty()) {
                Text(
                    text = "All settled — no balances to clear.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = FinoColors.ink3()
                )
            } else {
                rows.forEach { row ->
                    val prefix = if (row.owesYou) "+" else "−"
                    SettleRow(
                        name = row.who,
                        owesYou = row.owesYou,
                        amount = "$prefix₹${formatAmountLoose(row.amount)}"
                    )
                }
            }
        }
    }
}

@Composable
private fun EventRow(summary: EventSummary, onClick: () -> Unit) {
    val event = summary.event
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
    val lifecycleLabel = when (event.status.name.lowercase()) {
        "active" -> "active"
        "completed" -> "settled"
        else -> "archived"
    }
    val budgetLabel = if (event.excludeFromMainTotals) "standalone" else "in budget"
    val statusLabel = "$budgetLabel · $lifecycleLabel"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.name,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${event.startDate.format(dateFormatter)} · ${summary.transactionCount} ${if (summary.transactionCount == 1) "tx" else "txns"} · $statusLabel",
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = FinoColors.ink3()
            )
        }
        Text(
            text = "₹${formatAmountLoose(summary.totalSpent)}",
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink(),
            style = NumericStyle
        )
    }
}

@Composable
private fun EventsEmptyState(onCreateEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, FinoColors.line(), RoundedCornerShape(16.dp))
            .background(FinoColors.cardTint())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.EventNote,
            contentDescription = null,
            tint = FinoColors.ink3(),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No events yet",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Gather spending for trips, weddings, or birthdays in one place.",
            fontSize = 12.sp,
            color = FinoColors.ink3(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Create first event",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.accentInk(),
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .clickable(onClick = onCreateEvent)
                .background(FinoColors.accentSoft())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

private fun formatAmountLoose(amount: Double): String {
    if (amount <= 0) return "0"
    return String.format("%,.0f", amount)
}
