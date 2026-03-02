package com.example.stugbygget.domain.usecase

import com.example.stugbygget.domain.model.RoomFurniture
import kotlin.math.roundToInt

class MoveFurnitureUseCase {

    /**
     * Applies room-planner movement rules:
     * 1) snap position to the configured grid,
     * 2) keep furniture inside room bounds,
     * 3) reject overlaps with other furniture.
     */
    operator fun invoke(
        furniture: List<RoomFurniture>,
        movingId: String,
        targetXCm: Float,
        targetYCm: Float,
        roomWidthCm: Int,
        roomHeightCm: Int,
        snapStepCm: Int
    ): MoveFurnitureResult {
        val current = furniture.firstOrNull { it.id == movingId }
            ?: return MoveFurnitureResult(furniture, false, "Furniture item was not found.")

        val snappedX = snapToGrid(targetXCm, snapStepCm)
        val snappedY = snapToGrid(targetYCm, snapStepCm)
        val boundedX = snappedX.coerceIn(0, roomWidthCm - current.widthCm)
        val boundedY = snappedY.coerceIn(0, roomHeightCm - current.depthCm)
        val candidate = current.copy(xCm = boundedX, yCm = boundedY)

        val overlaps = furniture
            .asSequence()
            .filter { it.id != movingId }
            .any { other -> isOverlapping(candidate, other) }
        if (overlaps) {
            return MoveFurnitureResult(furniture, false, "Placement blocked: overlaps another furniture item.")
        }

        val updated = furniture.map { item ->
            if (item.id == movingId) candidate else item
        }
        return MoveFurnitureResult(updated, true, null)
    }

    private fun snapToGrid(value: Float, stepCm: Int): Int {
        return (value / stepCm).roundToInt() * stepCm
    }

    private fun isOverlapping(a: RoomFurniture, b: RoomFurniture): Boolean {
        val separatedX = a.xCm + a.widthCm <= b.xCm || b.xCm + b.widthCm <= a.xCm
        val separatedY = a.yCm + a.depthCm <= b.yCm || b.yCm + b.depthCm <= a.yCm
        return !(separatedX || separatedY)
    }
}

data class MoveFurnitureResult(
    val furniture: List<RoomFurniture>,
    val applied: Boolean,
    val reason: String?
)
