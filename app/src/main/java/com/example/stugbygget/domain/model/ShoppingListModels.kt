package com.example.stugbygget.domain.model

import java.time.Instant

data class ShoppingList(
    val id: String,
    val name: String,
    val phaseId: String,
    val items: List<ShoppingItem>,
    val createdBy: String,
    val sharedWith: List<String>,
    val totalEstimate: Double,
    val updatedAt: Instant
)

data class ShoppingItem(
    val id: String,
    val materialId: String?,
    val name: String,
    val quantity: Double,
    val unit: String,
    val purchased: Boolean,
    val purchasedPrice: Double?,
    val purchasedAt: Instant?
)
