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
    object AddTransactionForEvent : Screen("add_transaction_event/{eventId}") {
        fun createRoute(eventId: Long) = "add_transaction_event/$eventId"
    }
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
    object AddSubCategory : Screen("add_sub_category/{eventId}") {
        fun createRoute(eventId: Long) = "add_sub_category/$eventId"
    }
    object EditSubCategory : Screen("edit_sub_category/{eventId}/{subCategoryId}") {
        fun createRoute(eventId: Long, subCategoryId: Long) = "edit_sub_category/$eventId/$subCategoryId"
    }
    object SubCategoryDetail : Screen("sub_category_detail/{eventId}/{subCategoryId}") {
        fun createRoute(eventId: Long, subCategoryId: Long) = "sub_category_detail/$eventId/$subCategoryId"
    }
    object AddVendor : Screen("add_vendor/{eventId}") {
        fun createRoute(eventId: Long) = "add_vendor/$eventId"
    }
    object EditVendor : Screen("edit_vendor/{eventId}/{vendorId}") {
        fun createRoute(eventId: Long, vendorId: Long) = "edit_vendor/$eventId/$vendorId"
    }
    object FamilyMembers : Screen("family_members")
    object CategoryTransactions : Screen("category_transactions/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Long, categoryName: String) = "category_transactions/$categoryId/${categoryName.replace("/", "_")}"
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
                onNavigateToComparison = { navController.navigate(Screen.Comparison.route) },
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(Screen.CategoryTransactions.createRoute(categoryId, categoryName))
                }
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
                onEditEvent = { navController.navigate(Screen.EditEvent.createRoute(eventId)) },
                onAddExpense = { navController.navigate(Screen.AddTransactionForEvent.createRoute(eventId)) },
                onAddSubCategory = { navController.navigate(Screen.AddSubCategory.createRoute(eventId)) },
                onEditSubCategory = { subCategoryId -> navController.navigate(Screen.SubCategoryDetail.createRoute(eventId, subCategoryId)) },
                onAddVendor = { navController.navigate(Screen.AddVendor.createRoute(eventId)) },
                onEditVendor = { vendorId -> navController.navigate(Screen.EditVendor.createRoute(eventId, vendorId)) },
                onPaymentClick = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) },
                onManageFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) },
                onTransactionClick = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) }
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

        composable(
            route = Screen.AddTransactionForEvent.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            // AddTransactionViewModel reads eventId from SavedStateHandle
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Transaction
        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
        ) {
            // AddTransactionViewModel reads transactionId from SavedStateHandle
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Add Sub-Category
        composable(
            route = Screen.AddSubCategory.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            // AddSubCategoryViewModel reads eventId from SavedStateHandle
            AddSubCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Sub-Category
        composable(
            route = Screen.EditSubCategory.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
                navArgument("subCategoryId") { type = NavType.LongType }
            )
        ) {
            // AddSubCategoryViewModel reads eventId and subCategoryId from SavedStateHandle
            AddSubCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sub-Category Detail (shows transactions list)
        composable(
            route = Screen.SubCategoryDetail.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
                navArgument("subCategoryId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
            val subCategoryId = backStackEntry.arguments?.getLong("subCategoryId") ?: return@composable
            SubCategoryDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditSubCategory = { navController.navigate(Screen.EditSubCategory.createRoute(eventId, subCategoryId)) },
                onAddExpense = { navController.navigate(Screen.AddTransactionForEvent.createRoute(eventId)) },
                onTransactionClick = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) }
            )
        }

        // Add Vendor
        composable(
            route = Screen.AddVendor.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            // AddVendorViewModel reads eventId from SavedStateHandle
            AddVendorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Vendor
        composable(
            route = Screen.EditVendor.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.LongType },
                navArgument("vendorId") { type = NavType.LongType }
            )
        ) {
            // AddVendorViewModel reads eventId and vendorId from SavedStateHandle
            AddVendorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Family Members Management
        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Category Transactions
        composable(
            route = Screen.CategoryTransactions.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) {
            // CategoryTransactionsViewModel reads categoryId and categoryName from SavedStateHandle
            CategoryTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }
    }
}
