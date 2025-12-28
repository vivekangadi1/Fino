package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.domain.model.PatternSuggestion
import com.fino.app.domain.model.RecurringFrequency
import com.fino.app.presentation.theme.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Card displaying a detected pattern suggestion with confirm/dismiss actions.
 */
@Composable
fun PatternSuggestionCard(
    suggestion: PatternSuggestion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with suggestion badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(AccentCyan.copy(alpha = 0.3f), Info.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = suggestion.displayName,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Suggested recurring bill",
                            color = AccentCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // Confidence badge
                ConfidenceBadge(confidence = suggestion.confidence)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Amount",
                    value = "~${currencyFormatter.format(suggestion.averageAmount)}"
                )
                DetailItem(
                    label = "Frequency",
                    value = suggestion.detectedFrequency.toDisplayString()
                )
                DetailItem(
                    label = "Next Due",
                    value = suggestion.nextExpected.format(dateFormatter)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Occurrence info
            Text(
                text = "Detected ${suggestion.occurrenceCount} occurrences in your history",
                color = TextTertiary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dismiss", fontSize = 13.sp)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bill", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val confidencePercent = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.9f -> Success
        confidence >= 0.7f -> Info
        else -> Warning
    }

    Text(
        text = "$confidencePercent% match",
        color = color,
        fontSize = 10.sp,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextTertiary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Compact version of suggestion card for lists.
 */
@Composable
fun PatternSuggestionCardCompact(
    suggestion: PatternSuggestion,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Info.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Info,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.displayName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "~${currencyFormatter.format(suggestion.averageAmount)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Add",
                    color = Secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun RecurringFrequency.toDisplayString(): String = when (this) {
    RecurringFrequency.ONE_TIME -> "One-Time"
    RecurringFrequency.WEEKLY -> "Weekly"
    RecurringFrequency.MONTHLY -> "Monthly"
    RecurringFrequency.YEARLY -> "Yearly"
}
