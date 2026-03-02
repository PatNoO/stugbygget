package com.example.stugbygget.feature.roomplanner.ui

data class RoomPlannerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val roomWidthCm: Int = 1200,
    val roomHeightCm: Int = 800,
    val gridStepCm: Int = 50,
    val furniture: List<FurniturePrimitiveUiModel> = defaultFurniture(),
    val selectedFurnitureId: String? = null
)

data class FurniturePrimitiveUiModel(
    val id: String,
    val label: String,
    val widthCm: Int,
    val depthCm: Int,
    val xCm: Int,
    val yCm: Int
)

internal fun defaultFurniture(): List<FurniturePrimitiveUiModel> {
    return listOf(
        FurniturePrimitiveUiModel("soffa", "Soffa", 700, 300, 100, 120),
        FurniturePrimitiveUiModel("sang", "Säng", 600, 500, 180, 470),
        FurniturePrimitiveUiModel("matbord", "Matbord", 400, 300, 750, 280),
        FurniturePrimitiveUiModel("badkar", "Badkar", 550, 250, 660, 560)
    )
}
