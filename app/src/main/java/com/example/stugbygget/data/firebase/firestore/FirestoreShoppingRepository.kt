package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.core.offline.OfflineSyncCoordinator
import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.example.stugbygget.domain.repository.ShoppingRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreShoppingRepository(
    private val firestore: FirebaseFirestore,
    private val offlineSyncCoordinator: OfflineSyncCoordinator
) : ShoppingRepository {
    override fun observeShoppingLists(projectId: String): Flow<List<ShoppingList>> = callbackFlow {
        val query = firestore.collection("projects")
            .document(projectId)
            .collection("shopping_lists")

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val lists = snapshot?.documents.orEmpty()
                .mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    runCatching { ShoppingDocumentMapper.fromMap(doc.id, data) }.getOrNull()
                }
                .sortedByDescending { it.updatedAt }
            trySend(lists)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun createShoppingList(
        projectId: String,
        name: String,
        phaseId: String,
        createdBy: String
    ) {
        val doc = firestore.collection("projects")
            .document(projectId)
            .collection("shopping_lists")
            .document()

        val list = ShoppingList(
            id = doc.id,
            name = name.trim(),
            phaseId = phaseId.trim().ifBlank { "general" },
            items = emptyList(),
            createdBy = createdBy,
            sharedWith = listOf(createdBy),
            totalEstimate = 0.0,
            updatedAt = Instant.now()
        )
        offlineSyncCoordinator.runOrQueue {
            doc.set(ShoppingDocumentMapper.toMap(list)).await()
        }
    }

    override suspend fun addItem(projectId: String, listId: String, item: ShoppingItem) {
        val listRef = firestore.collection("projects")
            .document(projectId)
            .collection("shopping_lists")
            .document(listId)
        offlineSyncCoordinator.runOrQueue {
            val snapshot = listRef.get().await()
            val currentData = snapshot.data ?: return@runOrQueue
            val currentList = ShoppingDocumentMapper.fromMap(snapshot.id, currentData)
            val updated = currentList.copy(items = currentList.items + item)
            listRef.set(ShoppingDocumentMapper.toMap(updated)).await()
        }
    }

    override suspend fun updateItemPurchased(
        projectId: String,
        listId: String,
        itemId: String,
        purchased: Boolean
    ) {
        val listRef = firestore.collection("projects")
            .document(projectId)
            .collection("shopping_lists")
            .document(listId)
        offlineSyncCoordinator.runOrQueue {
            val snapshot = listRef.get().await()
            val currentData = snapshot.data ?: return@runOrQueue
            val currentList = ShoppingDocumentMapper.fromMap(snapshot.id, currentData)
            val updatedItems = currentList.items.map { item ->
                if (item.id == itemId) {
                    item.copy(
                        purchased = purchased,
                        purchasedAt = if (purchased) Instant.now() else null
                    )
                } else {
                    item
                }
            }
            val updated = currentList.copy(items = updatedItems)
            listRef.set(ShoppingDocumentMapper.toMap(updated)).await()
        }
    }
}
