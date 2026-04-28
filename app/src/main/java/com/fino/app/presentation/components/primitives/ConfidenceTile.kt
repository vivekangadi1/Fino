package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import kotlin.math.roundToInt

/**
 * Radio-style selectable row with a confidence bar beneath it.
 * Used on the Review / smart-match flows.
 */
@Composable
fun ConfidenceTile(
    label: String,
    confidence: Float,
    selected: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val borderColor = if (selected) FinoColors.accentColor() else FinoColors.line()
    val background = if (selected) FinoColors.accentSoft() else FinoColors.card()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (selected) 5.dp else 1.dp,
                        color = if (selected) FinoColors.accentColor() else FinoColors.ink4(),
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FinoColors.ink()
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = FinoColors.ink3()
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${(confidence.coerceIn(0f, 1f) * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono
                ),
                color = FinoColors.ink3()
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(FinoColors.line2())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(confidence.coerceIn(0f, 1f))
                    .fillMaxSize()
                    .clip(RoundedCornerShape(1.dp))
                    .background(FinoColors.accentColor())
            )
        }
    }
}
