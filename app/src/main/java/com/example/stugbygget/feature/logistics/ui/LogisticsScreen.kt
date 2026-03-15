package com.example.stugbygget.feature.logistics.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.stugbygget.domain.model.TransportOptionResult
import com.example.stugbygget.domain.model.TransportType
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarButton
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.SommarShapes
import com.example.stugbygget.ui.theme.TextLight
import com.example.stugbygget.ui.theme.WoodWarm
import com.example.stugbygget.ui.theme.staggeredFadeIn

@Composable
fun LogisticsScreen(container: AppContainer) {
    val viewModel: LogisticsViewModel = viewModel(factory = LogisticsViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { CircularProgressIndicator(color = WoodWarm) }
        return
    }

    if (uiState.errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SommarInfoBox(
                emoji = "⚠️",
                title = "Could not calculate logistics",
                text = uiState.errorMessage ?: "Unknown error",
                accentColor = WoodWarm,
            )
            Spacer(Modifier.height(16.dp))
            SommarButton(text = "Retry", onClick = viewModel::onRetry)
        }
        return
    }

    if (uiState.calculation == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SommarInfoBox(
                emoji = "🚛",
                title = "No logistics data yet",
                text = "Add items to a shopping list to see transport recommendations.",
                accentColor = WoodWarm,
            )
        }
        return
    }

    val calculation = uiState.calculation!!

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.woodWarm,
                title = "Logistics & Transport 🚛",
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(
                    text = "Distance: ~${calculation.distanceKm.toInt()} km · ${calculation.totalWeightKg.toInt()} kg",
                    style = MonoStyles.dataSmall.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)),
                )
            }
        }

        // ── Material summary ──
        item {
            SommarCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Material Summary", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SummaryStatColumn(label = "Total weight", value = "${calculation.totalWeightKg.toInt()} kg")
                    SummaryStatColumn(label = "Total volume", value = "${String.format("%.1f", calculation.totalVolumeM3)} m³")
                    SummaryStatColumn(label = "Distance", value = "~${calculation.distanceKm.toInt()} km")
                }
            }
        }

        // ── Transport options ──
        item {
            SommarSectionTitle(
                text = "Transport Options",
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        val allCostsEqual = calculation.options.map { it.totalCost }.distinct().size == 1

        itemsIndexed(calculation.options, key = { _, opt -> opt.type.name }) { index, option ->
            val isRecommended = !allCostsEqual && option.type == calculation.recommendedType
            TransportOptionCard(
                option = option,
                isRecommended = isRecommended,
                modifier = Modifier.staggeredFadeIn(index),
            )
        }

        // ── Delivery schedule placeholder ──
        item {
            SommarSectionTitle(
                text = "Delivery Schedule",
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        }
        item {
            SommarInfoBox(
                emoji = "📅",
                title = "Timeline sync coming in SB71",
                text = "Delivery dates will be linked to your renovation phases once Cloud Functions are integrated.",
                accentColor = LakeBlue,
            )
        }
    }
}

@Composable
private fun TransportOptionCard(
    option: TransportOptionResult,
    isRecommended: Boolean,
    modifier: Modifier = Modifier,
) {
    val (emoji, label) = when (option.type) {
        TransportType.SELF -> "🚗" to "Self-transport"
        TransportType.STORE_DELIVERY -> "🏪" to "Store delivery"
        TransportType.FREIGHT -> "📦" to "Freight carrier (DHL)"
    }

    val cardModifier = modifier
        .padding(bottom = 10.dp)
        .then(
            if (isRecommended)
                Modifier.border(2.dp, MeadowGreen, SommarShapes.card)
            else Modifier
        )

    SommarCard(modifier = cardModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.titleSmall,
            )
            if (isRecommended) {
                SommarBadge(
                    text = "✓ Recommended",
                    color = MeadowGreen,
                    backgroundColor = MeadowGreen.copy(alpha = 0.08f),
                    borderColor = MeadowGreen.copy(alpha = 0.2f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Cost: ${option.totalCost.toInt()} SEK",
                style = MonoStyles.data.copy(
                    color = if (isRecommended) MeadowGreen else MidsummerGold,
                ),
            )
            if (option.timeMinutes > 0) {
                Text(
                    text = "${option.timeMinutes} min",
                    style = MonoStyles.dataSmall.copy(color = TextLight),
                )
            }
        }
        if (option.notes.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = option.notes,
                style = MaterialTheme.typography.bodySmall.copy(color = TextLight),
            )
        }
    }
}

@Composable
private fun SummaryStatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MonoStyles.data.copy(color = WoodWarm))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextLight))
    }
}
