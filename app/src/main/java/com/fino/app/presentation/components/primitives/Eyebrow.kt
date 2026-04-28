package com.fino.app.presentation.components.primitives

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.fino.app.presentation.theme.EyebrowStyle
import com.fino.app.presentation.theme.FinoColors

/**
 * Uppercase monospace metadata label.
 * Used for section anchors, eyebrow text above hero amounts, and metric labels.
 */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FinoColors.ink3()
) {
    Text(
        text = text.uppercase(),
        style = EyebrowStyle,
        color = color,
        modifier = modifier
    )
}
