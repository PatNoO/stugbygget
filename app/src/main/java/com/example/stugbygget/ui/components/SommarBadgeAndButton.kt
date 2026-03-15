package com.example.stugbygget.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.BorderLight
import com.example.stugbygget.ui.theme.ParchmentSoft
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.buttonShadow

/**
 * Small badge/chip for filter and room labels.
 *
 * Usage:
 *   SommarBadge(text = "Kitchen")
 *   SommarBadge(text = "Before", color = FaluRed)
 */
@Composable
fun SommarBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = ParchmentSoft,
    borderColor: Color = BorderLight,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clip(SommarShapes.badge)
            .background(backgroundColor)
            .border(1.dp, borderColor, SommarShapes.badge)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/**
 * Filter chip that can be active or inactive.
 *
 * Usage:
 *   SommarFilterChip(
 *       text = "Dad",
 *       selected = currentFilter == "Dad",
 *       onClick = { currentFilter = "Dad" }
 *   )
 */
@Composable
fun SommarFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(SommarShapes.badge)
            .background(if (selected) activeColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.5.dp,
                color = if (selected) activeColor else Border,
                shape = SommarShapes.badge,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

/**
 * Primary button with gradient background.
 *
 * Usage:
 *   SommarButton(text = "Save", onClick = { ... })
 */
@Composable
fun SommarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = SommarGradients.faluRed,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .buttonShadow()
            .clip(SommarShapes.button)
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Border, BorderLight)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Secondary outline button for less prominent actions.
 *
 * Usage:
 *   SommarOutlineButton(text = "Cancel", onClick = { ... })
 */
@Composable
fun SommarOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SommarShapes.button)
            .background(Color.Transparent)
            .border(1.5.dp, color, SommarShapes.button)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

/**
 * Phase badge with colour dot for timeline and todo connections.
 *
 * Usage:
 *   SommarPhaseBadge(text = "Painting", color = FaluRedLight)
 */
@Composable
fun SommarPhaseBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(SommarShapes.badge)
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), SommarShapes.badge)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(SommarShapes.badge)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
