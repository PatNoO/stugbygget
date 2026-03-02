package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RoomFurniture
import com.example.stugbygget.domain.repository.RoomLayoutRepository

class SaveRoomLayoutUseCase(
    private val repository: RoomLayoutRepository
) {
    suspend operator fun invoke(roomId: String, furniture: List<RoomFurniture>) {
        repository.saveLayout(roomId, furniture)
    }
}
