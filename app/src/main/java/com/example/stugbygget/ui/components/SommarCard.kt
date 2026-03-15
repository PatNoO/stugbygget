package com.example.stugbygget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.TextLight
import com.example.stugbygget.ui.theme.cardShadow
import com.example.stugbygget.ui.theme.headerShadow

/**
 * Standard card with warm shadow and border.
 */
@Composable
fun SommarCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardShadow()
            .clip(SommarShapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, SommarShapes.card)
            .padding(padding),
        content = content,
    )
}

/**
 * Gradient header card — one per module screen.
 *
 * Usage:
 *   SommarHeaderCard(
 *       gradient = SommarGradients.faluRed,
 *       title = "Planning",
 *       subtitle = "8 phases · Summer 2026",
 *       emoji = "📅"
 *   )
 */
@Composable
fun SommarHeaderCard(
    gradient: Brush,
    title: String,
    subtitle: String? = null,
    emoji: String? = null,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .headerShadow()
            .clip(SommarShapes.headerCard)
            .background(gradient)
            .padding(20.dp),
    ) {
        if (emoji != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(SommarShapes.timelineIcon)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (content != null) {
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Stat card for numeric values — progress percentage, days remaining, etc.
 *
 * Usage:
 *   SommarStatCard(value = "57", label = "days left", color = MeadowGreen)
 */
@Composable
fun SommarStatCard(
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .cardShadow()
            .clip(SommarShapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, SommarShapes.card)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = Fraunces,
                color = color,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextLight,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
