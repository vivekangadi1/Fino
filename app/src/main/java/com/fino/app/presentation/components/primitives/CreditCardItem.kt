package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.Newsreader
import com.fino.app.presentation.theme.NumericStyle

/**
 * Credit card item for the Cards list.
 * Card bg, 16 radius, 16×18 padding, 1px line.
 * Top row: [bank·name uppercase 11sp · "•••• 4523" 14sp letterspacing 0.18em] / [amount 22sp Newsreader · "Due Apr 24 · min ₹625" 11sp].
 * Bottom: 4dp utilization bar (paper-3 bg + accent/warn fill) + "72% utilized" / "of ₹1,20,000".
 */
@Composable
fun CreditCardItem(
    bankAndName: String,
    last4: String,
    due: String,
    dueMeta: String,
    utilizationPercent: Float,
    totalLimitLabel: String,
    modifier: Modifier = Modifier,
    isWarn: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    val fill = if (isWarn) FinoColors.warn() else FinoColors.accentColor()
    val dueColor = if (isWarn) FinoColors.warn() else FinoColors.ink3()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(16.dp))
            .then(clickMod)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankAndName.uppercase(),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 1.1.sp,
                    color = FinoColors.ink3()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = last4,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    letterSpacing = 2.5.sp,
                    color = FinoColors.ink2(),
                    style = NumericStyle
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = due,
                    fontFamily = Newsreader,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.44).sp,
                    color = FinoColors.ink(),
                    style = NumericStyle
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dueMeta,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = dueColor
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(FinoColors.paper3())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(utilizationPercent.coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(fill)
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(utilizationPercent * 100).toInt()}% utilized",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = FinoColors.ink3()
            )
            Text(
                text = totalLimitLabel,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = FinoColors.ink3(),
                style = NumericStyle
            )
        }
    }
}
