package com.example.stugbygget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.pulseAnimation

/**
 * Skeleton loading card — animated placeholder for content not yet loaded.
 *
 * Usage:
 *   SommarShimmerCard(lines = 3)
 */
@Composable
fun SommarShimmerCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
) {
    val alpha = pulseAnimation()
    SommarCard(modifier = modifier) {
        repeat(lines) { i ->
            Box(
                modifier = Modifier
                    .padding(bottom = if (i < lines - 1) 8.dp else 0.dp)
                    .fillMaxWidth(if (i == lines - 1) 0.6f else 1f)
                    .height(14.dp)
                    .clip(SommarShapes.button)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .alpha(alpha),
            )
        }
    }
}

/**
 * Inline error card with optional retry action.
 *
 * Usage:
 *   SommarErrorCard(message = "Failed to load", onRetry = viewModel::onRetry)
 */
@Composable
fun SommarErrorCard(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    SommarCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚠",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
        }
        if (onRetry != null) {
            SommarOutlineButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
