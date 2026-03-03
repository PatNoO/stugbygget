package com.example.stugbygget.feature.shopping.ui

import com.example.stugbygget.domain.model.ShoppingList

data class ShoppingItemDraftUiState(
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "pcs"
)

data class ShoppingUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val shoppingLists: List<ShoppingList> = emptyList(),
    val listNameInput: String = "",
    val phaseInput: String = "general",
    val itemDrafts: Map<String, ShoppingItemDraftUiState> = emptyMap()
)
