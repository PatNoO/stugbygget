package com.example.stugbygget.feature.planning.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

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
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Fel", style = MaterialTheme.typography.headlineSmall)
                Text(text = uiState.errorMessage ?: "")
            }
        }

        uiState.phases.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Inga faser ännu")
            }
        }

        else -> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val totalProgress = uiState.phases.map { it.progress }.average().roundToInt()
            val maxEndDate = uiState.phases.maxOfOrNull { it.endDate } ?: Instant.now()
            val daysLeft = (maxEndDate.epochSecond - Instant.now().epochSecond) / 86_400L
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Total progress: $totalProgress%",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (daysLeft > 0) "$daysLeft dagar kvar" else "Fasplanen är passerad",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider()
                }
                items(uiState.phases) { phase ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            progress = { phase.progress / 100f },
                            modifier = Modifier.size(42.dp),
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(text = phase.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Rum: ${phase.room}")
                            Text(
                                text = "${formatter.format(phase.startDate.atZone(ZoneId.systemDefault()))} - " +
                                    formatter.format(phase.endDate.atZone(ZoneId.systemDefault()))
                            )
                            Text(text = "Progress: ${phase.progress}%")
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
