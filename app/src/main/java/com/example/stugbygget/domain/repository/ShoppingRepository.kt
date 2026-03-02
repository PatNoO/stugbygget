package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeShoppingLists(projectId: String): Flow<List<ShoppingList>>
    suspend fun createShoppingList(
        projectId: String,
        name: String,
        phaseId: String,
        createdBy: String
    )

    suspend fun addItem(projectId: String, listId: String, item: ShoppingItem)
    suspend fun updateItemPurchased(
        projectId: String,
        listId: String,
        itemId: String,
        purchased: Boolean
    )
}
