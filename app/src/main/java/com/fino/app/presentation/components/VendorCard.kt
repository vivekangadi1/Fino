package com.fino.app.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fino.app.domain.model.EventVendorSummary
import com.fino.app.domain.model.PaymentStatus
import com.fino.app.presentation.theme.*
import com.fino.app.util.AmountFormatter

@Composable
fun VendorCard(
    summary: EventVendorSummary,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                // Name and category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.vendor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (summary.subCategoryName != null) {
                        Text(
                            text = summary.subCategoryName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Status indicator and actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Payment status badge
                    PaymentStatusBadge(status = summary.paymentStatus)

                    // Call button if phone available
                    if (!summary.vendor.phone.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${summary.vendor.phone}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quoted vs Paid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Quoted",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (summary.quotedAmount > 0)
                            AmountFormatter.format(summary.quotedAmount)
                        else "Not set",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (summary.quotedAmount > 0) TextPrimary else TextTertiary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = AmountFormatter.format(summary.paidAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = IncomeGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = AmountFormatter.format(summary.balanceRemaining),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (summary.balanceRemaining > 0) Warning else TextSecondary
                    )
                }
            }

            if (summary.paymentCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${summary.paymentCount} payment${if (summary.paymentCount > 1) "s" else ""} made",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color, backgroundColor) = when (status) {
        PaymentStatus.PAID -> Triple(Icons.Default.Check, IncomeGreen, IncomeGreen.copy(alpha = 0.2f))
        PaymentStatus.PENDING -> Triple(Icons.Default.Schedule, Warning, Warning.copy(alpha = 0.2f))
        PaymentStatus.PARTIAL -> Triple(Icons.Default.Schedule, Info, Info.copy(alpha = 0.2f))
        PaymentStatus.OVERDUE -> Triple(Icons.Default.Schedule, ExpenseRed, ExpenseRed.copy(alpha = 0.2f))
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
    }
}
