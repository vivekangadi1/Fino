package com.fino.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.PaymentMethodBreakdown
import com.fino.app.domain.model.PaymentMethodSpending
import com.fino.app.presentation.theme.*

@Composable
fun PaymentMethodSection(
    paymentMethodBreakdown: PaymentMethodBreakdown?,
    onViewAllUpi: (String) -> Unit = {},
    onViewAllCreditCard: (String) -> Unit = {},
    onViewAllOther: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (paymentMethodBreakdown == null) return

    var expandedUpi by remember { mutableStateOf(false) }
    var expandedCreditCard by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payment Methods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "This Period",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // UPI Section (Expandable)
        if (paymentMethodBreakdown.upiTransactions.isNotEmpty()) {
            ExpandablePaymentMethodCard(
                title = "UPI Payments",
                totalAmount = paymentMethodBreakdown.totalUpiSpend,
                transactionCount = paymentMethodBreakdown.upiTransactions.sumOf { it.transactionCount },
                isExpanded = expandedUpi,
                onToggle = { expandedUpi = !expandedUpi },
                gradient = FinoGradients.Secondary,
                icon = "💳"
            ) {
                // Expanded content
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top 3 merchants
                    paymentMethodBreakdown.upiTransactions.take(3).forEach { spending ->
                        PaymentMethodDetailRow(
                            spending = spending,
                            onClick = { onViewAllUpi(spending.bankName ?: "") }
                        )
                    }

                    // View All Button
                    TextButton(
                        onClick = { onViewAllUpi("") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "View All UPI Transactions",
                            color = Secondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Credit Card Section (Expandable)
        if (paymentMethodBreakdown.creditCardTransactions.isNotEmpty()) {
            ExpandablePaymentMethodCard(
                title = "Credit Card Payments",
                totalAmount = paymentMethodBreakdown.totalCreditCardSpend,
                transactionCount = paymentMethodBreakdown.creditCardTransactions.sumOf { it.transactionCount },
                isExpanded = expandedCreditCard,
                onToggle = { expandedCreditCard = !expandedCreditCard },
                gradient = FinoGradients.Primary,
                icon = "💳"
            ) {
                // Expanded content
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top 3 cards
                    paymentMethodBreakdown.creditCardTransactions.take(3).forEach { spending ->
                        PaymentMethodDetailRow(
                            spending = spending,
                            onClick = { onViewAllCreditCard(spending.cardLastFour ?: spending.bankName ?: "") }
                        )
                    }

                    // View All Button
                    TextButton(
                        onClick = { onViewAllCreditCard("") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "View All Card Transactions",
                            color = Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Unknown Section
        if (paymentMethodBreakdown.unknownTransactions != null) {
            SlideInCard(delay = 450) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewAllOther() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❓", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Other Payments",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${paymentMethodBreakdown.unknownTransactions.transactionCount} transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }

                        Text(
                            text = "₹${paymentMethodBreakdown.totalUnknownSpend.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandablePaymentMethodCard(
    title: String,
    totalAmount: Double,
    transactionCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    gradient: Brush,
    icon: String,
    expandedContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(gradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and transaction count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "$transactionCount transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                // Amount
                Text(
                    text = "₹${totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Expand/collapse icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            // Expandable content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                expandedContent()
            }
        }
    }
}

@Composable
private fun PaymentMethodDetailRow(
    spending: PaymentMethodSpending,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = spending.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${spending.transactionCount} transactions",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${spending.amount.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${(spending.percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View details",
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
