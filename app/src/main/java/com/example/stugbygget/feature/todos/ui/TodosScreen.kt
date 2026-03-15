package com.example.stugbygget.feature.todos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.TodoItem
import com.example.stugbygget.domain.model.TodoPriority
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarFilterChip
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarProgressRing
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.Sand
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.cardShadow
import com.example.stugbygget.ui.theme.staggeredFadeIn

private fun priorityColor(priority: TodoPriority) = when (priority) {
    TodoPriority.HIGH -> FaluRed
    TodoPriority.MEDIUM -> MidsummerGold
    TodoPriority.LOW -> MeadowGreen
}

@Composable
fun TodosScreen(container: AppContainer) {
    val viewModel: TodosViewModel = viewModel(factory = TodosViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(color = MeadowGreen)
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

        else -> TodosContent(
            uiState = uiState,
            onAssigneeSelected = viewModel::onAssigneeFilterSelected,
            onTodoToggle = { id, checked -> viewModel.onTodoToggle(id, checked) },
        )
    }
}

@Composable
private fun TodosContent(
    uiState: TodosUiState,
    onAssigneeSelected: (String?) -> Unit,
    onTodoToggle: (String, Boolean) -> Unit,
) {
    val allTodos = uiState.todos
    val doneCount = allTodos.count { it.done }
    val total = allTodos.size.coerceAtLeast(1)

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Progress header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.meadowGreen,
                title = "Tasks done",
                modifier = Modifier.padding(bottom = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$doneCount/${allTodos.size}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = Fraunces,
                            color = Color.White,
                        ),
                    )
                    SommarProgressRing(
                        progress = doneCount.toFloat() / total,
                        size = 52.dp,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                }
            }
        }

        // ── Assignee filter chips ──
        item {
            val assignees = listOf(null) + uiState.availableAssignees
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                items(assignees) { assignee ->
                    SommarFilterChip(
                        text = if (assignee == null) "All" else assignee,
                        selected = uiState.selectedAssignee == assignee,
                        onClick = { onAssigneeSelected(assignee) },
                        activeColor = MeadowGreen,
                    )
                }
            }
        }

        // ── Empty state for filtered results ──
        if (allTodos.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "✅",
                    title = if (uiState.selectedAssignee != null) "No tasks for this person" else "No tasks yet",
                    text = if (uiState.selectedAssignee != null)
                        "Try selecting a different filter."
                    else
                        "Todos will appear here once added to the project.",
                    accentColor = MeadowGreen,
                )
            }
        }

        // ── Todo rows ──
        itemsIndexed(allTodos) { index, todo ->
            TodoRow(
                todo = todo,
                onToggle = { onTodoToggle(todo.id, !todo.done) },
                modifier = Modifier.staggeredFadeIn(index),
            )
        }
    }
}

@Composable
private fun TodoRow(
    todo: TodoItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .cardShadow()
            .clip(com.example.stugbygget.ui.theme.SommarShapes.card)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (todo.done) MeadowGreen.copy(alpha = 0.2f) else Border,
                com.example.stugbygget.ui.theme.SommarShapes.card,
            )
            .clickable(onClick = onToggle)
            .padding(14.dp),
    ) {
        // Custom rounded checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(StugbyggetShapes.extraSmall)
                .background(if (todo.done) MeadowGreen else Color.Transparent)
                .border(
                    2.dp,
                    if (todo.done) MeadowGreen else Border,
                    StugbyggetShapes.extraSmall,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (todo.done) {
                Text("✓", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (todo.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = todo.phaseId,
                    style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text("•", style = MonoStyles.dataSmall.copy(color = Sand))
                Text(
                    text = todo.assignee,
                    style = MonoStyles.dataSmall.copy(color = LakeBlue),
                )
            }
        }

        Spacer(Modifier.width(8.dp))
        SommarBadge(
            text = todo.priority.name,
            color = priorityColor(todo.priority),
            backgroundColor = priorityColor(todo.priority).copy(alpha = 0.08f),
            borderColor = priorityColor(todo.priority).copy(alpha = 0.2f),
        )
    }
}
