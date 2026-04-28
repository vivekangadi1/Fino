package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors

/**
 * Hairline line at 0.5dp. Use between rows and sections to keep the page calm.
 */
@Composable
fun HairlineDivider(
    modifier: Modifier = Modifier,
    color: Color = FinoColors.line(),
    thickness: Dp = 0.5.dp
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = thickness,
        color = color
    )
}
