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
import com.fino.app.presentation.theme.NumericStyle

/**
 * "Needs a glance" review item on Home.
 * Row 1: merchant 14sp 500 + amount 14sp 500 num.
 * Row 2: "time · bank · channel" 12sp ink-3.
 * Row 3: hint pill (accent-soft bg, or warn-soft if unmatched) — 8×10 padding, 8 radius.
 */
@Composable
fun ReviewItem(
    merchant: String,
    amount: String,
    meta: String,
    hint: String,
    action: String,
    modifier: Modifier = Modifier,
    isUnmatched: Boolean = false,
    onHintClick: (() -> Unit)? = null
) {
    val pillBg = if (isUnmatched) FinoColors.warnSoft() else FinoColors.accentSoft()
    val pillFg = if (isUnmatched) FinoColors.warn() else FinoColors.accentInk()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = merchant,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.height(0.dp))
            Text(
                text = amount,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                style = NumericStyle
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = meta,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = FinoColors.ink3()
        )
        Spacer(Modifier.height(10.dp))
        val clickMod = if (onHintClick != null) Modifier.clickable { onHintClick() } else Modifier
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(pillBg)
                .then(clickMod)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = hint,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = pillFg,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
            Spacer(Modifier.height(0.dp))
            Text(
                text = action,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = pillFg
            )
        }
    }
}
