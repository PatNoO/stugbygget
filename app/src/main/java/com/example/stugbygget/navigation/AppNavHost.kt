package com.example.stugbygget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stugbygget.core.ui.PlaceholderScreen
import com.example.stugbygget.di.AppContainer
import com.example.stugbygget.feature.aichat.ui.AiChatScreen
import com.example.stugbygget.feature.gallery.ui.GalleryScreen
import com.example.stugbygget.feature.planning.ui.PlanningScreen
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
            AiChatScreen()
        }
        composable(AppRoute.RoomPlanner.route) {
            PlaceholderScreen("Rumsplanerare", "2D-planering med möbler och mått")
        }
        composable(AppRoute.ArMeasure.route) {
            PlaceholderScreen("AR-mätning", "Mät ytor och avstånd med kameran")
        }
        composable(AppRoute.Materials.route) {
            PlaceholderScreen("Material", "Materialkatalog och mängdberäkning")
        }
        composable(AppRoute.Shopping.route) {
            PlaceholderScreen("Inköp", "Delade inköpslistor och prisjämförelse")
        }
        composable(AppRoute.Budget.route) {
            PlaceholderScreen("Budget", "Budget, prognos och varningar")
        }
        composable(AppRoute.Logistics.route) {
            PlaceholderScreen("Logistik", "Transportkostnad och leveransplanering")
        }
    }
}
