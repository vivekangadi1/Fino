package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.EventSubCategorySummary
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter

@Composable
fun SubCategoryCard(
    summary: EventSubCategorySummary,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon and Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = summary.subCategory.emoji,
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = summary.subCategory.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (summary.vendorCount > 0) {
                            Text(
                                text = "${summary.vendorCount} vendor${if (summary.vendorCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Edit button
                if (onEditClick != null) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Budget vs Paid
            if (summary.budgetAmount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = AmountFormatter.format(summary.budgetAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = AmountFormatter.format(summary.paidAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (summary.isOverBudget) ExpenseRed else IncomeGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { (summary.percentUsed / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        summary.isOverBudget -> ExpenseRed
                        summary.isApproachingBudget -> Warning
                        else -> IncomeGreen
                    },
                    trackColor = DarkSurfaceHigh
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Deviation percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${summary.transactionCount} transaction${if (summary.transactionCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = when {
                            summary.percentDeviation > 0 -> "+${String.format("%.1f", summary.percentDeviation)}% over"
                            summary.percentDeviation < 0 -> "${String.format("%.1f", -summary.percentDeviation)}% under"
                            else -> "On budget"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            summary.percentDeviation > 0 -> ExpenseRed
                            summary.percentDeviation < 0 -> IncomeGreen
                            else -> TextSecondary
                        }
                    )
                }
            } else {
                // No budget set
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = AmountFormatter.format(summary.paidAmount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    if (summary.pendingAmount > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = AmountFormatter.format(summary.pendingAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Warning
                            )
                        }
                    }
                }
            }
        }
    }
}
