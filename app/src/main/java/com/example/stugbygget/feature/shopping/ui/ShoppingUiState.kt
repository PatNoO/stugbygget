package com.example.stugbygget.feature.shopping.ui

import com.example.stugbygget.domain.model.ShoppingList

data class ShoppingUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val shoppingLists: List<ShoppingList> = emptyList(),
    val listNameInput: String = "",
    val phaseInput: String = "general",
    val itemNameInput: String = "",
    val itemQuantityInput: String = "1",
    val itemUnitInput: String = "pcs"
)
