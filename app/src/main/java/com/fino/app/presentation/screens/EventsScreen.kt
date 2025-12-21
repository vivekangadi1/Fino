package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.AnimatedEmptyState
import com.fino.app.presentation.components.EventCard
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.EventFilter
import com.fino.app.presentation.viewmodel.EventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit,
    onCreateEvent: () -> Unit,
    onEventClick: (Long) -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Events",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            // Gradient FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FinoGradients.Primary)
                    .clickable { onCreateEvent() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Event",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Filter chips
            item {
                FilterChips(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    activeCount = uiState.activeEvents.size,
                    completedCount = uiState.completedEvents.size
                )
            }

            // Events list
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            } else if (uiState.filteredEvents.isEmpty()) {
                // Empty state
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurfaceVariant)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedEmptyState(
                            emoji = when (uiState.selectedFilter) {
                                EventFilter.ACTIVE -> "🎯"
                                EventFilter.COMPLETED -> "✅"
                                EventFilter.ALL -> "📋"
                            },
                            title = when (uiState.selectedFilter) {
                                EventFilter.ACTIVE -> "No active events"
                                EventFilter.COMPLETED -> "No completed events"
                                EventFilter.ALL -> "No events yet"
                            },
                            subtitle = "Tap + to create your first event"
                        )
                    }
                }
            } else {
                // Event cards
                items(uiState.filteredEvents, key = { it.event.id }) { eventSummary ->
                    SlideInCard(delay = 100) {
                        EventCard(
                            eventSummary = eventSummary,
                            onClick = { onEventClick(eventSummary.event.id) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: EventFilter,
    onFilterSelected: (EventFilter) -> Unit,
    activeCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "Active ($activeCount)",
            selected = selectedFilter == EventFilter.ACTIVE,
            onClick = { onFilterSelected(EventFilter.ACTIVE) }
        )
        FilterChip(
            label = "Completed ($completedCount)",
            selected = selectedFilter == EventFilter.COMPLETED,
            onClick = { onFilterSelected(EventFilter.COMPLETED) }
        )
        FilterChip(
            label = "All (${activeCount + completedCount})",
            selected = selectedFilter == EventFilter.ALL,
            onClick = { onFilterSelected(EventFilter.ALL) }
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) Primary else DarkSurfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}
