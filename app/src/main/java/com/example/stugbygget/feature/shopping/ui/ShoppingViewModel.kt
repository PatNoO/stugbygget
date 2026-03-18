package com.example.stugbygget.feature.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.AddShoppingItemUseCase
import com.example.stugbygget.domain.usecase.CompareShoppingPricesUseCase
import com.example.stugbygget.domain.usecase.CreateShoppingListUseCase
import com.example.stugbygget.domain.usecase.ObservePriceQuotesUseCase
import com.example.stugbygget.domain.usecase.ObserveShoppingListsUseCase
import com.example.stugbygget.domain.usecase.ToggleShoppingItemPurchasedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val observeShoppingListsUseCase: ObserveShoppingListsUseCase,
    private val createShoppingListUseCase: CreateShoppingListUseCase,
    private val addShoppingItemUseCase: AddShoppingItemUseCase,
    private val toggleShoppingItemPurchasedUseCase: ToggleShoppingItemPurchasedUseCase,
    private val compareShoppingPricesUseCase: CompareShoppingPricesUseCase,
    private val observePriceQuotesUseCase: ObservePriceQuotesUseCase,
    private val projectId: String,
    private val currentUserIdProvider: () -> String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        observeLists()
    }

    fun onListNameChanged(value: String) {
        _uiState.update { it.copy(listNameInput = value, errorMessage = null) }
    }

    fun onPhaseChanged(value: String) {
        _uiState.update { it.copy(phaseInput = value, errorMessage = null) }
    }

    fun onItemNameChanged(listId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                errorMessage = null,
                itemDrafts = state.itemDrafts + (
                    listId to state.itemDrafts.draftFor(listId).copy(name = value)
                    )
            )
        }
    }

    fun onItemQuantityChanged(listId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                errorMessage = null,
                itemDrafts = state.itemDrafts + (
                    listId to state.itemDrafts.draftFor(listId).copy(quantity = value)
                    )
            )
        }
    }

    fun onItemUnitChanged(listId: String, value: String) {
        _uiState.update { state ->
            state.copy(
                errorMessage = null,
                itemDrafts = state.itemDrafts + (
                    listId to state.itemDrafts.draftFor(listId).copy(unit = value)
                    )
            )
        }
    }

    fun onCreateList() {
        val state = _uiState.value
        if (state.listNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "List name is required.") }
            return
        }

        val currentUserId = currentUserIdProvider()
        if (currentUserId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to create shopping lists.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                createShoppingListUseCase(
                    projectId = projectId,
                    name = state.listNameInput,
                    phaseId = state.phaseInput,
                    createdBy = currentUserId
                )
            }.onSuccess {
                _uiState.update { it.copy(listNameInput = "", isSubmitting = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "Failed to create list."
                    )
                }
            }
        }
    }

    fun onAddItem(listId: String) {
        val state = _uiState.value
        val draft = state.itemDrafts.draftFor(listId)

        if (draft.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Item name is required.") }
            return
        }

        val quantity = draft.quantity.toDoubleOrNull()
        if (quantity == null || quantity <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Quantity must be greater than zero.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            runCatching {
                addShoppingItemUseCase(
                    projectId = projectId,
                    listId = listId,
                    name = draft.name,
                    quantity = quantity,
                    unit = draft.unit.ifBlank { "pcs" }
                )
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isSubmitting = false,
                        itemDrafts = current.itemDrafts + (listId to ShoppingItemDraftUiState())
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "Failed to add item."
                    )
                }
            }
        }
    }

    fun onTogglePurchased(listId: String, itemId: String, purchased: Boolean) {
        viewModelScope.launch {
            runCatching {
                toggleShoppingItemPurchasedUseCase(
                    projectId = projectId,
                    listId = listId,
                    itemId = itemId,
                    purchased = purchased
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "Failed to update item.")
                }
            }
        }
    }

    fun onComparePrice(listId: String) {
        val list = _uiState.value.shoppingLists.firstOrNull { it.id == listId } ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isComparingPrice = it.isComparingPrice + (listId to true)) }
            runCatching {
                val quotes = list.items
                    .mapNotNull { it.materialId }
                    .flatMap { materialId ->
                        observePriceQuotesUseCase(projectId, materialId).first()
                    }
                compareShoppingPricesUseCase(projectId, list, quotes)
            }.onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        isComparingPrice = state.isComparingPrice + (listId to false),
                        priceComparisons = state.priceComparisons + (listId to result)
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isComparingPrice = state.isComparingPrice + (listId to false),
                        errorMessage = throwable.message ?: "Failed to compare prices."
                    )
                }
            }
        }
    }

    private fun observeLists() {
        viewModelScope.launch {
            observeShoppingListsUseCase(projectId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = throwable.message ?: "Failed to load lists.")
                    }
                }
                .collect { lists ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            shoppingLists = lists,
                            itemDrafts = lists.associate { list ->
                                list.id to state.itemDrafts.draftFor(list.id)
                            },
                            errorMessage = null
                        )
                    }
                }
        }
    }
}

private fun Map<String, ShoppingItemDraftUiState>.draftFor(listId: String): ShoppingItemDraftUiState {
    return this[listId] ?: ShoppingItemDraftUiState()
}
