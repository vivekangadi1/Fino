package com.fino.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.fino.app.presentation.theme.*
import java.time.YearMonth

@Composable
fun PeriodPickerDialog(
    currentSelection: YearMonth,
    monthlySpending: List<MonthSpendingData>,
    onDismiss: () -> Unit,
    onPeriodSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedYear by remember { mutableStateOf(currentSelection.year) }
    var tempSelectedMonth by remember { mutableStateOf(currentSelection) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Period",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear-- },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Year",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )

                    IconButton(
                        onClick = { selectedYear++ },
                        enabled = selectedYear < YearMonth.now().year,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (selectedYear < YearMonth.now().year)
                                    DarkSurfaceVariant
                                else
                                    DarkSurfaceVariant.copy(alpha = 0.3f)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Year",
                            tint = if (selectedYear < YearMonth.now().year) TextPrimary else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Spending Heatmap
                SpendingHeatmap(
                    year = selectedYear,
                    monthlySpending = monthlySpending.filter { it.yearMonth.year == selectedYear },
                    selectedMonth = tempSelectedMonth,
                    onMonthSelected = { yearMonth ->
                        tempSelectedMonth = yearMonth
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending Intensity:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Low",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f).forEach { intensity ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        ExpenseRed.copy(alpha = intensity),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }

                        Text(
                            text = "High",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onPeriodSelected(tempSelectedMonth)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}
