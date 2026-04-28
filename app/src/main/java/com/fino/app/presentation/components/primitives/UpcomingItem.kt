package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * Up-next row for Home feed.
 * 6×36dp colored bar (warn if due, accent if upcoming) + name + meta + right amount.
 * 12×0 padding, borderTop 1px line.
 */
@Composable
fun UpcomingItem(
    name: String,
    meta: String,
    amount: String,
    modifier: Modifier = Modifier,
    isDue: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val barColor: Color = if (isDue) FinoColors.warn() else FinoColors.accentColor()
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod)
            .border(
                width = 1.dp,
                color = Color.Transparent,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 6.dp, height = 36.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(barColor)
        )
        Spacer(Modifier.width(12.dp))
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
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = meta,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = FinoColors.ink3(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = amount,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            color = FinoColors.ink(),
            style = NumericStyle
        )
    }
}
