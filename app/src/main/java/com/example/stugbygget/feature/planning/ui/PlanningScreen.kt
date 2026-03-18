package com.example.stugbygget.feature.planning.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.RenovationPhase
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarOutlineButton
import com.example.stugbygget.ui.components.SommarPhotoPicker
import com.example.stugbygget.ui.components.SommarProgressBar
import com.example.stugbygget.ui.components.SommarProgressRing
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.components.SommarStatCard
import com.example.stugbygget.ui.components.SommarTimelineIcon
import com.example.stugbygget.ui.theme.CreamBackground
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MonoStyles
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(container: AppContainer) {
    val viewModel: PlanningViewModel = viewModel(
        factory = PlanningViewModelFactory(container)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
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
                        title = stringResource(R.string.common_error_title),
                        text = uiState.errorMessage ?: stringResource(R.string.common_error_default),
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
                        title = stringResource(R.string.planning_empty_title),
                        text = stringResource(R.string.planning_empty_message),
                    )
                }
            }

            else -> PlanningContent(
            uiState = uiState,
            onEditPhase = viewModel::onShowEditSheet,
            onDeletePhase = viewModel::onRequestDelete,
        )
        }

        // FAB
        SommarButton(
            text = stringResource(R.string.planning_fab),
            onClick = viewModel::onShowAddSheet,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (uiState.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::onCancelDelete,
            title = { Text("Delete Phase") },
            text = { Text("This will permanently delete the phase. Continue?") },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDelete, enabled = !uiState.isDeleting) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDelete) { Text("Cancel") }
            },
        )
    }

    if (uiState.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissAddSheet,
            sheetState = sheetState,
            containerColor = CreamBackground,
        ) {
            AddPhaseSheet(
                uiState = uiState,
                onNameChanged = viewModel::onDraftNameChanged,
                onRoomChanged = viewModel::onDraftRoomChanged,
                onStartDateChanged = viewModel::onDraftStartDateChanged,
                onEndDateChanged = viewModel::onDraftEndDateChanged,
                onColorChanged = viewModel::onDraftColorChanged,
                onIconChanged = viewModel::onDraftIconChanged,
                onSubmit = viewModel::onSubmitPhase,
                onDismiss = viewModel::onDismissAddSheet,
            )
        }
    }
}

@Composable
private fun AddPhaseSheet(
    uiState: PlanningUiState,
    onNameChanged: (String) -> Unit,
    onRoomChanged: (String) -> Unit,
    onStartDateChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onColorChanged: (String) -> Unit,
    onIconChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (uiState.editingPhase != null) "Edit Phase" else stringResource(R.string.planning_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fraunces),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )

        OutlinedTextField(
            value = uiState.draftName,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.planning_field_name)) },
            singleLine = true,
            isError = uiState.addError != null && uiState.draftName.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.draftRoom,
            onValueChange = onRoomChanged,
            label = { Text(stringResource(R.string.planning_field_room)) },
            singleLine = true,
            isError = uiState.addError != null && uiState.draftRoom.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = uiState.draftStartDate,
                onValueChange = onStartDateChanged,
                label = { Text(stringResource(R.string.planning_field_start_date)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = uiState.draftEndDate,
                onValueChange = onEndDateChanged,
                label = { Text(stringResource(R.string.planning_field_end_date)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = uiState.draftColor,
                onValueChange = onColorChanged,
                label = { Text(stringResource(R.string.planning_field_color)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = uiState.draftIcon,
                onValueChange = onIconChanged,
                label = { Text(stringResource(R.string.planning_field_icon)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        // Photos
        SommarPhotoPicker()

        if (uiState.addError != null) {
            Text(
                text = uiState.addError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SommarOutlineButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            if (uiState.isAddingPhase) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FaluRed, modifier = Modifier.size(28.dp))
                }
            } else {
                SommarButton(
                    text = if (uiState.editingPhase != null) "Save Changes" else stringResource(R.string.planning_button_add),
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PlanningContent(
    uiState: PlanningUiState,
    onEditPhase: (RenovationPhase) -> Unit,
    onDeletePhase: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Summary stats ──
        item {
            SommarSectionTitle(
                text = stringResource(R.string.planning_section_overview),
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
                            text = stringResource(R.string.planning_stat_complete),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                SommarStatCard(
                    value = "${uiState.phases.size}",
                    label = stringResource(R.string.planning_stat_phases),
                    color = FaluRed,
                    modifier = Modifier.weight(1f),
                )
                SommarStatCard(
                    value = if (uiState.isSchedulePassed) "—" else "${uiState.daysLeft}",
                    label = if (uiState.isSchedulePassed) stringResource(R.string.planning_stat_overdue) else stringResource(R.string.planning_stat_days_left),
                    color = if (uiState.isSchedulePassed) MaterialTheme.colorScheme.error else MeadowGreen,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Phase timeline ──
        item {
            SommarSectionTitle(
                text = stringResource(R.string.planning_section_timeline),
                modifier = Modifier.fadeUpIn(delay = 100),
            )
        }

        itemsIndexed(uiState.phases) { index, phase ->
            PhaseRow(
                phase = phase,
                index = index,
                isLast = index == uiState.phases.lastIndex,
                onEdit = { onEditPhase(phase) },
                onDelete = { onDeletePhase(phase.id) },
            )
        }
    }
}

@Composable
private fun PhaseRow(
    phase: RenovationPhase,
    index: Int,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            SommarProgressBar(
                progress = phase.progress / 100f,
                color = if (isComplete) MeadowGreen else color,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SommarOutlineButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                )
                SommarOutlineButton(
                    text = "Delete",
                    onClick = onDelete,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
