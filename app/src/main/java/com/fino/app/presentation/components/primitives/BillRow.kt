package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.JetBrainsMono
import com.fino.app.presentation.theme.NumericStyle

/**
 * Upcoming bill row.
 * 10×12 padding, 10 radius, card bg, 1px line border.
 * 9sp mono tag (2×6 padding, paper-2 bg, upi/card/bank) · name 13sp 500 + sub 11sp ink-3 · amount right.
 */
@Composable
fun BillRow(
    tag: String,
    name: String,
    sub: String,
    amount: String,
    modifier: Modifier = Modifier,
    isWarn: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(10.dp))
            .then(clickMod)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(FinoColors.paper2())
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = tag.uppercase(),
                fontFamily = JetBrainsMono,
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.4.sp,
                color = FinoColors.ink3()
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (sub.isNotBlank()) {
                Spacer(Modifier.width(1.dp))
                Text(
                    text = sub,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = FinoColors.ink3(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = amount,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (isWarn) FinoColors.warn() else FinoColors.ink(),
            style = NumericStyle
        )
    }
}
