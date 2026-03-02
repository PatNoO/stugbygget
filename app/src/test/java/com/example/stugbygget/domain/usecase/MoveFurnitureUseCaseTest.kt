package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RoomFurniture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveFurnitureUseCaseTest {

    private val useCase = MoveFurnitureUseCase()

    @Test
    fun `snap to grid when moving furniture`() {
        val furniture = listOf(
            RoomFurniture("a", "Soffa", 200, 100, 0, 0),
            RoomFurniture("b", "Säng", 100, 100, 500, 300)
        )

        val result = useCase(
            furniture = furniture,
            movingId = "a",
            targetXCm = 133f,
            targetYCm = 88f,
            roomWidthCm = 1200,
            roomHeightCm = 800,
            snapStepCm = 50
        )

        val moved = result.furniture.first { it.id == "a" }
        assertTrue(result.applied)
        assertEquals(150, moved.xCm)
        assertEquals(100, moved.yCm)
    }

    @Test
    fun `reject overlap with another furniture`() {
        val furniture = listOf(
            RoomFurniture("a", "Soffa", 200, 100, 0, 0),
            RoomFurniture("b", "Säng", 100, 100, 300, 0)
        )

        val result = useCase(
            furniture = furniture,
            movingId = "a",
            targetXCm = 320f,
            targetYCm = 20f,
            roomWidthCm = 1200,
            roomHeightCm = 800,
            snapStepCm = 50
        )

        assertFalse(result.applied)
        assertEquals("Placering blockerad: överlappar annan möbel.", result.reason)
    }
}
