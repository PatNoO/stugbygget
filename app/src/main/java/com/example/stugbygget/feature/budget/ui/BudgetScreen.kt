package com.example.stugbygget.feature.budget.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stugbygget.R
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.CategoryBudget
import com.example.stugbygget.domain.model.PhaseBudget
import com.example.stugbygget.ui.components.SommarBadge
import com.example.stugbygget.ui.components.SommarCard
import com.example.stugbygget.ui.components.SommarHeaderCard
import com.example.stugbygget.ui.components.SommarInfoBox
import com.example.stugbygget.ui.components.SommarProgressBar
import com.example.stugbygget.ui.components.SommarSectionTitle
import com.example.stugbygget.ui.theme.FaluRed
import com.example.stugbygget.ui.theme.LakeBlue
import com.example.stugbygget.ui.theme.MeadowGreen
import com.example.stugbygget.ui.theme.MidsummerGold
import com.example.stugbygget.ui.theme.MonoStyles
import com.example.stugbygget.ui.theme.SommarGradients
import com.example.stugbygget.ui.theme.WoodWarm
import com.example.stugbygget.ui.theme.staggeredFadeIn

@Composable
fun BudgetScreen(container: AppContainer) {
    val viewModel: BudgetViewModel = viewModel(factory = BudgetViewModelFactory(container))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) { CircularProgressIndicator(color = MidsummerGold) }
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
                title = stringResource(R.string.budget_error_title),
                text = uiState.errorMessage ?: stringResource(R.string.common_error_default),
                accentColor = FaluRed,
            )
        }
        return
    }

    if (uiState.overview == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SommarInfoBox(
                emoji = "💰",
                title = stringResource(R.string.budget_empty_title),
                text = stringResource(R.string.budget_empty_message),
                accentColor = MidsummerGold,
            )
        }
        return
    }

    val overview = uiState.overview!!
    val totalProgress = if (overview.totalBudget <= 0.0) 0f
    else (overview.totalSpent / overview.totalBudget).toFloat().coerceIn(0f, 1f)
    val isOverBudget = overview.totalSpent > overview.totalBudget

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Header ──
        item {
            SommarHeaderCard(
                gradient = SommarGradients.midsummerGold,
                title = stringResource(R.string.budget_header_title),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.budget_header_subtitle, "%.0f".format(overview.totalSpent) + " / " + "%.0f".format(overview.totalBudget)),
                    style = MonoStyles.dataSmall.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f)),
                )
            }
        }

        // ── Overview card ──
        item {
            SommarCard(modifier = Modifier.padding(bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.budget_total_label), style = MaterialTheme.typography.titleSmall)
                    if (isOverBudget) {
                        SommarBadge(
                            text = stringResource(R.string.budget_badge_over),
                            color = FaluRed,
                            backgroundColor = FaluRed.copy(alpha = 0.08f),
                            borderColor = FaluRed.copy(alpha = 0.2f),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                SommarProgressBar(
                    progress = totalProgress,
                    color = if (isOverBudget) FaluRed else MidsummerGold,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.budget_stat_spent) + "${overview.totalSpent.toInt()}" + stringResource(R.string.common_sek_suffix),
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                    Text(
                        text = stringResource(R.string.budget_stat_budget) + "${overview.totalBudget.toInt()}" + stringResource(R.string.common_sek_suffix),
                        style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.budget_stat_estimated) + "${overview.estimatedFinalCost.toInt()}" + stringResource(R.string.common_sek_suffix),
                    style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
        }

        // ── Phase breakdown ──
        item {
            SommarSectionTitle(
                text = stringResource(R.string.budget_section_phases),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (overview.phaseBudgets.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "📋",
                    title = stringResource(R.string.budget_phases_empty_title),
                    text = stringResource(R.string.budget_phases_empty_message),
                    accentColor = MidsummerGold,
                )
            }
        } else {
            itemsIndexed(overview.phaseBudgets, key = { _, p -> p.phaseId }) { index, phase ->
                PhaseBudgetCard(
                    phase = phase,
                    isOverspent = uiState.overspentPhaseIds.contains(phase.phaseId),
                    modifier = Modifier.staggeredFadeIn(index),
                )
            }
        }

        // ── Category breakdown ──
        item {
            SommarSectionTitle(
                text = stringResource(R.string.budget_section_categories),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
        }

        if (overview.categoryBudgets.isEmpty()) {
            item {
                SommarInfoBox(
                    emoji = "🏷️",
                    title = stringResource(R.string.budget_categories_empty_title),
                    text = stringResource(R.string.budget_categories_empty_message),
                    accentColor = LakeBlue,
                )
            }
        } else {
            items(overview.categoryBudgets, key = { it.category }) { category ->
                CategoryBudgetRow(category = category)
            }
        }
    }
}

@Composable
private fun PhaseBudgetCard(
    phase: PhaseBudget,
    isOverspent: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress = if (phase.budgeted <= 0.0) 0f
    else (phase.spent / phase.budgeted).toFloat().coerceAtLeast(0f)
    val progressClamped = progress.coerceIn(0f, 1f)
    val barColor = when {
        isOverspent -> FaluRed
        progress > 0.8f -> MidsummerGold
        else -> MeadowGreen
    }

    SommarCard(modifier = modifier.padding(bottom = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(phase.phaseId, style = MaterialTheme.typography.titleSmall)
            if (isOverspent) {
                SommarBadge(
                    text = stringResource(R.string.budget_phase_badge_over),
                    color = FaluRed,
                    backgroundColor = FaluRed.copy(alpha = 0.08f),
                    borderColor = FaluRed.copy(alpha = 0.2f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        SommarProgressBar(
            progress = progressClamped,
            color = barColor,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.budget_stat_spent) + "${phase.spent.toInt()}" + stringResource(R.string.common_sek_suffix),
                style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Text(
                text = stringResource(R.string.budget_stat_budgeted) + "${phase.budgeted.toInt()}" + stringResource(R.string.common_sek_suffix),
                style = MonoStyles.dataSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }
}

@Composable
private fun CategoryBudgetRow(category: CategoryBudget) {
    val accentColor = when (category.category.uppercase()) {
        "MATERIALS" -> WoodWarm
        "CONTRACTORS" -> LakeBlue
        "TRANSPORT" -> MidsummerGold
        else -> MeadowGreen
    }
    SommarCard(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SommarBadge(
                text = category.category,
                color = accentColor,
                backgroundColor = accentColor.copy(alpha = 0.08f),
                borderColor = accentColor.copy(alpha = 0.2f),
            )
            Text(
                text = "${category.spent.toInt()}" + stringResource(R.string.common_sek_suffix),
                style = MonoStyles.data.copy(color = accentColor),
            )
        }
    }
}
