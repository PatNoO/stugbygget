package com.example.stugbygget.feature.logistics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.domain.model.LogisticsAssumptions
import com.example.stugbygget.domain.model.LogisticsInput
import com.example.stugbygget.domain.usecase.CalculateLogisticsRecommendationUseCase
import com.example.stugbygget.domain.repository.ProjectSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Default Swedish building-supply assumptions used until SB71 wires live data. */
private val defaultAssumptions = LogisticsAssumptions(
    fuelCostPerKm = 2.50,         // ~2.50 SEK/km for a loaded van
    trailerRentalCost = 350.0,    // typical Swedish trailer hire per day
    deliveryBaseFee = 499.0,      // common store delivery fee (e.g. Byggmax)
    deliveryFreeThreshold = 10_000.0, // free delivery above 10 000 SEK order
    freightRatePerKg = 4.50       // DHL Freight estimate per kg
)

/** Placeholder input used until live shopping-list weight/volume data is wired (SB71). */
private val placeholderInput = LogisticsInput(
    shoppingListId = "placeholder",
    distanceKm = 50.0,       // placeholder 50 km — live route in SB71
    totalWeightKg = 500.0,
    totalVolumeM3 = 2.0
)

class LogisticsViewModel(
    private val calculateLogisticsRecommendationUseCase: CalculateLogisticsRecommendationUseCase,
    private val projectSessionRepository: ProjectSessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    init {
        calculate()
    }

    private fun calculate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val projectId = projectSessionRepository.getProjectId()
                val result = calculateLogisticsRecommendationUseCase(
                    projectId = projectId,
                    input = placeholderInput,
                    assumptions = defaultAssumptions,
                    orderValue = 12_000.0  // placeholder order value
                )
                _uiState.update { it.copy(isLoading = false, calculation = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to calculate logistics.")
                }
            }
        }
    }

    fun onRetry() = calculate()
}

class LogisticsViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LogisticsViewModel(
            calculateLogisticsRecommendationUseCase = container.calculateLogisticsRecommendationUseCase,
            projectSessionRepository = container.projectSessionRepository,
        ) as T
}
