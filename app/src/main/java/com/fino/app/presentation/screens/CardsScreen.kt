package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.CreditCard
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToRewards: () -> Unit,
    viewModel: CardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentRoute by remember { mutableStateOf("cards") }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            // Gradient FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FinoGradients.Primary)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Card",
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
                        "home" -> onNavigateToHome()
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
            // Header
            item {
                CardsHeader()
            }

            // Cards Section
            item {
                CardsSectionContent(
                    cards = uiState.cards,
                    totalCreditLimit = uiState.totalCreditLimit,
                    totalOutstanding = uiState.totalOutstanding,
                    isLoading = uiState.isLoading
                )
            }

            // Tips Section
            item {
                CardsTipsSection()
            }
        }
    }
}

@Composable
private fun CardsHeader() {
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
            Text(
                text = "Credit Cards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Manage your cards and track due dates",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun CardsSectionContent(
    cards: List<CreditCard>,
    totalCreditLimit: Double,
    totalOutstanding: Double,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (cards.isEmpty()) {
            // Empty State - Credit Card Visual
            SlideInCard(delay = 100) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.586f) // Credit card aspect ratio
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DarkSurfaceVariant, DarkSurfaceHigh)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Credit card outline icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No cards added yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Add your credit cards to track spending",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // Summary Section
            SlideInCard(delay = 100) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Limit",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            AnimatedCounter(
                                targetValue = totalCreditLimit.toInt(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                prefix = "₹",
                                formatAsRupees = true
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Outstanding",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            AnimatedCounter(
                                targetValue = totalOutstanding.toInt(),
                                style = MaterialTheme.typography.titleMedium,
                                color = ExpenseRed,
                                prefix = "₹",
                                formatAsRupees = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Credit Cards List
            cards.forEachIndexed { index, card ->
                SlideInCard(delay = 150 + (index * 50)) {
                    CreditCardItem(card)
                }
                if (index < cards.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Card Button
        SlideInCard(delay = if (cards.isEmpty()) 200 else 300 + (cards.size * 50)) {
            BouncyButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                gradient = FinoGradients.Primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Credit Card",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun CreditCardItem(card: CreditCard) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Primary.copy(alpha = 0.3f),
                        Secondary.copy(alpha = 0.2f)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.bankName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "•••• ${card.lastFourDigits}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            card.cardName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Credit Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "₹${(card.creditLimit ?: 0.0).toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Outstanding",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    val outstanding = card.currentUnbilled + card.previousDue
                    Text(
                        text = "₹${outstanding.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (outstanding > 0) ExpenseRed else TextPrimary
                    )
                }
            }

            // Due date info if available
            card.dueDateDay?.let { dueDay ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due on ${dueDay}th of every month",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CardsTipsSection() {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Text(
            text = "Card Benefits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Tips List
        val tips = listOf(
            CardTip("📅", "Due Date Alerts", "Never miss a payment again"),
            CardTip("📊", "Spending Insights", "Track spending per card"),
            CardTip("💳", "Bill Tracking", "Auto-detect card transactions"),
            CardTip("🎯", "Credit Score", "Coming soon")
        )

        tips.forEachIndexed { index, tip ->
            SlideInCard(delay = 300 + (index * 50)) {
                TipRow(tip)
            }
            if (index < tips.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TipRow(tip: CardTip) {
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
            Text(
                text = tip.emoji,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = tip.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

private data class CardTip(
    val emoji: String,
    val title: String,
    val subtitle: String
)
