package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * Add-transaction field row: label (80dp, 12sp ink-3) / optional swatch / value 14sp 500 / optional right tag.
 * 14×0 padding, borderTop 1px line.
 */
@Composable
fun FieldRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    swatchColor: Color? = null,
    rightTag: String? = null,
    valueMuted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                modifier = Modifier.width(80.dp)
            )
            if (swatchColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(swatchColor)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = value,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = if (valueMuted) FinoColors.ink4() else FinoColors.ink(),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (rightTag != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FinoColors.paper2())
                        .border(1.dp, FinoColors.line(), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = rightTag,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = FinoColors.ink3()
                    )
                }
            }
        }
    }
}
