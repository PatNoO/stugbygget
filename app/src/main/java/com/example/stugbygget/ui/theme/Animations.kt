package com.example.stugbygget.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

// ── Easing curves ──

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

// ── Fade up in — standard entry animation ──
// Usage: Box(modifier = Modifier.fadeUpIn(delay = 100))

fun Modifier.fadeUpIn(
    delay: Int = 0,
    duration: Int = 400,
    slideDistance: Float = 24f,
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val animSpec = tween<Float>(
        durationMillis = duration,
        delayMillis = delay,
        easing = EaseOutCubic,
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animSpec,
        label = "fadeUpAlpha",
    )
    val translationY by animateFloatAsState(
        targetValue = if (visible) 0f else slideDistance,
        animationSpec = animSpec,
        label = "fadeUpTranslation",
    )

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }
}

// ── Staggered fade in — for lists with index ──
// Usage: items.forEachIndexed { index, item -> ItemCard(modifier = Modifier.staggeredFadeIn(index)) }

fun Modifier.staggeredFadeIn(
    index: Int,
    baseDelay: Int = 50,
    duration: Int = 350,
): Modifier = fadeUpIn(
    delay = index * baseDelay,
    duration = duration,
)

// ── Pulse animation — for active indicators ──
// Usage: val pulse = pulseAnimation(); Box(modifier = Modifier.alpha(pulse))

@Composable
fun pulseAnimation(
    minAlpha: Float = 0.4f,
    maxAlpha: Float = 1f,
    duration: Int = 1000,
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = maxAlpha,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )
    return alpha
}

// ── Progress animation — smooth progress value transitions ──
// Usage: val animatedProgress = animateProgress(targetProgress = phase.progress)

@Composable
fun animateProgress(
    targetProgress: Float,
    duration: Int = 1000,
    delay: Int = 0,
): Float {
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = EaseOutCubic,
        ),
        label = "progressAnimation",
    )
    return animatedProgress
}

// ── Shared enter/exit transitions for Navigation Compose ──

object SommarTransitions {
    val enterTransition: EnterTransition =
        fadeIn(tween(300)) + slideInVertically(
            initialOffsetY = { it / 20 },
            animationSpec = tween(300, easing = EaseOutCubic),
        )

    val exitTransition: ExitTransition =
        fadeOut(tween(200))

    val popEnterTransition: EnterTransition =
        fadeIn(tween(300))

    val popExitTransition: ExitTransition =
        fadeOut(tween(200)) + slideOutVertically(
            targetOffsetY = { it / 20 },
            animationSpec = tween(200),
        )
}
