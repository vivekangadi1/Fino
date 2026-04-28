package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.NumericStyle

/**
 * Day group header: date 13sp 600 num + weekday 11sp ink-3 · right total 13sp 500 num.
 * Content inside is rendered by the caller (typically BillRow list with gap 8).
 */
@Composable
fun DayGroup(
    dateLabel: String,
    weekdayLabel: String,
    total: String,
    modifier: Modifier = Modifier,
    isWarn: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = dateLabel,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FinoColors.ink(),
                    style = NumericStyle
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = weekdayLabel,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = FinoColors.ink3()
                )
            }
            Text(
                text = total,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isWarn) FinoColors.warn() else FinoColors.ink2(),
                style = NumericStyle
            )
        }
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}
