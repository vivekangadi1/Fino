package com.fino.app.presentation.screens.activity

import androidx.compose.runtime.Composable
import com.fino.app.presentation.screens.EventsBody

@Composable
fun EventsTabBody(
    onCreateEvent: () -> Unit,
    onEventClick: (Long) -> Unit
) {
    EventsBody(
        onCreateEvent = onCreateEvent,
        onEventClick = onEventClick
    )
}
