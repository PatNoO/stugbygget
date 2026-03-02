package com.example.stugbygget.feature.roomplanner.ui

import com.example.stugbygget.domain.model.RoomFurniture

data class RoomPlannerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val roomId: String = "default-room",
    val roomWidthCm: Int = 1200,
    val roomHeightCm: Int = 800,
    val gridStepCm: Int = 50,
    val furniture: List<RoomFurniture> = defaultFurniture(),
    val selectedFurnitureId: String? = null
)

internal fun defaultFurniture(): List<RoomFurniture> {
    return listOf(
        RoomFurniture("soffa", "Soffa", 700, 300, 100, 120),
        RoomFurniture("sang", "Säng", 600, 500, 180, 470),
        RoomFurniture("matbord", "Matbord", 400, 300, 750, 280),
        RoomFurniture("badkar", "Badkar", 550, 250, 660, 560)
    )
}
