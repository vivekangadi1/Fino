package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * A small metric block — eyebrow label above a value in tabular figures,
 * with an optional DeltaPill beneath. Meant for "this month / last month / avg"
 * rows on Home and Insights.
 */
@Composable
fun MetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = FinoColors.ink(),
    delta: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        Eyebrow(text = label)
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.merge(NumericStyle),
            color = valueColor
        )
        if (delta != null) {
            Spacer(Modifier.height(6.dp))
            delta()
        }
    }
}
