package com.example.stugbygget.feature.materials.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.model.OwnedMaterial
import com.example.stugbygget.domain.usecase.CalculateMaterialQuantityUseCase
import com.example.stugbygget.domain.usecase.DeleteOwnedMaterialUseCase
import com.example.stugbygget.domain.usecase.ObserveMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObserveOwnedMaterialsUseCase
import com.example.stugbygget.domain.usecase.ObservePriceQuotesUseCase
import com.example.stugbygget.domain.usecase.UpsertOwnedMaterialUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MaterialsViewModel(
    private val observeMaterialsUseCase: ObserveMaterialsUseCase,
    private val observeOwnedMaterialsUseCase: ObserveOwnedMaterialsUseCase,
    private val upsertOwnedMaterialUseCase: UpsertOwnedMaterialUseCase,
    private val deleteOwnedMaterialUseCase: DeleteOwnedMaterialUseCase,
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
        viewModelScope.launch {
            observeOwnedMaterialsUseCase(projectId)
                .catch { /* owned list is optional — ignore errors */ }
                .collect { owned -> _uiState.update { it.copy(ownedMaterials = owned) } }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // ── Owned materials ──

    fun onShowOwnedAddSheet() {
        _uiState.update {
            it.copy(
                showOwnedAddSheet = true,
                draftOwnedName = "",
                draftOwnedQuantity = "",
                draftOwnedUnit = "",
                draftOwnedNotes = "",
                ownedAddError = null,
            )
        }
    }

    fun onDismissOwnedAddSheet() {
        _uiState.update { it.copy(showOwnedAddSheet = false, ownedAddError = null) }
    }

    fun onDraftOwnedNameChanged(value: String) = _uiState.update { it.copy(draftOwnedName = value) }
    fun onDraftOwnedQuantityChanged(value: String) = _uiState.update { it.copy(draftOwnedQuantity = value) }
    fun onDraftOwnedUnitChanged(value: String) = _uiState.update { it.copy(draftOwnedUnit = value) }
    fun onDraftOwnedNotesChanged(value: String) = _uiState.update { it.copy(draftOwnedNotes = value) }

    fun onSubmitOwnedMaterial() {
        val state = _uiState.value
        if (state.draftOwnedName.isBlank()) {
            _uiState.update { it.copy(ownedAddError = "Name is required.") }
            return
        }
        val quantity = state.draftOwnedQuantity.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(ownedAddError = "Quantity must be a number.") }
            return
        }
        _uiState.update { it.copy(isAddingOwned = true, ownedAddError = null) }
        viewModelScope.launch {
            runCatching {
                upsertOwnedMaterialUseCase(
                    projectId,
                    OwnedMaterial(
                        id = "",
                        name = state.draftOwnedName.trim(),
                        quantity = quantity,
                        unit = state.draftOwnedUnit.trim(),
                        notes = state.draftOwnedNotes.trim(),
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isAddingOwned = false, showOwnedAddSheet = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(isAddingOwned = false, ownedAddError = error.message ?: "Failed to save.") }
            }
        }
    }

    fun onDeleteOwnedMaterial(id: String) {
        viewModelScope.launch { runCatching { deleteOwnedMaterialUseCase(projectId, id) } }
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
