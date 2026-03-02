package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.RoomDimensions
import com.example.stugbygget.domain.repository.RoomDimensionsRepository

enum class RoomDimensionTarget {
    WIDTH,
    HEIGHT
}

class ExportMeasurementToRoomPlannerUseCase(
    private val roomDimensionsRepository: RoomDimensionsRepository,
    private val converter: MeasurementUnitConverter
) {
    operator fun invoke(
        roomId: String,
        valueMeters: Float,
        target: RoomDimensionTarget
    ): RoomDimensions {
        val current = roomDimensionsRepository.read(roomId)
        val valueCm = converter.metersToRoundedCentimeters(valueMeters)
        val updated = when (target) {
            RoomDimensionTarget.WIDTH -> current.copy(widthCm = valueCm)
            RoomDimensionTarget.HEIGHT -> current.copy(heightCm = valueCm)
        }
        roomDimensionsRepository.save(roomId, updated)
        return updated
    }
}
