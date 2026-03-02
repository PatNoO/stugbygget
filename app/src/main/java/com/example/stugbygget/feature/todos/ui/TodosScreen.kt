package com.example.stugbygget.feature.todos.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer

@Composable
fun TodosScreen(container: AppContainer) {
    val viewModel: TodosViewModel = viewModel(factory = TodosViewModelFactory(container))
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
                Text("Fel", style = MaterialTheme.typography.titleLarge)
                Text(uiState.errorMessage ?: "")
            }
        }

        else -> {
            val doneCount = uiState.todos.count { it.done }
            val totalCount = uiState.todos.size.coerceAtLeast(1)
            val progress = doneCount.toFloat() / totalCount.toFloat()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Todo", style = MaterialTheme.typography.headlineSmall)
                        Text("$doneCount av ${uiState.todos.size} klara")
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Filtrera per fas")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedPhase == null,
                                    onClick = { viewModel.onPhaseFilterSelected(null) },
                                    label = { Text("Alla") }
                                )
                            }
                            items(uiState.availablePhases) { phase ->
                                FilterChip(
                                    selected = uiState.selectedPhase == phase,
                                    onClick = { viewModel.onPhaseFilterSelected(phase) },
                                    label = { Text(phase) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Filtrera per person")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedAssignee == null,
                                    onClick = { viewModel.onAssigneeFilterSelected(null) },
                                    label = { Text("Alla") }
                                )
                            }
                            items(uiState.availableAssignees) { assignee ->
                                FilterChip(
                                    selected = uiState.selectedAssignee == assignee,
                                    onClick = { viewModel.onAssigneeFilterSelected(assignee) },
                                    label = { Text(assignee) }
                                )
                            }
                        }
                    }
                }

                if (uiState.todos.isEmpty()) {
                    item {
                        Text(
                            text = "Inga uppgifter hittades",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                items(uiState.todos) { todo ->
                    val alpha by animateFloatAsState(if (todo.done) 0.45f else 1f, label = "todo-alpha")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = todo.done,
                            onCheckedChange = { checked -> viewModel.onTodoToggle(todo.id, checked) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(todo.text, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${todo.phaseId} · ${todo.assignee} · ${todo.priority.name}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
