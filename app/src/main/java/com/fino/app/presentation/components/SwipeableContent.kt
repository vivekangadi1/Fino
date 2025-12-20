package com.fino.app.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * A composable that wraps content and provides swipe gesture detection.
 *
 * Swipe left triggers [onSwipeLeft], swipe right triggers [onSwipeRight].
 * Uses AnimatedContent for smooth transitions when [currentKey] changes.
 *
 * @param currentKey Key for AnimatedContent transitions
 * @param onSwipeLeft Callback when user swipes left (drag from right to left)
 * @param onSwipeRight Callback when user swipes right (drag from left to right)
 * @param swipeThreshold Minimum horizontal distance (in pixels) to trigger swipe
 * @param enableHaptic Whether to provide haptic feedback on swipe
 * @param modifier Modifier for the container
 * @param content Content to display and make swipeable
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeableContent(
    currentKey: Any,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    swipeThreshold: Float = 100f,
    enableHaptic: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var swipeOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    if (enableHaptic) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    swipeOffset += dragAmount
                    change.consume()
                },
                onDragEnd = {
                    when {
                        swipeOffset > swipeThreshold -> {
                            onSwipeRight()
                            if (enableHaptic) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                        swipeOffset < -swipeThreshold -> {
                            onSwipeLeft()
                            if (enableHaptic) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                    swipeOffset = 0f
                }
            )
        }
    ) {
        AnimatedContent(
            targetState = currentKey,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = spring(dampingRatio = 0.8f),
                    initialOffsetX = { if (swipeOffset > 0) -it else it }
                ) + fadeIn() togetherWith
                slideOutHorizontally(
                    animationSpec = spring(dampingRatio = 0.8f),
                    targetOffsetX = { if (swipeOffset > 0) it else -it }
                ) + fadeOut()
            },
            label = "swipe_content"
        ) {
            content()
        }
    }
}
