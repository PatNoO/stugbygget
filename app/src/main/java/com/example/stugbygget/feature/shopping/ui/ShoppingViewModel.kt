package com.example.stugbygget.feature.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stugbygget.domain.usecase.AddShoppingItemUseCase
import com.example.stugbygget.domain.usecase.CreateShoppingListUseCase
import com.example.stugbygget.domain.usecase.ObserveShoppingListsUseCase
import com.example.stugbygget.domain.usecase.ToggleShoppingItemPurchasedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val observeShoppingListsUseCase: ObserveShoppingListsUseCase,
    private val createShoppingListUseCase: CreateShoppingListUseCase,
    private val addShoppingItemUseCase: AddShoppingItemUseCase,
    private val toggleShoppingItemPurchasedUseCase: ToggleShoppingItemPurchasedUseCase,
    private val projectId: String,
    private val currentUserIdProvider: () -> String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingUiState())
    val uiState: StateFlow<ShoppingUiState> = _uiState.asStateFlow()

    init {
        observeLists()
    }

    fun onListNameChanged(value: String) {
        _uiState.update { it.copy(listNameInput = value) }
    }

    fun onPhaseChanged(value: String) {
        _uiState.update { it.copy(phaseInput = value) }
    }

    fun onItemNameChanged(value: String) {
        _uiState.update { it.copy(itemNameInput = value) }
    }

    fun onItemQuantityChanged(value: String) {
        _uiState.update { it.copy(itemQuantityInput = value) }
    }

    fun onItemUnitChanged(value: String) {
        _uiState.update { it.copy(itemUnitInput = value) }
    }

    fun onCreateList() {
        val state = _uiState.value
        if (state.listNameInput.isBlank()) return
        val currentUserId = currentUserIdProvider()
        if (currentUserId.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to create shopping lists.") }
            return
        }
        viewModelScope.launch {
            runCatching {
                createShoppingListUseCase(
                    projectId = projectId,
                    name = state.listNameInput,
                    phaseId = state.phaseInput,
                    createdBy = currentUserId
                )
            }.onSuccess {
                _uiState.update { it.copy(listNameInput = "") }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Failed to create list.") }
            }
        }
    }

    fun onAddItem(listId: String) {
        val state = _uiState.value
        val quantity = state.itemQuantityInput.toDoubleOrNull() ?: 1.0
        if (state.itemNameInput.isBlank()) return

        viewModelScope.launch {
            runCatching {
                addShoppingItemUseCase(
                    projectId = projectId,
                    listId = listId,
                    name = state.itemNameInput,
                    quantity = quantity,
                    unit = state.itemUnitInput
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        itemNameInput = "",
                        itemQuantityInput = "1",
                        itemUnitInput = "pcs"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Failed to add item.") }
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
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Failed to update item.") }
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            shoppingLists = lists,
                            errorMessage = null
                        )
                    }
            }
        }
    }
}
