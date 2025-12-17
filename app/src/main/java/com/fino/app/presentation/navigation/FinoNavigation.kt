package com.fino.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fino.app.presentation.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Cards : Screen("cards")
    object Analytics : Screen("analytics")
    object Rewards : Screen("rewards")
    object Settings : Screen("settings")
    object AddTransaction : Screen("add_transaction")
}

@Composable
fun FinoNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) }
            )
        }

        composable(Screen.Rewards.route) {
            RewardsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
