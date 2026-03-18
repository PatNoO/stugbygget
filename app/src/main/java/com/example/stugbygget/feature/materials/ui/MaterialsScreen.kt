package com.example.stugbygget.feature.materials.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarOutlineButton
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.CreamBackground
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.WoodWarm
import com.example.stugbygget.ui.theme.staggeredFadeIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    container: AppContainer,
    onMaterialClick: (String) -> Unit,
) {
    val viewModel: MaterialsViewModel = viewModel(factory = MaterialsViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

        else -> MaterialsContent(
            uiState = uiState,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onMaterialClick = onMaterialClick,
            onShowOwnedAddSheet = viewModel::onShowOwnedAddSheet,
            onDeleteOwned = viewModel::onDeleteOwnedMaterial,
        )
    }

    if (uiState.showOwnedAddSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissOwnedAddSheet,
            sheetState = sheetState,
            containerColor = CreamBackground,
        ) {
            AddOwnedMaterialSheet(
                uiState = uiState,
                onNameChanged = viewModel::onDraftOwnedNameChanged,
                onQuantityChanged = viewModel::onDraftOwnedQuantityChanged,
                onUnitChanged = viewModel::onDraftOwnedUnitChanged,
                onNotesChanged = viewModel::onDraftOwnedNotesChanged,
                onSubmit = viewModel::onSubmitOwnedMaterial,
                onDismiss = viewModel::onDismissOwnedAddSheet,
            )
        }
    }
}

@Composable
private fun AddOwnedMaterialSheet(
    uiState: MaterialsUiState,
    onNameChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onUnitChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
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
            text = stringResource(R.string.materials_owned_sheet_title),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fraunces),
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )

        OutlinedTextField(
            value = uiState.draftOwnedName,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.materials_field_name)) },
            singleLine = true,
            isError = uiState.ownedAddError != null && uiState.draftOwnedName.isBlank(),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = uiState.draftOwnedQuantity,
                onValueChange = onQuantityChanged,
                label = { Text(stringResource(R.string.materials_field_quantity)) },
                singleLine = true,
                isError = uiState.ownedAddError != null && uiState.draftOwnedQuantity.isBlank(),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = uiState.draftOwnedUnit,
                onValueChange = onUnitChanged,
                label = { Text(stringResource(R.string.materials_field_unit)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = uiState.draftOwnedNotes,
            onValueChange = onNotesChanged,
            label = { Text(stringResource(R.string.common_field_notes_optional)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.ownedAddError != null) {
            Text(
                text = uiState.ownedAddError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SommarOutlineButton(
                text = stringResource(R.string.common_cancel),
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
            if (uiState.isAddingOwned) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MeadowGreen, modifier = Modifier.size(28.dp))
                }
            } else {
                SommarButton(
                    text = stringResource(R.string.materials_button_add),
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MaterialsContent(
    uiState: MaterialsUiState,
    onSearchChanged: (String) -> Unit,
    onMaterialClick: (String) -> Unit,
    onShowOwnedAddSheet: () -> Unit,
    onDeleteOwned: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.faluRed,
                title = stringResource(R.string.materials_header_title),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.materials_header_subtitle, uiState.materials.size),
                    style = MonoStyles.dataSmall.copy(color = Color.White.copy(alpha = 0.8f)),
                )
            }
        }

        // ── Search field ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(StugbyggetShapes.small)
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, Border, StugbyggetShapes.small)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (uiState.searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.materials_search_hint),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
                BasicTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(FaluRed),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // ── Empty state ──
        if (uiState.filteredMaterials.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "🔍",
                    title = if (uiState.searchQuery.isNotBlank()) stringResource(R.string.materials_empty_no_results_title) else stringResource(R.string.materials_empty_title),
                    text = if (uiState.searchQuery.isNotBlank())
                        stringResource(R.string.materials_empty_no_results_message, uiState.searchQuery)
                    else
                        stringResource(R.string.materials_empty_message),
                    accentColor = FaluRed,
                )
            }
        }

        // ── Material list ──
        itemsIndexed(uiState.filteredMaterials) { index, material ->
            MaterialCard(
                material = material,
                onClick = { onMaterialClick(material.id) },
                modifier = Modifier.staggeredFadeIn(index),
            )
        }

        // ── At Home section ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SommarSectionTitle(text = stringResource(R.string.materials_at_home_title))
                SommarOutlineButton(
                    text = stringResource(R.string.materials_add_button),
                    onClick = onShowOwnedAddSheet,
                )
            }
        }

        if (uiState.ownedMaterials.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "📦",
                    title = stringResource(R.string.materials_owned_empty_title),
                    text = stringResource(R.string.materials_owned_empty_message),
                    accentColor = MeadowGreen,
                )
            }
        }

        itemsIndexed(uiState.ownedMaterials) { index, owned ->
            OwnedMaterialRow(
                owned = owned,
                onDelete = { onDeleteOwned(owned.id) },
                modifier = Modifier.staggeredFadeIn(index),
            )
        }
    }
}

@Composable
private fun OwnedMaterialRow(
    owned: OwnedMaterial,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SommarCard(
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = owned.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    val qtyLabel = if (owned.unit.isBlank()) {
                        "${owned.quantity}"
                    } else {
                        "${owned.quantity} ${owned.unit}"
                    }
                    SommarBadge(
                        text = qtyLabel,
                        color = MeadowGreen,
                        backgroundColor = MeadowGreen.copy(alpha = 0.08f),
                        borderColor = MeadowGreen.copy(alpha = 0.2f),
                    )
                    if (owned.notes.isNotBlank()) {
                        Text(
                            text = owned.notes,
                            style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "✕",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .clip(StugbyggetShapes.extraSmall)
                    .clickable(onClick = onDelete)
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun MaterialCard(
    material: MaterialSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SommarCard(
        modifier = modifier
            .padding(bottom = 10.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    SommarBadge(
                        text = material.category.name,
                        color = categoryColor(material.category),
                        backgroundColor = categoryColor(material.category).copy(alpha = 0.08f),
                        borderColor = categoryColor(material.category).copy(alpha = 0.2f),
                    )
                    Text(
                        text = "${material.coveragePerUnit} ${material.unitType.name.lowercase()} / unit",
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }
}

internal fun categoryColor(category: MaterialCategory): androidx.compose.ui.graphics.Color = when (category) {
    MaterialCategory.PAINT -> FaluRed
    MaterialCategory.WOOD -> WoodWarm
    MaterialCategory.INSULATION -> MidsummerGold
    MaterialCategory.TILE -> LakeBlue
}
