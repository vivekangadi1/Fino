package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * Card-tint stat cell used in 3-column grid on Home.
 * Label 11sp ink-3 / value 17sp 500 num with -0.02em / delta 11sp positive|ink-3.
 */
@Composable
fun Mini(
    label: String,
    value: String,
    delta: String,
    modifier: Modifier = Modifier,
    positive: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FinoColors.cardTint())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = FinoColors.ink3(),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.34).sp,
            color = FinoColors.ink(),
            style = NumericStyle
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = delta,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = if (positive) FinoColors.positive() else FinoColors.ink3()
        )
    }
}
