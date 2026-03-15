package com.example.stugbygget.feature.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.CalculateMaterialQuantityUseCase
import com.example.stugbygget.domain.usecase.ObserveMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObservePriceQuotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MaterialsViewModel(
    private val observeMaterialsUseCase: ObserveMaterialsUseCase,
    private val projectId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialsUiState(isLoading = true))
    val uiState: StateFlow<MaterialsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeMaterialsUseCase(projectId)
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load materials.")
                    }
                }
                .collect { materials ->
                    _uiState.update { it.copy(isLoading = false, materials = materials, errorMessage = null) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

class MaterialDetailViewModel(
    private val observeMaterialsUseCase: ObserveMaterialsUseCase,
    private val observePriceQuotesUseCase: ObservePriceQuotesUseCase,
    private val calculateMaterialQuantityUseCase: CalculateMaterialQuantityUseCase,
    private val projectId: String,
    private val materialId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialDetailUiState(isLoading = true))
    val uiState: StateFlow<MaterialDetailUiState> = _uiState.asStateFlow()

    init {
        loadMaterial()
        loadPriceQuotes()
    }

    private fun loadMaterial() {
        viewModelScope.launch {
            observeMaterialsUseCase(projectId)
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
                .collect { materials ->
                    val material = materials.firstOrNull { it.id == materialId }
                    _uiState.update { it.copy(isLoading = false, material = material) }
                }
        }
    }

    private fun loadPriceQuotes() {
        viewModelScope.launch {
            observePriceQuotesUseCase(projectId, materialId)
                .catch { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
                .collect { quotes ->
                    _uiState.update { it.copy(priceQuotes = quotes) }
                    recalculate()
                }
        }
    }

    fun onAreaInputChanged(value: String) {
        _uiState.update { it.copy(areaInput = value) }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val material = state.material ?: return
        val area = state.areaInput.toDoubleOrNull() ?: return
        val result = calculateMaterialQuantityUseCase(spec = material, areaM2 = area)
        _uiState.update { it.copy(calculatedUnits = result.roundedUnits) }
    }
}
