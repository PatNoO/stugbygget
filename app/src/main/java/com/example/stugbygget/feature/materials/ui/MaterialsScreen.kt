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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.MaterialCategory
import com.example.stugbygget.domain.model.MaterialSpec
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.TextDark
import com.example.stugbygget.ui.theme.TextLight
import com.example.stugbygget.ui.theme.WoodWarm
import com.example.stugbygget.ui.theme.staggeredFadeIn

@Composable
fun MaterialsScreen(
    container: AppContainer,
    onMaterialClick: (String) -> Unit,
) {
    val viewModel: MaterialsViewModel = viewModel(factory = MaterialsViewModelFactory(container))
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

        else -> MaterialsContent(
            uiState = uiState,
            onSearchChanged = viewModel::onSearchQueryChanged,
            onMaterialClick = onMaterialClick,
        )
    }
}

@Composable
private fun MaterialsContent(
    uiState: MaterialsUiState,
    onSearchChanged: (String) -> Unit,
    onMaterialClick: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.faluRed,
                title = "Materials & Prices 🔗",
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(
                    text = "${uiState.materials.size} materials",
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
                        text = "Search materials…",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                    )
                }
                BasicTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextDark),
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
                    title = if (uiState.searchQuery.isNotBlank()) "No results" else "No materials yet",
                    text = if (uiState.searchQuery.isNotBlank())
                        "No materials match \"${uiState.searchQuery}\"."
                    else
                        "Materials will appear here once added to the project.",
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
                        style = MonoStyles.dataSmall.copy(color = TextLight),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "→",
                style = MaterialTheme.typography.titleMedium.copy(color = TextLight),
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
