package com.fino.app.presentation.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.util.AmountFormatter
import java.time.format.DateTimeFormatter

@Composable
fun NewMerchantsDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenMerchant: (String) -> Unit,
    viewModel: NewMerchantsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    DetailScaffold(
        title = "New merchants",
        onNavigateBack = onNavigateBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { NewMerchantsHero(state) }
            item {
                Spacer(Modifier.height(4.dp))
                Eyebrow(text = "First-time visits")
            }
            if (state.rows.isEmpty()) {
                item {
                    Text(
                        text = "No new merchants this period.",
                        fontSize = 13.sp,
                        color = FinoColors.ink3()
                    )
                }
            } else {
                items(state.rows) { row ->
                    NewMerchantRowView(row = row, onClick = { onOpenMerchant(row.merchantKey) })
                }
            }
        }
    }
}

@Composable
private fun NewMerchantsHero(state: NewMerchantsDetailUiState) {
    Column {
        Eyebrow(text = state.periodLabel.ifBlank { "This period" })
        Spacer(Modifier.height(8.dp))
        Text(
            text = AmountFormatter.formatCompact(state.totalSpent),
            style = SerifHero.copy(color = FinoColors.ink(), fontSize = 40.sp, lineHeight = 46.sp)
        )
        Spacer(Modifier.height(4.dp))
        val merchantWord = if (state.merchantCount == 1) "merchant" else "merchants"
        Text(
            text = "${state.merchantCount} new $merchantWord this period.",
            fontSize = 13.sp,
            color = FinoColors.ink3()
        )
    }
}

@Composable
private fun NewMerchantRowView(row: NewMerchantRow, onClick: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("MMM d")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink()
            )
            Spacer(Modifier.height(2.dp))
            val countStr = if (row.count == 1) "1 visit" else "${row.count} visits"
            Text(
                text = "${row.firstSeen.format(fmt)} · ${row.categoryName} · $countStr",
                fontSize = 11.sp,
                color = FinoColors.ink3()
            )
        }
        Text(
            text = AmountFormatter.format(row.amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FinoColors.ink()
        )
    }
}
