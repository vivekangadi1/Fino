package com.fino.app.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*

@Composable
fun PeriodJumpChips(
    onJumpToLastMonth: () -> Unit,
    onJumpTo3MonthsAgo: () -> Unit,
    onJumpToLastYear: () -> Unit,
    onJumpToSameMonthLastYear: () -> Unit,
    onCompareMonths: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            JumpChip(
                label = "Last Month",
                onClick = onJumpToLastMonth
            )
        }
        item {
            JumpChip(
                label = "3 Months Ago",
                onClick = onJumpTo3MonthsAgo
            )
        }
        item {
            JumpChip(
                label = "Last Year",
                onClick = onJumpToLastYear
            )
        }
        item {
            JumpChip(
                label = "Same Month Last Year",
                onClick = onJumpToSameMonthLastYear
            )
        }
        item {
            JumpChip(
                label = "Compare Months",
                onClick = onCompareMonths,
                isHighlighted = true
            )
        }
    }
}

@Composable
private fun JumpChip(
    label: String,
    onClick: () -> Unit,
    isHighlighted: Boolean = false
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isHighlighted) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (isHighlighted) Primary.copy(alpha = 0.2f) else DarkSurfaceVariant,
            labelColor = if (isHighlighted) Primary else TextPrimary
        )
    )
}
