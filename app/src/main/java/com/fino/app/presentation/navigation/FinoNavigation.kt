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
    object AddRecurringBill : Screen("add_recurring_bill?ruleId={ruleId}") {
        fun createRoute(ruleId: Long? = null) = if (ruleId != null) "add_recurring_bill?ruleId=$ruleId" else "add_recurring_bill"
    }
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
    object PeriodTransactions : Screen("period_transactions/{startDate}/{endDate}/{periodLabel}") {
        fun createRoute(startDate: Long, endDate: Long, periodLabel: String) =
            "period_transactions/$startDate/$endDate/${periodLabel.replace(" ", "_")}"
    }
    object TypeTransactions : Screen("type_transactions/{transactionType}/{label}") {
        fun createRoute(transactionType: String, label: String) =
            "type_transactions/$transactionType/$label"
    }
    object PaymentMethodTransactions : Screen("payment_method_transactions/{method}/{filter}") {
        fun createRoute(method: String, filter: String = "") = "payment_method_transactions/$method/${filter.replace("/", "_").ifEmpty { "all" }}"
    }
    object ReviewUncategorized : Screen("review_uncategorized")
    object ManageMerchantMappings : Screen("manage_merchant_mappings")
    object PatternSuggestions : Screen("pattern_suggestions")
    object AddEditCreditCard : Screen("add_edit_credit_card?cardId={cardId}") {
        fun createRoute(cardId: Long? = null) = if (cardId != null) "add_edit_credit_card?cardId=$cardId" else "add_edit_credit_card"
    }
    object EMITracker : Screen("emi_tracker")
    object AddEditEMI : Screen("add_edit_emi?emiId={emiId}") {
        fun createRoute(emiId: Long? = null) = if (emiId != null) "add_edit_emi?emiId=$emiId" else "add_edit_emi"
    }
    object AddEditLoan : Screen("add_edit_loan?loanId={loanId}") {
        fun createRoute(loanId: Long? = null) = if (loanId != null) "add_edit_loan?loanId=$loanId" else "add_edit_loan"
    }
    object SettingsScreen : Screen("settings_screen")
    object EditCreditCardBill : Screen("edit_credit_card_bill/{cardId}") {
        fun createRoute(cardId: Long) = "edit_credit_card_bill/$cardId"
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
                onAddRecurringBill = { navController.navigate(Screen.AddRecurringBill.createRoute()) },
                onEditRecurringBill = { ruleId -> navController.navigate(Screen.AddRecurringBill.createRoute(ruleId)) },
                onEditTransaction = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) },
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToEventDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                onNavigateToReviewUncategorized = { navController.navigate(Screen.ReviewUncategorized.route) },
                onNavigateToPeriodTransactions = { startDate, endDate, periodLabel ->
                    navController.navigate(Screen.PeriodTransactions.createRoute(startDate, endDate, periodLabel))
                },
                onNavigateToTypeTransactions = { transactionType, label ->
                    navController.navigate(Screen.TypeTransactions.createRoute(transactionType, label))
                }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onAddCard = { navController.navigate(Screen.AddEditCreditCard.createRoute()) },
                onEditCard = { cardId -> navController.navigate(Screen.AddEditCreditCard.createRoute(cardId)) },
                onNavigateToEMITracker = { navController.navigate(Screen.EMITracker.route) }
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
                },
                onPaymentMethodClick = { method, filter ->
                    navController.navigate(Screen.PaymentMethodTransactions.createRoute(method, filter))
                }
            )
        }

        composable(Screen.Rewards.route) {
            RewardsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToSettings = { navController.navigate(Screen.SettingsScreen.route) }
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
                onAddBill = { navController.navigate(Screen.AddRecurringBill.createRoute()) },
                onEditBill = { ruleId -> navController.navigate(Screen.AddRecurringBill.createRoute(ruleId)) },
                onEditCreditCardBill = { cardId -> navController.navigate(Screen.EditCreditCardBill.createRoute(cardId)) },
                onScanPatterns = { navController.navigate(Screen.PatternSuggestions.route) }
            )
        }

        // Edit Credit Card Bill
        composable(
            route = Screen.EditCreditCardBill.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            // EditCreditCardBillViewModel reads cardId from SavedStateHandle
            EditCreditCardBillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddRecurringBill.route,
            arguments = listOf(
                navArgument("ruleId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            // AddRecurringBillViewModel reads ruleId from SavedStateHandle
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

        // Period Transactions (Today, Week, Month, Year)
        composable(
            route = Screen.PeriodTransactions.route,
            arguments = listOf(
                navArgument("startDate") { type = NavType.LongType },
                navArgument("endDate") { type = NavType.LongType },
                navArgument("periodLabel") { type = NavType.StringType }
            )
        ) {
            PeriodTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        // Type Transactions (Expenses, Income, Savings)
        composable(
            route = Screen.TypeTransactions.route,
            arguments = listOf(
                navArgument("transactionType") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType }
            )
        ) {
            TypeTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        // Payment Method Transactions
        composable(
            route = Screen.PaymentMethodTransactions.route,
            arguments = listOf(
                navArgument("method") { type = NavType.StringType },
                navArgument("filter") { type = NavType.StringType; defaultValue = "all" }
            )
        ) {
            // PaymentMethodTransactionsViewModel reads method and filter from SavedStateHandle
            PaymentMethodTransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                }
            )
        }

        // Review Uncategorized Transactions
        composable(Screen.ReviewUncategorized.route) {
            ReviewUncategorizedScreen(
                onNavigateBack = { navController.popBackStack() },
                onManageMappings = { navController.navigate(Screen.ManageMerchantMappings.route) }
            )
        }

        // Manage Merchant Mappings
        composable(Screen.ManageMerchantMappings.route) {
            ManageMerchantMappingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Pattern Suggestions
        composable(Screen.PatternSuggestions.route) {
            PatternSuggestionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Add/Edit Credit Card
        composable(
            route = Screen.AddEditCreditCard.route,
            arguments = listOf(
                navArgument("cardId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddEditCreditCardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // EMI Tracker
        composable(Screen.EMITracker.route) {
            EMITrackerScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddEMI = { navController.navigate(Screen.AddEditEMI.createRoute()) },
                onEditEMI = { emiId -> navController.navigate(Screen.AddEditEMI.createRoute(emiId)) },
                onAddLoan = { navController.navigate(Screen.AddEditLoan.createRoute()) },
                onEditLoan = { loanId -> navController.navigate(Screen.AddEditLoan.createRoute(loanId)) }
            )
        }

        // Add/Edit EMI
        composable(
            route = Screen.AddEditEMI.route,
            arguments = listOf(
                navArgument("emiId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddEditEMIScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Add/Edit Loan
        composable(
            route = Screen.AddEditLoan.route,
            arguments = listOf(
                navArgument("loanId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddEditLoanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMerchantMappings = { navController.navigate(Screen.ManageMerchantMappings.route) }
            )
        }
    }
}
