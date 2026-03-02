package com.example.stugbygget.feature.budget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun BudgetScreen(container: AppContainer) {
    val viewModel: BudgetViewModel = viewModel(factory = BudgetViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Budget Error", style = MaterialTheme.typography.titleLarge)
                Text(uiState.errorMessage ?: "")
            }
        }

        uiState.overview == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("No budget data yet.") }
        }

        else -> {
            val overview = uiState.overview!!
            val totalProgress = if (overview.totalBudget <= 0.0) 0f
            else (overview.totalSpent / overview.totalBudget).toFloat().coerceIn(0f, 1f)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Budget Dashboard", style = MaterialTheme.typography.headlineSmall)
                    Text("Track total budget, phase spend, and projected final cost.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Budget: ${overview.totalBudget.toInt()} SEK")
                            Text("Spent: ${overview.totalSpent.toInt()} SEK")
                            Text("Estimated Final: ${overview.estimatedFinalCost.toInt()} SEK")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { totalProgress }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                item { Text("Phase Breakdown", style = MaterialTheme.typography.titleMedium) }
                items(overview.phaseBudgets, key = { it.phaseId }) { phase ->
                    val progress = if (phase.budgeted <= 0.0) 0f
                    else (phase.spent / phase.budgeted).toFloat().coerceAtLeast(0f)
                    val warning = uiState.overspentPhaseIds.contains(phase.phaseId)

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (warning) Color(0xFFFFF1F0) else Color.Transparent)
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(phase.phaseId)
                                Text("${phase.spent.toInt()} / ${phase.budgeted.toInt()} SEK")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (warning) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Warning: phase is over budget.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item { Text("Category Spend", style = MaterialTheme.typography.titleMedium) }
                items(overview.categoryBudgets, key = { it.category }) { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category.category)
                        Text("${category.spent.toInt()} SEK")
                    }
                }
            }
        }
    }
}
