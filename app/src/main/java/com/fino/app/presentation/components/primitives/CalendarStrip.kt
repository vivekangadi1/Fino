package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Horizontal 30-day strip — each day shows weekday initial + date number; days with
 * an event get a 3dp dot below. Used on Upcoming Bills / Events screens.
 */
@Composable
fun CalendarStrip(
    start: LocalDate,
    days: Int = 30,
    markedDays: Set<LocalDate> = emptySet(),
    selected: LocalDate? = null,
    modifier: Modifier = Modifier,
    onSelect: ((LocalDate) -> Unit)? = null
) {
    val dayList = (0 until days).map { start.plusDays(it.toLong()) }
    val listState = rememberLazyListState()
    val today = LocalDate.now()

    LaunchedEffect(selected) {
        val idx = selected?.let { ChronoUnit.DAYS.between(start, it).toInt() } ?: 0
        if (idx in dayList.indices) {
            listState.animateScrollToItem(idx)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(dayList) { day ->
            val isSelected = day == selected
            val isToday = day == today
            val isMarked = day in markedDays
            val bg = when {
                isSelected -> FinoColors.ink()
                isToday -> FinoColors.paper2()
                else -> FinoColors.card()
            }
            val fg = if (isSelected) FinoColors.paper() else FinoColors.ink()
            val fgMeta = if (isSelected) FinoColors.paper() else FinoColors.ink3()
            val clickableMod = if (onSelect != null) Modifier.clickable { onSelect(day) } else Modifier
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .then(clickableMod)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day.format(DateTimeFormatter.ofPattern("EEE")).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                    color = fgMeta
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = fg
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMarked) {
                                if (isSelected) FinoColors.paper() else FinoColors.accentColor()
                            } else androidx.compose.ui.graphics.Color.Transparent
                        )
                )
            }
        }
    }
}
