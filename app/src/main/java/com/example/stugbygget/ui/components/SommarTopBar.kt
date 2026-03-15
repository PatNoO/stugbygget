package com.example.stugbygget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarShapes

/**
 * App top bar with logo and optional trailing slot.
 *
 * Usage:
 *   SommarTopBar()
 */
@Composable
fun SommarTopBar(
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "StugBygget",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = Fraunces,
                    color = FaluRed,
                ),
            )
            Text(
                text = "Summer 2026 — Renovation in progress",
                style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(SommarShapes.timelineIcon)
                    .background(MeadowGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("☀️", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * Section title — uppercase monospace, as in the design prototype.
 *
 * Usage:
 *   SommarSectionTitle(text = "Measurements")
 */
@Composable
fun SommarSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text.uppercase(),
        style = MonoStyles.sectionLabel.copy(color = color),
        modifier = modifier.padding(bottom = 12.dp),
    )
}

/**
 * Info/tip box with accent colour.
 *
 * Usage:
 *   SommarInfoBox(emoji = "💡", title = "Tip", text = "Drag furniture to move it.")
 */
@Composable
fun SommarInfoBox(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String = "💡",
    title: String = "Tip",
    accentColor: Color = MidsummerGold,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SommarShapes.card)
            .background(accentColor.copy(alpha = 0.06f))
            .padding(14.dp),
    ) {
        Text(emoji, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(color = accentColor),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                ),
            )
        }
    }
}
