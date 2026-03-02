package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RoomFurniture
import com.example.stugbygget.domain.repository.RoomLayoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveRoomLayoutUseCase(
    private val repository: RoomLayoutRepository
) {
    operator fun invoke(roomId: String): Flow<List<RoomFurniture>> = repository.observeLayout(roomId)
}
