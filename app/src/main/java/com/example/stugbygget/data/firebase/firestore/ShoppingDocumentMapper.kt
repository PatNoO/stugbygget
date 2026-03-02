package com.example.stugbygget.data.firebase.firestore

import com.example.stugbygget.domain.model.ShoppingItem
import com.example.stugbygget.domain.model.ShoppingList
import com.google.firebase.Timestamp
import java.time.Instant

object ShoppingDocumentMapper {
    fun fromMap(id: String, map: Map<String, Any>): ShoppingList {
        val rawItems = map["items"] as? List<*> ?: emptyList<Any>()
        val items = rawItems.mapNotNull { raw ->
            val itemMap = raw as? Map<*, *> ?: return@mapNotNull null
            val itemId = itemMap["id"] as? String ?: return@mapNotNull null
            ShoppingItem(
                id = itemId,
                materialId = itemMap["materialId"] as? String,
                name = itemMap["name"] as? String ?: "",
                quantity = (itemMap["quantity"] as? Number)?.toDouble() ?: 1.0,
                unit = itemMap["unit"] as? String ?: "pcs",
                purchased = itemMap["purchased"] as? Boolean ?: false,
                purchasedPrice = (itemMap["purchasedPrice"] as? Number)?.toDouble(),
                purchasedAt = (itemMap["purchasedAt"] as? Timestamp)?.toDate()?.toInstant()
            )
        }

        return ShoppingList(
            id = id,
            name = map["name"] as? String ?: "Unnamed list",
            phaseId = map["phaseId"] as? String ?: "general",
            items = items,
            createdBy = map["createdBy"] as? String ?: "unknown",
            sharedWith = (map["sharedWith"] as? List<*>)?.filterIsInstance<String>().orEmpty(),
            totalEstimate = (map["totalEstimate"] as? Number)?.toDouble() ?: 0.0,
            updatedAt = (map["updatedAt"] as? Timestamp)?.toDate()?.toInstant() ?: Instant.now()
        )
    }

    fun toMap(list: ShoppingList): Map<String, Any?> {
        return mapOf(
            "name" to list.name,
            "phaseId" to list.phaseId,
            "createdBy" to list.createdBy,
            "sharedWith" to list.sharedWith,
            "totalEstimate" to list.totalEstimate,
            "updatedAt" to Timestamp.now(),
            "items" to list.items.map { item ->
                mapOf(
                    "id" to item.id,
                    "materialId" to item.materialId,
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "unit" to item.unit,
                    "purchased" to item.purchased,
                    "purchasedPrice" to item.purchasedPrice,
                    "purchasedAt" to item.purchasedAt?.let { Timestamp(it.epochSecond, it.nano) }
                )
            }
        )
    }
}
