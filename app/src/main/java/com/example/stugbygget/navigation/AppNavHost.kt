package com.example.stugbygget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stugbygget.core.ui.PlaceholderScreen
import com.example.stugbygget.feature.materials.ui.MaterialDetailScreen
import com.example.stugbygget.feature.materials.ui.MaterialsScreen
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.feature.aichat.ui.AiChatScreen
import com.example.stugbygget.feature.armeasure.ui.ArMeasureScreen
import com.example.stugbygget.feature.budget.ui.BudgetScreen
import com.example.stugbygget.feature.gallery.ui.GalleryScreen
import com.example.stugbygget.feature.planning.ui.PlanningScreen
import com.example.stugbygget.feature.roomplanner.ui.RoomPlannerScreen
import com.example.stugbygget.feature.shopping.ui.ShoppingScreen
import com.example.stugbygget.feature.todos.ui.TodosScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Planning.route,
        modifier = modifier
    ) {
        composable(AppRoute.Planning.route) {
            PlanningScreen(container = container)
        }
        composable(AppRoute.Todos.route) {
            TodosScreen(container = container)
        }
        composable(AppRoute.Gallery.route) {
            GalleryScreen(container = container)
        }
        composable(AppRoute.AiChat.route) {
            AiChatScreen(container = container)
        }
        composable(AppRoute.RoomPlanner.route) {
            RoomPlannerScreen(container = container)
        }
        composable(AppRoute.ArMeasure.route) {
            ArMeasureScreen(container = container)
        }
        composable(AppRoute.Materials.route) {
            MaterialsScreen(
                container = container,
                onMaterialClick = { materialId ->
                    navController.navigate(AppRoute.MaterialDetail.createRoute(materialId))
                },
            )
        }
        composable(AppRoute.MaterialDetail.route) { backStackEntry ->
            val materialId = backStackEntry.arguments?.getString("materialId") ?: ""
            MaterialDetailScreen(container = container, materialId = materialId)
        }
        composable(AppRoute.Shopping.route) {
            ShoppingScreen(container = container)
        }
        composable(AppRoute.Budget.route) {
            BudgetScreen(container = container)
        }
        composable(AppRoute.Logistics.route) {
            PlaceholderScreen("Logistics", "Transport costs and delivery planning")
        }
    }
}
