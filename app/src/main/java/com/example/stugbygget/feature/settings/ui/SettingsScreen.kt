package com.example.stugbygget.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stugbygget.ui.theme.AppTheme
import com.example.stugbygget.ui.theme.DarkMode
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.SommarSky
import com.example.stugbygget.ui.theme.SommarSunshine
import com.example.stugbygget.ui.theme.SverigeBlue
import com.example.stugbygget.ui.theme.SverigeYellow
import com.example.stugbygget.ui.theme.VirkeOrange
import com.example.stugbygget.ui.theme.VirkePine

private data class ThemeOption(
    val theme: AppTheme,
    val label: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
)

private val themeOptions = listOf(
    ThemeOption(
        theme = AppTheme.SVENSK_SOMMAR,
        label = "Svensk Sommar",
        description = "Traditionell — Faluröd & sjöblå",
        primaryColor = FaluRed,
        secondaryColor = Color(0xFF2E6B8A),
        accentColor = Color(0xFFD4A843),
    ),
    ThemeOption(
        theme = AppTheme.SVERIGE,
        label = "Sverige",
        description = "Svenska flaggans blå & gult",
        primaryColor = SverigeBlue,
        secondaryColor = SverigeYellow,
        accentColor = Color(0xFF004E7C),
    ),
    ThemeOption(
        theme = AppTheme.SOMMARDAG,
        label = "Sommardag",
        description = "Ljus & pastellig sommarkänsla",
        primaryColor = SommarSky,
        secondaryColor = SommarSunshine,
        accentColor = Color(0xFF6FAF72),
    ),
    ThemeOption(
        theme = AppTheme.VIRKE,
        label = "Virke",
        description = "Trä, furu & byggkänsla",
        primaryColor = VirkePine,
        secondaryColor = VirkeOrange,
        accentColor = Color(0xFF787878),
    ),
)

@Composable
fun SettingsScreen(
    appThemeViewModel: AppThemeViewModel,
) {
    val themeState by appThemeViewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // ── Theme picker ──
        SettingsSection(title = "Färgtema") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                themeOptions.forEach { option ->
                    ThemeOptionCard(
                        option = option,
                        selected = themeState.appTheme == option.theme,
                        onClick = { appThemeViewModel.setTheme(option.theme) },
                    )
                }
            }
        }

        // ── Dark mode ──
        SettingsSection(title = "Ljus / mörkt läge") {
            DarkModeSelector(
                current = themeState.darkMode,
                onSelect = { appThemeViewModel.setDarkMode(it) },
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        content()
    }
}

@Composable
private fun ThemeOptionCard(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.outline

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ColorSwatches(
                primary = option.primaryColor,
                secondary = option.secondaryColor,
                accent = option.accentColor,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun ColorSwatches(
    primary: Color,
    secondary: Color,
    accent: Color,
) {
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primary)
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(secondary)
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent)
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        )
    }
}

@Composable
private fun DarkModeSelector(
    current: DarkMode,
    onSelect: (DarkMode) -> Unit,
) {
    val options = listOf(
        DarkMode.SYSTEM to "Systemstandard",
        DarkMode.LIGHT to "Ljust läge",
        DarkMode.DARK to "Mörkt läge",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        options.forEachIndexed { index, (mode, label) ->
            val selected = current == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
