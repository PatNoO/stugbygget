package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.repository.ShoppingRepository
import java.util.UUID

class AddShoppingItemUseCase(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(
        projectId: String,
        listId: String,
        name: String,
        quantity: Double,
        unit: String
    ) {
        repository.addItem(
            projectId = projectId,
            listId = listId,
            item = ShoppingItem(
                id = UUID.randomUUID().toString(),
                materialId = null,
                name = name.trim(),
                quantity = quantity,
                unit = unit.trim().ifBlank { "pcs" },
                purchased = false,
                purchasedPrice = null,
                purchasedAt = null
            )
        )
    }
}
