package com.fino.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.FinoBottomNavBar
import com.fino.app.presentation.components.primitives.IconBtn
import com.fino.app.presentation.components.primitives.SegmentedToggle
import com.fino.app.presentation.screens.activity.EventsTabBody
import com.fino.app.presentation.screens.activity.TransactionsTabBody
import com.fino.app.presentation.screens.activity.UpcomingTabBody
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.viewmodel.ActivityViewModel

enum class ActivityTab { TRANSACTIONS, UPCOMING, EVENTS }

fun activityTabFromArg(raw: String?): ActivityTab = when (raw?.lowercase()) {
    "upcoming" -> ActivityTab.UPCOMING
    "events" -> ActivityTab.EVENTS
    else -> ActivityTab.TRANSACTIONS
}

@Composable
fun ActivityScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onAddBill: () -> Unit,
    onEditBill: (Long) -> Unit,
    onEditCreditCardBill: (Long) -> Unit,
    onScanPatterns: () -> Unit,
    onCreateEvent: () -> Unit,
    onEventClick: (Long) -> Unit,
    initialTab: ActivityTab = ActivityTab.TRANSACTIONS,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    LaunchedEffect(initialTab) { tab = initialTab }

    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ActivityTopBar(
                    tab = tab,
                    onFilter = onScanPatterns,
                    onPlus = onCreateEvent
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                ) {
                    SegmentedToggle(
                        options = listOf("Transactions", "Upcoming", "Events"),
                        selectedIndex = when (tab) {
                            ActivityTab.TRANSACTIONS -> 0
                            ActivityTab.UPCOMING -> 1
                            ActivityTab.EVENTS -> 2
                        },
                        onSelect = { idx ->
                            tab = when (idx) {
                                0 -> ActivityTab.TRANSACTIONS
                                1 -> ActivityTab.UPCOMING
                                else -> ActivityTab.EVENTS
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        equalWeight = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    when (tab) {
                        ActivityTab.TRANSACTIONS -> TransactionsTabBody(
                            state = state,
                            onFilterSelect = viewModel::setFilter,
                            onTransactionClick = onTransactionClick
                        )
                        ActivityTab.UPCOMING -> UpcomingTabBody(
                            onAddBill = onAddBill,
                            onEditBill = onEditBill,
                            onEditCreditCardBill = onEditCreditCardBill
                        )
                        ActivityTab.EVENTS -> EventsTabBody(
                            onCreateEvent = onCreateEvent,
                            onEventClick = onEventClick
                        )
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                FinoBottomNavBar(
                    currentRoute = "activity",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> onNavigateToHome()
                            "insights" -> onNavigateToAnalytics()
                            "cards" -> onNavigateToCards()
                        }
                    },
                    onAddClick = onAddTransaction
                )
            }
        }
    }
}

@Composable
private fun ActivityTopBar(
    tab: ActivityTab,
    onFilter: () -> Unit,
    onPlus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 18.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Activity",
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink()
        )
        if (tab == ActivityTab.EVENTS) {
            IconBtn(
                icon = Icons.Outlined.Add,
                contentDescription = "Create event",
                onClick = onPlus
            )
        } else {
            IconBtn(
                icon = Icons.Outlined.FilterList,
                contentDescription = "Filter",
                onClick = onFilter
            )
        }
    }
}
