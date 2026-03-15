package com.example.stugbygget.feature.planning.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarProgressBar
import com.example.stugbygget.ui.components.SommarProgressRing
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.components.SommarStatCard
import com.example.stugbygget.ui.components.SommarTimelineIcon
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.TextLight
import com.example.stugbygget.ui.theme.fadeUpIn
import com.example.stugbygget.ui.theme.staggeredFadeIn
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter
    .ofPattern("MMM d", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

private fun phaseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    FaluRed
}

@Composable
fun PlanningScreen(container: AppContainer) {
    val viewModel: PlanningViewModel = viewModel(
        factory = PlanningViewModelFactory(container)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = FaluRed)
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                SommarInfoBox(
                    emoji = "⚠️",
                    title = "Error",
                    text = uiState.errorMessage ?: "Something went wrong.",
                    accentColor = MaterialTheme.colorScheme.error,
                )
            }
        }

        uiState.phases.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                SommarInfoBox(
                    emoji = "📋",
                    title = "No phases yet",
                    text = "Renovation phases will appear here once added to the project.",
                )
            }
        }

        else -> PlanningContent(uiState)
    }
}

@Composable
private fun PlanningContent(uiState: PlanningUiState) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Summary stats ──
        item {
            SommarSectionTitle(
                text = "Overview",
                modifier = Modifier.fadeUpIn(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .fadeUpIn(delay = 50),
            ) {
                SommarCard(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SommarProgressRing(
                            progress = uiState.totalProgressPercent / 100f,
                            color = FaluRed,
                            label = "${uiState.totalProgressPercent}%",
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight,
                        )
                    }
                }
                SommarStatCard(
                    value = "${uiState.phases.size}",
                    label = "phases",
                    color = FaluRed,
                    modifier = Modifier.weight(1f),
                )
                SommarStatCard(
                    value = if (uiState.isSchedulePassed) "—" else "${uiState.daysLeft}",
                    label = if (uiState.isSchedulePassed) "overdue" else "days left",
                    color = if (uiState.isSchedulePassed) MaterialTheme.colorScheme.error else MeadowGreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Phase timeline ──
        item {
            SommarSectionTitle(
                text = "Timeline",
                modifier = Modifier.fadeUpIn(delay = 100),
            )
        }

        itemsIndexed(uiState.phases) { index, phase ->
            PhaseRow(
                phase = phase,
                index = index,
                isLast = index == uiState.phases.lastIndex,
            )
        }
    }
}

@Composable
private fun PhaseRow(
    phase: RenovationPhase,
    index: Int,
    isLast: Boolean,
) {
    val color = phaseColor(phase.color)
    val isComplete = phase.progress >= 100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .staggeredFadeIn(index),
    ) {
        SommarTimelineIcon(
            icon = phase.icon,
            color = color,
            isComplete = isComplete,
            showLine = !isLast,
        )
        Spacer(Modifier.width(14.dp))
        SommarCard(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = phase.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fraunces),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${phase.progress}%",
                    style = MonoStyles.data.copy(
                        color = if (isComplete) MeadowGreen else color,
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SommarBadge(text = phase.room)
                SommarBadge(
                    text = "${dateFormatter.format(phase.startDate)} — ${dateFormatter.format(phase.endDate)}",
                    color = TextLight,
                )
            }
            Spacer(Modifier.height(10.dp))
            SommarProgressBar(
                progress = phase.progress / 100f,
                color = if (isComplete) MeadowGreen else color,
            )
        }
    }
}
