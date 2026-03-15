package com.example.stugbygget.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.ParchmentSoft
import com.example.stugbygget.ui.theme.SommarShapes

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/**
 * Circular progress ring with animation and optional centre label.
 *
 * Usage:
 *   SommarProgressRing(progress = 0.65f, color = FaluRed, label = "65%")
 */
@Composable
fun SommarProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    strokeWidth: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = Border,
    label: String? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "ringProgress",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = Fraunces,
                    color = color,
                ),
            )
        }
    }
}

/**
 * Linear progress bar with animation.
 *
 * Usage:
 *   SommarProgressBar(progress = 0.4f, color = LakeBlue)
 */
@Composable
fun SommarProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = ParchmentSoft,
    height: Dp = 6.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "barProgress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(SommarShapes.progressBar)
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = animatedProgress)
                .clip(SommarShapes.progressBar)
                .background(color),
        )
    }
}

/**
 * Timeline icon — circle with emoji or checkmark, with optional vertical line below.
 *
 * Usage:
 *   SommarTimelineIcon(icon = "🔨", color = FaluRed, isComplete = true, showLine = true)
 */
@Composable
fun SommarTimelineIcon(
    icon: String,
    color: Color,
    isComplete: Boolean = false,
    showLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(36.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(SommarShapes.timelineIcon)
                .background(if (isComplete) MeadowGreen else color.copy(alpha = 0.1f))
                .border(
                    width = 2.dp,
                    color = if (isComplete) MeadowGreen else color,
                    shape = SommarShapes.timelineIcon,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isComplete) {
                Text("✓", color = Color.White, style = MaterialTheme.typography.titleSmall)
            } else {
                Text(icon, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (showLine) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(20.dp)
                    .background(if (isComplete) MeadowGreen else Border),
            )
        }
    }
}
