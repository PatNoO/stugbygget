package com.example.stugbygget.domain.repository

import com.example.stugbygget.domain.model.RoomFurniture
import kotlinx.coroutines.flow.Flow

interface RoomLayoutRepository {
    fun observeLayout(roomId: String): Flow<List<RoomFurniture>>
    suspend fun saveLayout(roomId: String, furniture: List<RoomFurniture>)
}
