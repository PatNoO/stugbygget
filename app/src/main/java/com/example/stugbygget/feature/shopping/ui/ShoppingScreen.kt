package com.example.stugbygget.feature.shopping.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarOutlineButton
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.staggeredFadeIn

@Composable
fun ShoppingScreen(container: AppContainer) {
    val viewModel: ShoppingViewModel = viewModel(factory = ShoppingViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { CircularProgressIndicator(color = MeadowGreen) }
        return
    }

    val expandedLists = remember { mutableStateMapOf<String, Boolean>() }
    val grouped = uiState.shoppingLists.groupBy { it.phaseId }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.meadowGreen,
                title = stringResource(R.string.shopping_header_title),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                val totalCount = uiState.shoppingLists.sumOf { it.items.size }
                val purchasedCount = uiState.shoppingLists.sumOf { list -> list.items.count { it.purchased } }
                Text(
                    text = stringResource(R.string.shopping_header_subtitle, purchasedCount, totalCount),
                    style = MonoStyles.dataSmall.copy(color = Color.White.copy(alpha = 0.8f)),
                )
            }
        }

        // ── Create list form ──
        item {
            SommarCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = stringResource(R.string.shopping_new_list_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(10.dp))
                StyledInput(
                    value = uiState.listNameInput,
                    onValueChange = viewModel::onListNameChanged,
                    placeholder = stringResource(R.string.shopping_field_list_name),
                )
                Spacer(Modifier.height(8.dp))
                StyledInput(
                    value = uiState.phaseInput,
                    onValueChange = viewModel::onPhaseChanged,
                    placeholder = stringResource(R.string.shopping_field_phase_id),
                )
                Spacer(Modifier.height(10.dp))
                SommarButton(
                    text = if (uiState.isSubmitting) stringResource(R.string.shopping_button_creating) else stringResource(R.string.shopping_button_create),
                    onClick = viewModel::onCreateList,
                    enabled = !uiState.isSubmitting,
                )
                uiState.errorMessage?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error,
                        ),
                    )
                }
            }
        }

        // ── Empty state ──
        if (uiState.shoppingLists.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "🛒",
                    title = stringResource(R.string.shopping_empty_title),
                    text = stringResource(R.string.shopping_empty_message),
                    accentColor = MeadowGreen,
                )
            }
            return@LazyColumn
        }

        // ── Lists grouped by phase ──
        grouped.entries.forEachIndexed { groupIndex, (phase, lists) ->
            item {
                SommarSectionTitle(
                    text = "Phase: $phase",
                    modifier = Modifier.padding(top = if (groupIndex == 0) 4.dp else 16.dp),
                )
            }
            itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
                val isExpanded = expandedLists[list.id] ?: true
                ShoppingListCard(
                    list = list,
                    isExpanded = isExpanded,
                    draft = uiState.itemDrafts[list.id] ?: ShoppingItemDraftUiState(),
                    isSubmitting = uiState.isSubmitting,
                    onToggleExpand = { expandedLists[list.id] = !isExpanded },
                    onItemNameChanged = { v -> viewModel.onItemNameChanged(list.id, v) },
                    onItemQuantityChanged = { v -> viewModel.onItemQuantityChanged(list.id, v) },
                    onItemUnitChanged = { v -> viewModel.onItemUnitChanged(list.id, v) },
                    onAddItem = { viewModel.onAddItem(list.id) },
                    onTogglePurchased = { itemId, checked ->
                        viewModel.onTogglePurchased(list.id, itemId, checked)
                    },
                    modifier = Modifier.staggeredFadeIn(index),
                )
            }
        }
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingList,
    isExpanded: Boolean,
    draft: ShoppingItemDraftUiState,
    isSubmitting: Boolean,
    onToggleExpand: () -> Unit,
    onItemNameChanged: (String) -> Unit,
    onItemQuantityChanged: (String) -> Unit,
    onItemUnitChanged: (String) -> Unit,
    onAddItem: () -> Unit,
    onTogglePurchased: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val purchasedCount = list.items.count { it.purchased }

    SommarCard(modifier = modifier.padding(bottom = 10.dp)) {
        // ── List header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "$purchasedCount / ${list.items.size} items · ${list.totalEstimate.toInt()} SEK est.",
                    style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            Text(
                text = if (isExpanded) "▲" else "▼",
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }

        if (!isExpanded) return@SommarCard

        Spacer(Modifier.height(12.dp))

        // ── Items ──
        if (list.items.isEmpty()) {
            Text(
                text = stringResource(R.string.shopping_list_empty),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        } else {
            list.items.forEach { item ->
                ShoppingItemRow(
                    item = item,
                    onToggle = { checked -> onTogglePurchased(item.id, checked) },
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Price comparison ──
        // TODO: wire CompareShoppingPricesUseCase to show per-store totals
        if (list.totalEstimate > 0.0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(StugbyggetShapes.extraSmall)
                    .background(MeadowGreen.copy(alpha = 0.06f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.shopping_estimated_total),
                    style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Text(
                    text = "${list.totalEstimate.toInt()} SEK",
                    style = MonoStyles.data.copy(color = MeadowGreen),
                )
            }
            Spacer(Modifier.height(10.dp))
        }

        // ── Add item form ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                StyledInput(
                    value = draft.name,
                    onValueChange = onItemNameChanged,
                    placeholder = stringResource(R.string.shopping_field_item_name),
                )
            }
            Box(modifier = Modifier.width(64.dp)) {
                StyledInput(
                    value = draft.quantity,
                    onValueChange = onItemQuantityChanged,
                    placeholder = stringResource(R.string.shopping_field_qty),
                )
            }
            Box(modifier = Modifier.width(58.dp)) {
                StyledInput(
                    value = draft.unit,
                    onValueChange = onItemUnitChanged,
                    placeholder = stringResource(R.string.shopping_field_unit),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        SommarOutlineButton(
            text = if (isSubmitting) stringResource(R.string.shopping_button_adding) else stringResource(R.string.shopping_button_add_item),
            onClick = onAddItem,
            color = MeadowGreen,
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Custom checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(StugbyggetShapes.extraSmall)
                .background(if (item.purchased) MeadowGreen else Color.Transparent)
                .border(2.dp, if (item.purchased) MeadowGreen else Border, StugbyggetShapes.extraSmall)
                .clickable { onToggle(!item.purchased) },
            contentAlignment = Alignment.Center,
        ) {
            if (item.purchased) {
                Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (item.purchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.purchased) TextDecoration.LineThrough else null,
                ),
            )
            Text(
                text = "${item.quantity} ${item.unit}${item.purchasedPrice?.let { " · ${it.toInt()}${stringResource(R.string.shopping_price_paid_suffix)}" } ?: ""}",
                style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
        if (item.purchased) {
            SommarBadge(
                text = "✓",
                color = MeadowGreen,
                backgroundColor = MeadowGreen.copy(alpha = 0.08f),
                borderColor = MeadowGreen.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun StyledInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(StugbyggetShapes.extraSmall)
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, Border, StugbyggetShapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MeadowGreen),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
