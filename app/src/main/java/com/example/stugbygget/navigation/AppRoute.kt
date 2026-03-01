package com.example.stugbygget.navigation

sealed class AppRoute(val route: String, val title: String) {
    data object Planning : AppRoute("planning", "Planering")
    data object Todos : AppRoute("todos", "Todo")
    data object Gallery : AppRoute("gallery", "Galleri")
    data object AiChat : AppRoute("ai_chat", "Stugan AI")
    data object RoomPlanner : AppRoute("room_planner", "Rumsplanerare")
    data object ArMeasure : AppRoute("ar_measure", "AR-mätning")
    data object Materials : AppRoute("materials", "Material")
    data object Shopping : AppRoute("shopping", "Inköp")
    data object Budget : AppRoute("budget", "Budget")
    data object Logistics : AppRoute("logistics", "Logistik")
}

val primaryRoutes = listOf(
    AppRoute.Planning,
    AppRoute.Todos,
    AppRoute.Gallery,
    AppRoute.AiChat,
    AppRoute.RoomPlanner,
    AppRoute.ArMeasure,
    AppRoute.Materials,
    AppRoute.Shopping,
    AppRoute.Budget,
    AppRoute.Logistics
)
