package com.example.stugbygget.navigation

sealed class AppRoute(val route: String, val title: String) {
    data object Planning : AppRoute("planning", "Planning")
    data object Todos : AppRoute("todos", "Todos")
    data object Gallery : AppRoute("gallery", "Gallery")
    data object AiChat : AppRoute("ai_chat", "Stugan AI")
    data object ArMeasure : AppRoute("ar_measure", "AR Measure")
    data object Materials : AppRoute("materials", "Materials")
    object MaterialDetail : AppRoute("materials/{materialId}", "Material") {
        fun createRoute(materialId: String) = "materials/$materialId"
    }
    data object Shopping : AppRoute("shopping", "Shopping")
    data object Budget : AppRoute("budget", "Budget")
    data object Logistics : AppRoute("logistics", "Logistics")
}

val primaryRoutes = listOf(
    AppRoute.Planning,
    AppRoute.Todos,
    AppRoute.Gallery,
    AppRoute.AiChat,
    AppRoute.ArMeasure,
    AppRoute.Materials,
    AppRoute.Shopping,
    AppRoute.Budget,
    AppRoute.Logistics
)
