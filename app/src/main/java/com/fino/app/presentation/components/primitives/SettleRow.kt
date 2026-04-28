package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * Event split "settle up" row.
 * 30dp avatar circle (paper-2 bg, 1px line, first letter ink-2 12sp 500) / name + direction / amount right.
 */
@Composable
fun SettleRow(
    name: String,
    owesYou: Boolean,
    amount: String,
    modifier: Modifier = Modifier
) {
    val firstLetter = name.firstOrNull()?.uppercase() ?: ""
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(FinoColors.paper2())
                .border(1.dp, FinoColors.line(), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstLetter,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink2()
            )
        }
        Spacer(Modifier.width(12.dp))
        val direction = if (owesYou) " owes you" else " you owe"
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = FinoColors.ink())) {
                    append(name)
                }
                withStyle(SpanStyle(color = FinoColors.ink3())) {
                    append(direction)
                }
            },
            fontSize = 14.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = amount,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (owesYou) FinoColors.positive() else FinoColors.negative(),
            style = NumericStyle
        )
    }
}
