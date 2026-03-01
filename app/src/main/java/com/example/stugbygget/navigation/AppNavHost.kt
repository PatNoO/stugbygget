package com.example.stugbygget.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stugbygget.core.ui.PlaceholderScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Planning.route,
        modifier = modifier
    ) {
        composable(AppRoute.Planning.route) {
            PlaceholderScreen("Planering", "Tidslinje med faser och progress")
        }
        composable(AppRoute.Todos.route) {
            PlaceholderScreen("Todo", "Uppgiftshantering med ansvar och prioritet")
        }
        composable(AppRoute.Gallery.route) {
            PlaceholderScreen("Galleri", "Före, under och efter bilder")
        }
        composable(AppRoute.AiChat.route) {
            PlaceholderScreen("Stugan AI", "Fråga AI om material, regler och planering")
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
