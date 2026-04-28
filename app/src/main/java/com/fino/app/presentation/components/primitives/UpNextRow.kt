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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * Single row for Up-next / transaction feed items.
 * Icon (circled, Paper2 bg) · name + meta · right-aligned amount.
 */
@Composable
fun UpNextRow(
    icon: ImageVector,
    name: String,
    meta: String,
    amount: String,
    modifier: Modifier = Modifier,
    amountColor: Color = FinoColors.ink(),
    onClick: (() -> Unit)? = null
) {
    val clickable = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickable)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(FinoColors.paper2()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FinoColors.ink2(),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = FinoColors.ink(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = FinoColors.ink3(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium.merge(NumericStyle),
            color = amountColor
        )
    }
}
