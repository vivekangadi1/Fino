package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.Transaction
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun PendingPaymentCard(
    transaction: Transaction,
    vendorName: String? = null,
    subCategoryName: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = transaction.isOverdue
    val daysUntilDue = transaction.dueDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it)
    }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isOverdue) ExpenseRed.copy(alpha = 0.1f)
                else DarkSurfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Warning indicator for overdue
            if (isOverdue) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ExpenseRed.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Overdue",
                        tint = ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                // Vendor or merchant name
                Text(
                    text = vendorName ?: transaction.merchantName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                // Sub-category if available
                if (subCategoryName != null) {
                    Text(
                        text = subCategoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Due date info
                if (transaction.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isOverdue && daysUntilDue != null -> "Overdue by ${-daysUntilDue} days"
                            daysUntilDue == 0L -> "Due today"
                            daysUntilDue == 1L -> "Due tomorrow"
                            daysUntilDue != null && daysUntilDue <= 7 -> "Due in $daysUntilDue days"
                            else -> "Due ${transaction.dueDate.format(dateFormatter)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isOverdue -> ExpenseRed
                            daysUntilDue != null && daysUntilDue <= 3 -> Warning
                            else -> TextSecondary
                        }
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AmountFormatter.format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) ExpenseRed else Warning
                )
                Text(
                    text = transaction.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PendingPaymentsSection(
    payments: List<Transaction>,
    vendorNames: Map<Long, String> = emptyMap(),
    subCategoryNames: Map<Long, String> = emptyMap(),
    onPaymentClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (payments.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pending Payments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            val overdueCount = payments.count { it.isOverdue }
            if (overdueCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ExpenseRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$overdueCount overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            payments.take(5).forEach { payment ->
                PendingPaymentCard(
                    transaction = payment,
                    vendorName = payment.eventVendorId?.let { vendorNames[it] },
                    subCategoryName = payment.eventSubCategoryId?.let { subCategoryNames[it] },
                    onClick = { onPaymentClick(payment) }
                )
            }

            if (payments.size > 5) {
                Text(
                    text = "and ${payments.size - 5} more...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
