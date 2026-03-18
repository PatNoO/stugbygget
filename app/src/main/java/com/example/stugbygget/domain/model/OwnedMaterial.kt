package com.example.stugbygget.domain.model

data class OwnedMaterial(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val notes: String = "",
)
