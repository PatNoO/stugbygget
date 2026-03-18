package com.example.stugbygget.feature.shopping.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stugbygget.di.AppContainer

class ShoppingViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            return ShoppingViewModel(
                observeShoppingListsUseCase = container.observeShoppingListsUseCase,
                createShoppingListUseCase = container.createShoppingListUseCase,
                addShoppingItemUseCase = container.addShoppingItemUseCase,
                toggleShoppingItemPurchasedUseCase = container.toggleShoppingItemPurchasedUseCase,
                compareShoppingPricesUseCase = container.compareShoppingPricesUseCase,
                observePriceQuotesUseCase = container.observePriceQuotesUseCase,
                projectId = container.projectSessionRepository.getProjectId(),
                currentUserIdProvider = container.projectSessionRepository::getCurrentUserId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
