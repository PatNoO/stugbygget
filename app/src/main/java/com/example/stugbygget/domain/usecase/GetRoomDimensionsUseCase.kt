package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.RoomDimensions
import com.example.stugbygget.domain.repository.RoomDimensionsRepository

class GetRoomDimensionsUseCase(
    private val repository: RoomDimensionsRepository
) {
    operator fun invoke(roomId: String): RoomDimensions = repository.read(roomId)
}
