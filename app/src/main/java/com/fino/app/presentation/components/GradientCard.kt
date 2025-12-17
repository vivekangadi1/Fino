package com.fino.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*

/**
 * A modern card with gradient background and optional glow effect.
 * Used throughout Fino for stats, transactions, achievements, etc.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = FinoGradients.Primary,
    cornerRadius: Dp = 16.dp,
    glowColor: Color? = null,
    glowRadius: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (glowColor != null) {
                    Modifier.drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.maxDimension + glowRadius.toPx()
                            )
                        )
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(gradient)
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * A card with solid dark surface color and subtle border.
 * Used for transaction items and list cards.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = DarkSurfaceVariant,
    borderColor: Color? = Border,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * A card with left accent border for category indication.
 * Used for transaction items to show category color.
 */
@Composable
fun AccentBorderCard(
    modifier: Modifier = Modifier,
    accentColor: Color,
    accentWidth: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = DarkSurfaceVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        onClick = onClick ?: {}
    ) {
        Row {
            // Accent border on left
            Box(
                modifier = Modifier
                    .width(accentWidth)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * A card with shimmer loading effect.
 * Used as placeholder while content is loading.
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            DarkSurfaceVariant.copy(alpha = 0.6f),
            DarkSurfaceHigh.copy(alpha = 0.2f),
            DarkSurfaceVariant.copy(alpha = 0.6f)
        ),
        start = Offset(translateAnimation - 500f, 0f),
        end = Offset(translateAnimation, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush)
    )
}

/**
 * Stats card with gradient background for displaying metrics.
 * Shows value with label and optional icon.
 */
@Composable
fun StatsGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush,
    glowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GradientCard(
        modifier = modifier,
        gradient = gradient,
        cornerRadius = 20.dp,
        glowColor = glowColor,
        content = content
    )
}

/**
 * Achievement card with locked/unlocked states.
 * Shows different visuals based on unlock status.
 */
@Composable
fun AchievementGradientCard(
    modifier: Modifier = Modifier,
    isUnlocked: Boolean,
    tierGradient: Brush = FinoGradients.Gold,
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundGradient = if (isUnlocked) {
        tierGradient
    } else {
        Brush.linearGradient(
            colors = listOf(AchievementLocked, AchievementLocked.copy(alpha = 0.7f))
        )
    }

    val alpha = if (isUnlocked) 1f else 0.5f

    Box(
        modifier = modifier.graphicsLayer { this.alpha = alpha }
    ) {
        GradientCard(
            gradient = backgroundGradient,
            cornerRadius = 16.dp,
            glowColor = if (isUnlocked) Accent else null,
            content = content
        )
    }
}

/**
 * Credit card style card with 3D tilt effect.
 */
@Composable
fun CreditCardStyleCard(
    modifier: Modifier = Modifier,
    gradient: Brush = FinoGradients.CardBlue,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.586f) // Standard credit card ratio
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(20.dp),
        content = content
    )
}

/**
 * Mini stat card for compact displays.
 */
@Composable
fun MiniStatCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurfaceVariant,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        content = content
    )
}
