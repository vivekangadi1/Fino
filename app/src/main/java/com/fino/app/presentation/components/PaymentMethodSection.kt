package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.PaymentMethodBreakdown
import com.fino.app.domain.model.PaymentMethodSpending
import com.fino.app.presentation.theme.*

@Composable
fun PaymentMethodSection(
    paymentMethodBreakdown: PaymentMethodBreakdown?,
    modifier: Modifier = Modifier
) {
    if (paymentMethodBreakdown == null) return

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

        // UPI Section
        if (paymentMethodBreakdown.upiTransactions.isNotEmpty()) {
            Text(
                text = "UPI Payments (₹${paymentMethodBreakdown.totalUpiSpend.toInt()})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            paymentMethodBreakdown.upiTransactions.forEachIndexed { index, spending ->
                SlideInCard(delay = 350 + (index * 50)) {
                    PaymentMethodRow(
                        spending = spending,
                        gradient = FinoGradients.Secondary, // Cyan for UPI
                        icon = "💳"
                    )
                }
                if (index < paymentMethodBreakdown.upiTransactions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Credit Card Section
        if (paymentMethodBreakdown.creditCardTransactions.isNotEmpty()) {
            Text(
                text = "Credit Card Payments (₹${paymentMethodBreakdown.totalCreditCardSpend.toInt()})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            paymentMethodBreakdown.creditCardTransactions.forEachIndexed { index, spending ->
                SlideInCard(delay = 400 + (index * 50)) {
                    PaymentMethodRow(
                        spending = spending,
                        gradient = FinoGradients.Primary, // Purple for Credit Cards
                        icon = "💳"
                    )
                }
                if (index < paymentMethodBreakdown.creditCardTransactions.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Unknown Section
        if (paymentMethodBreakdown.unknownTransactions != null) {
            Text(
                text = "Other (₹${paymentMethodBreakdown.totalUnknownSpend.toInt()})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SlideInCard(delay = 450) {
                PaymentMethodRow(
                    spending = paymentMethodBreakdown.unknownTransactions,
                    gradient = Brush.linearGradient(
                        listOf(DarkSurfaceHigh, DarkSurfaceHigh)
                    ),
                    icon = "❓"
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    spending: PaymentMethodSpending,
    gradient: Brush,
    icon: String
) {
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
            // Payment Method Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spending.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${spending.transactionCount} transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${spending.amount.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${(spending.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress bar
                AnimatedGradientProgress(
                    progress = spending.percentage,
                    gradient = gradient,
                    backgroundColor = DarkSurfaceHigh,
                    height = 4.dp
                )
            }
        }
    }
}
