package com.example.stugbygget.feature.planning.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.phases) { phase ->
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = phase.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Rum: ${phase.room}")
                        Text(
                            text = "${formatter.format(phase.startDate.atZone(ZoneId.systemDefault()))} - " +
                                formatter.format(phase.endDate.atZone(ZoneId.systemDefault()))
                        )
                        Text(text = "Progress: ${phase.progress}%")
                    }
                }
            }
        }
    }
}
