package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.fino.app.presentation.theme.NumericStyle

/**
 * Active EMI row on Cards screen.
 * 14×0 padding, borderTop 1px line.
 * Row 1: name 14sp 500 + sub 12sp ink-3 / amount 14sp 500 num + "per month" 11sp ink-3.
 * Row 2: 3dp progress bar (paper-3 + accent fill) + "₹21.4L left" 11sp num ink-3.
 */
@Composable
fun EMIRow(
    name: String,
    sub: String,
    monthlyAmount: String,
    progress: Float,
    remainingLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink()
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sub,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = FinoColors.ink3()
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = monthlyAmount,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink(),
                    style = NumericStyle
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "per month",
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = FinoColors.ink3()
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FinoColors.paper3())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(FinoColors.accentColor())
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = remainingLabel,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = FinoColors.ink3(),
                style = NumericStyle
            )
        }
    }
}
