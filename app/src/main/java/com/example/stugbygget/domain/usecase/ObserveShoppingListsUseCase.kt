package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow

class ObserveShoppingListsUseCase(
    private val repository: ShoppingRepository
) {
    operator fun invoke(projectId: String): Flow<List<ShoppingList>> {
        return repository.observeShoppingLists(projectId)
    }
}
