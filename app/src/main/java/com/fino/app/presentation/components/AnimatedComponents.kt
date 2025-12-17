package com.fino.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fino.app.presentation.theme.*
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/**
 * A pulsing dot indicator for active states.
 * Used for live indicators, online status, etc.
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    color: Color = Success,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Animated counter that smoothly transitions between values.
 * Perfect for displaying balances, XP, stats that change.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = TextPrimary,
    prefix: String = "",
    suffix: String = "",
    formatAsRupees: Boolean = false,
    duration: Int = 500
) {
    var oldValue by remember { mutableIntStateOf(targetValue) }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
        label = "counter"
    )

    LaunchedEffect(targetValue) {
        oldValue = targetValue
    }

    val displayValue = if (formatAsRupees) {
        NumberFormat.getNumberInstance(Locale("en", "IN")).format(animatedValue)
    } else {
        animatedValue.toString()
    }

    Text(
        text = "$prefix$displayValue$suffix",
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Animated counter for floating point values.
 */
@Composable
fun AnimatedFloatCounter(
    targetValue: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = TextPrimary,
    prefix: String = "",
    suffix: String = "",
    decimalPlaces: Int = 1,
    duration: Int = 500
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing),
        label = "float_counter"
    )

    Text(
        text = "$prefix${String.format("%.${decimalPlaces}f", animatedValue)}$suffix",
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * A button that bounces/scales on press.
 * More playful than standard Material button.
 */
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = FinoGradients.Primary,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(TextTertiary, TextTertiary)))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * A column where children fade in sequentially.
 * Great for list items appearing one after another.
 */
@Composable
fun FadeInColumn(
    modifier: Modifier = Modifier,
    staggerDelay: Long = 100L,
    content: @Composable ColumnScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }

    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 4 }
                    )
        ) {
            Column(content = content)
        }
    }
}

/**
 * A card that slides in from the bottom with spring animation.
 */
@Composable
fun SlideInCard(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = tween(200))
    ) {
        content()
    }
}

/**
 * Animated progress bar with gradient fill.
 */
@Composable
fun AnimatedGradientProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    gradient: Brush = FinoGradients.Primary,
    backgroundColor: Color = DarkSurfaceVariant,
    height: Dp = 8.dp,
    cornerRadius: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(gradient)
        )
    }
}

/**
 * Circular progress indicator with animated gradient.
 */
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color = DarkSurfaceVariant,
    progressColor: Color = Primary,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "circular_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            strokeWidth = strokeWidth,
            trackColor = backgroundColor
        )
        content()
    }
}

/**
 * Sparkle/shine effect for celebratory moments.
 */
@Composable
fun SparkleEffect(
    modifier: Modifier = Modifier,
    color: Color = Accent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

    Text(
        text = "✨",
        modifier = modifier
            .scale(scale)
            .graphicsLayer { rotationZ = rotation },
        color = color
    )
}

/**
 * Fire animation for streaks.
 */
@Composable
fun FireAnimation(
    modifier: Modifier = Modifier,
    size: TextStyle = MaterialTheme.typography.headlineMedium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire_scale"
    )

    Text(
        text = "🔥",
        style = size,
        modifier = modifier.scale(scale)
    )
}

/**
 * Bouncing arrow for "see more" indicators.
 */
@Composable
fun BouncingArrow(
    modifier: Modifier = Modifier,
    color: Color = TextSecondary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )

    Text(
        text = "→",
        color = color,
        modifier = modifier.graphicsLayer { translationX = offsetY }
    )
}

/**
 * Celebration confetti burst (simple version).
 */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    show: Boolean = false
) {
    AnimatedVisibility(
        visible = show,
        modifier = modifier,
        enter = scaleIn(animationSpec = spring(dampingRatio = 0.5f)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayMedium
        )
    }
}

/**
 * Loading spinner with brand colors.
 */
@Composable
fun FinoLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = Primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 4.dp
    )
}

/**
 * Empty state with animated illustration.
 */
@Composable
fun AnimatedEmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "empty_bounce"
    )

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.graphicsLayer { translationY = bounce }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
