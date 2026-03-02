package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.ShoppingRepository

class CreateShoppingListUseCase(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(
        projectId: String,
        name: String,
        phaseId: String,
        createdBy: String
    ) {
        repository.createShoppingList(
            projectId = projectId,
            name = name,
            phaseId = phaseId,
            createdBy = createdBy
        )
    }
}
