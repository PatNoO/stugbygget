package com.example.stugbygget.feature.planning.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.stugbygget.ui.components.SommarTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter
    .ofPattern("MMM d", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

private val datePickerFormatter = DateTimeFormatter
    .ofPattern("d MMM yyyy", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

private fun formatPickedDate(millis: Long?): String? =
    millis?.let { datePickerFormatter.format(Instant.ofEpochMilli(it)) }

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
            title = { Text(stringResource(R.string.planning_dialog_delete_title)) },
            text = { Text(stringResource(R.string.planning_dialog_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDelete, enabled = !uiState.isDeleting) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onCancelDelete) { Text(stringResource(R.string.common_cancel)) }
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
                onDescriptionChanged = viewModel::onDraftDescriptionChanged,
                onStartMillisSelected = viewModel::onDraftStartMillisChanged,
                onEndMillisSelected = viewModel::onDraftEndMillisChanged,
                onSubmit = viewModel::onSubmitPhase,
                onDismiss = viewModel::onDismissAddSheet,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhaseSheet(
    uiState: PlanningUiState,
    onNameChanged: (String) -> Unit,
    onRoomChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onStartMillisSelected: (Long) -> Unit,
    onEndMillisSelected: (Long) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (uiState.editingPhase != null) stringResource(R.string.planning_sheet_title_edit) else stringResource(R.string.planning_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fraunces),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )

        SommarTextField(
            value = uiState.draftName,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.planning_field_name)) },
            singleLine = true,
            isError = uiState.addError != null && uiState.draftName.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        SommarTextField(
            value = uiState.draftRoom,
            onValueChange = onRoomChanged,
            label = { Text(stringResource(R.string.planning_field_room)) },
            singleLine = true,
            isError = uiState.addError != null && uiState.draftRoom.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        // Description — expands on focus, same pattern as Todos
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            SommarTextField(
                value = uiState.draftDescription,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(R.string.planning_field_description)) },
                singleLine = !isDescriptionExpanded,
                minLines = if (isDescriptionExpanded) 4 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) isDescriptionExpanded = true },
            )
            if (isDescriptionExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(R.string.common_done),
                        style = MaterialTheme.typography.labelMedium.copy(color = FaluRed),
                        modifier = Modifier
                            .clickable {
                                isDescriptionExpanded = false
                                focusManager.clearFocus()
                            }
                            .padding(4.dp),
                    )
                }
            }
        }

        // Date pickers
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Start date
            Box(modifier = Modifier.weight(1f)) {
                SommarTextField(
                    value = formatPickedDate(uiState.draftStartMillis) ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.planning_field_start_date)) },
                    readOnly = true,
                    singleLine = true,
                    isError = uiState.addError != null && uiState.draftStartMillis == null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showStartPicker = true },
                )
            }
            // End date
            Box(modifier = Modifier.weight(1f)) {
                SommarTextField(
                    value = formatPickedDate(uiState.draftEndMillis) ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.planning_field_end_date)) },
                    readOnly = true,
                    singleLine = true,
                    isError = uiState.addError != null && uiState.draftEndMillis == null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showEndPicker = true },
                )
            }
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
                    text = if (uiState.editingPhase != null) stringResource(R.string.common_save_changes) else stringResource(R.string.planning_button_add),
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // ── Start date picker dialog ──
    if (showStartPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.draftStartMillis)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onStartMillisSelected(it) }
                    showStartPicker = false
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    // ── End date picker dialog ──
    if (showEndPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.draftEndMillis)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onEndMillisSelected(it) }
                    showEndPicker = false
                }) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
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
                    text = stringResource(R.string.common_edit),
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                )
                SommarOutlineButton(
                    text = stringResource(R.string.common_delete),
                    onClick = onDelete,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
