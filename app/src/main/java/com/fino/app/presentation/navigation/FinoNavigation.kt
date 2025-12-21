package com.fino.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fino.app.presentation.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Cards : Screen("cards")
    object Analytics : Screen("analytics")
    object Rewards : Screen("rewards")
    object Settings : Screen("settings")
    object AddTransaction : Screen("add_transaction")
    object UpcomingBills : Screen("upcoming_bills")
    object AddRecurringBill : Screen("add_recurring_bill")
    object Comparison : Screen("comparison")
    object Events : Screen("events")
    object EventDetail : Screen("event/{eventId}") {
        fun createRoute(eventId: Long) = "event/$eventId"
    }
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
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
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToUpcomingBills = { navController.navigate(Screen.UpcomingBills.route) },
                onAddRecurringBill = { navController.navigate(Screen.AddRecurringBill.route) },
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToEventDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) }
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
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onNavigateToComparison = { navController.navigate(Screen.Comparison.route) }
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

        composable(Screen.UpcomingBills.route) {
            UpcomingBillsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddBill = { navController.navigate(Screen.AddRecurringBill.route) }
            )
        }

        composable(Screen.AddRecurringBill.route) {
            AddRecurringBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Comparison.route) {
            ComparisonScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Events.route) {
            EventsScreen(
                onNavigateBack = { navController.popBackStack() },
                onCreateEvent = { navController.navigate(Screen.CreateEvent.route) },
                onEventClick = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
            EventDetailScreen(
                eventId = eventId,
                onNavigateBack = { navController.popBackStack() },
                onEditEvent = { navController.navigate(Screen.EditEvent.createRoute(eventId)) }
            )
        }

        composable(Screen.CreateEvent.route) {
            CreateEventScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            // CreateEventViewModel reads eventId from SavedStateHandle for edit mode
            CreateEventScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
