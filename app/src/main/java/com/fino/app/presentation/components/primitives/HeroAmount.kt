package com.fino.app.presentation.components.primitives

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.FinoColors
import com.fino.app.presentation.theme.SerifHero
import com.fino.app.presentation.theme.SerifMedium

enum class HeroAmountSize { Large, Medium }

@Composable
fun HeroAmount(
    amount: String,
    modifier: Modifier = Modifier,
    size: HeroAmountSize = HeroAmountSize.Large,
    color: Color = FinoColors.ink(),
    align: TextAlign = TextAlign.Start
) {
    val style = when (size) {
        HeroAmountSize.Large -> SerifHero
        HeroAmountSize.Medium -> SerifMedium
    }
    Text(
        text = amount,
        style = style,
        color = color,
        textAlign = align,
        modifier = modifier
    )
}

@Composable
fun HeroBlock(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    size: HeroAmountSize = HeroAmountSize.Large,
    color: Color = FinoColors.ink(),
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        Eyebrow(text = label)
        Spacer(modifier = Modifier.height(8.dp))
        HeroAmount(amount = amount, size = size, color = color)
        if (trailing != null) {
            Spacer(modifier = Modifier.height(10.dp))
            trailing()
        }
    }
}
