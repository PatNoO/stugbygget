package com.example.stugbygget.domain.repository

data class RoomDimensions(
    val widthCm: Int,
    val heightCm: Int
)

interface RoomDimensionsRepository {
    fun read(roomId: String): RoomDimensions
    fun save(roomId: String, dimensions: RoomDimensions)
}
