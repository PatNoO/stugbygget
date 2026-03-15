package com.example.stugbygget.feature.materials.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.stugbygget.domain.model.PriceQuote
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.components.SommarStatCard
import com.example.stugbygget.ui.theme.Border
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.Fraunces
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.StugbyggetShapes
import com.example.stugbygget.ui.theme.staggeredFadeIn

@Composable
fun MaterialDetailScreen(
    container: AppContainer,
    materialId: String,
) {
    val viewModel: MaterialDetailViewModel = viewModel(
        key = materialId,
        factory = MaterialDetailViewModelFactory(container, materialId),
    )
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

        uiState.material == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                SommarInfoBox(
                    emoji = "🔍",
                    title = "Material not found",
                    text = "This material could not be loaded.",
                    accentColor = FaluRed,
                )
            }
        }

        else -> MaterialDetailContent(
            uiState = uiState,
            onAreaInputChanged = viewModel::onAreaInputChanged,
        )
    }
}

@Composable
private fun MaterialDetailContent(
    uiState: MaterialDetailUiState,
    onAreaInputChanged: (String) -> Unit,
) {
    val material = uiState.material ?: return
    val accentColor = categoryColor(material.category)

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.faluRed,
                title = material.name,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                SommarBadge(
                    text = material.category.name,
                    color = Color.White,
                    backgroundColor = Color.White.copy(alpha = 0.15f),
                    borderColor = Color.White.copy(alpha = 0.3f),
                )
            }
        }

        // ── Spec stats ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
            ) {
                SommarStatCard(
                    value = "${material.coveragePerUnit}",
                    label = "${material.unitType.name.lowercase()} / unit",
                    color = accentColor,
                    modifier = Modifier.weight(1f),
                )
                SommarStatCard(
                    value = "${(material.wasteMargin * 100).toInt()}%",
                    label = "waste margin",
                    color = MidsummerGold,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Quantity calculator ──
        item {
            SommarSectionTitle(text = "Quantity calculator")
            SommarCard(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "Enter area (m²)",
                    style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(StugbyggetShapes.small)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, Border, StugbyggetShapes.small)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    if (uiState.areaInput.isEmpty()) {
                        Text(
                            text = "e.g. 48",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        )
                    }
                    BasicTextField(
                        value = uiState.areaInput,
                        onValueChange = onAreaInputChanged,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(FaluRed),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (uiState.calculatedUnits != null) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Units needed",
                            style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        )
                        Text(
                            text = "${uiState.calculatedUnits} ${material.unitType.name.lowercase()}",
                            style = MonoStyles.measurementMedium.copy(color = accentColor),
                        )
                    }
                    Text(
                        text = "Formula: ⌈(${uiState.areaInput} / ${material.coveragePerUnit}) × ${1.0 + material.wasteMargin}⌉",
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        // ── Price comparison ──
        item {
            SommarSectionTitle(text = "Price comparison")
        }

        if (uiState.priceQuotes.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "💰",
                    title = "No prices yet",
                    text = "Price data will appear here once added to the project.",
                    accentColor = MidsummerGold,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        } else {
            itemsIndexed(uiState.priceQuotes.sortedBy { it.unitPrice }) { index, quote ->
                PriceQuoteCard(
                    quote = quote,
                    isCheapest = index == 0,
                    modifier = Modifier.staggeredFadeIn(index),
                )
            }
        }

        // ── Show on map ──
        item {
            SommarButton(
                text = "Show on map",
                onClick = { /* TODO: connect to Google Maps */ },
                gradient = SommarGradients.lakeBlue,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun PriceQuoteCard(
    quote: PriceQuote,
    isCheapest: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        isCheapest -> MeadowGreen.copy(alpha = 0.4f)
        quote.store.contains("WALL", ignoreCase = true) -> LakeBlue.copy(alpha = 0.3f)
        else -> MidsummerGold.copy(alpha = 0.3f)
    }

    SommarCard(modifier = modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Stock indicator dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(StugbyggetShapes.extraSmall)
                    .background(if (quote.inStock) MeadowGreen else FaluRed),
            )
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.store,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = if (quote.inStock) "In stock" else "Out of stock",
                    style = MonoStyles.dataSmall.copy(
                        color = if (quote.inStock) MeadowGreen else FaluRed,
                    ),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f kr".format(quote.unitPrice),
                    style = MonoStyles.measurementMedium.copy(color = MidsummerGold),
                )
                if (isCheapest) {
                    SommarBadge(
                        text = "Best price",
                        color = MeadowGreen,
                        backgroundColor = MeadowGreen.copy(alpha = 0.08f),
                        borderColor = MeadowGreen.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}
