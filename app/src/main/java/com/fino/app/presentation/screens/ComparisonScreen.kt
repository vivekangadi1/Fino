package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.ComparisonCard
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.ComparisonViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: ComparisonViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Month Comparison",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Period selectors
            item {
                PeriodSelectors(
                    currentMonth = uiState.currentMonth,
                    previousMonth = uiState.previousMonth,
                    currentMonthLabel = uiState.currentMonthLabel,
                    previousMonthLabel = uiState.previousMonthLabel,
                    onCurrentPrevious = { viewModel.navigateCurrentPrevious() },
                    onCurrentNext = { viewModel.navigateCurrentNext() },
                    onPreviousPrevious = { viewModel.navigatePreviousPrevious() },
                    onPreviousNext = { viewModel.navigatePreviousNext() },
                    canNavigateCurrentNext = uiState.currentMonth < YearMonth.now(),
                    canNavigatePreviousNext = uiState.previousMonth < uiState.currentMonth.minusMonths(1)
                )
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Error state
            if (uiState.error != null) {
                item {
                    SlideInCard(delay = 100, modifier = Modifier.padding(horizontal = 20.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }

            // Comparison data
            if (!uiState.isLoading && uiState.comparison != null) {
                item {
                    ComparisonCard(
                        comparison = uiState.comparison!!,
                        currentLabel = uiState.currentMonthLabel,
                        previousLabel = uiState.previousMonthLabel,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelectors(
    currentMonth: YearMonth,
    previousMonth: YearMonth,
    currentMonthLabel: String,
    previousMonthLabel: String,
    onCurrentPrevious: () -> Unit,
    onCurrentNext: () -> Unit,
    onPreviousPrevious: () -> Unit,
    onPreviousNext: () -> Unit,
    canNavigateCurrentNext: Boolean,
    canNavigatePreviousNext: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(20.dp)
    ) {
        Text(
            text = "Select Periods to Compare",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current period selector
        PeriodSelectorRow(
            label = "Current Period",
            monthLabel = currentMonthLabel,
            onPrevious = onCurrentPrevious,
            onNext = onCurrentNext,
            canNavigateNext = canNavigateCurrentNext,
            isHighlighted = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Previous period selector
        PeriodSelectorRow(
            label = "Compare With",
            monthLabel = previousMonthLabel,
            onPrevious = onPreviousPrevious,
            onNext = onPreviousNext,
            canNavigateNext = canNavigatePreviousNext,
            isHighlighted = false
        )
    }
}

@Composable
private fun PeriodSelectorRow(
    label: String,
    monthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canNavigateNext: Boolean,
    isHighlighted: Boolean
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isHighlighted) DarkSurfaceVariant else DarkBackground,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = TextPrimary
                )
            }

            // Month label
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlighted) Primary else TextPrimary
            )

            // Next button
            IconButton(
                onClick = onNext,
                enabled = canNavigateNext,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (canNavigateNext) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = if (canNavigateNext) TextPrimary else TextSecondary
                )
            }
        }
    }
}
