package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.repository.RoomDimensions
import com.example.stugbygget.domain.repository.RoomDimensionsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportMeasurementToRoomPlannerUseCaseTest {

    @Test
    fun `export to width updates room width only`() {
        val repo = FakeRoomDimensionsRepository(RoomDimensions(1200, 800))
        val useCase = ExportMeasurementToRoomPlannerUseCase(repo, MeasurementUnitConverter())

        val updated = useCase(
            roomId = "default-room",
            valueMeters = 4.85f,
            target = RoomDimensionTarget.WIDTH
        )

        assertEquals(485, updated.widthCm)
        assertEquals(800, updated.heightCm)
    }

    private class FakeRoomDimensionsRepository(
        private var dimensions: RoomDimensions
    ) : RoomDimensionsRepository {
        override fun read(roomId: String): RoomDimensions = dimensions
        override fun save(roomId: String, dimensions: RoomDimensions) {
            this.dimensions = dimensions
        }
    }
}
