package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ShoppingRepository

class ToggleShoppingItemPurchasedUseCase(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(
        projectId: String,
        listId: String,
        itemId: String,
        purchased: Boolean
    ) {
        repository.updateItemPurchased(
            projectId = projectId,
            listId = listId,
            itemId = itemId,
            purchased = purchased
        )
    }
}
