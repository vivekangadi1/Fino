package com.fino.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fino.app.presentation.screens.*
import com.fino.app.presentation.screens.insights.BillDetailScreen
import com.fino.app.presentation.screens.insights.CompareDetailScreen
import com.fino.app.presentation.screens.insights.DayDetailScreen
import com.fino.app.presentation.screens.insights.MerchantDetailScreen
import com.fino.app.presentation.screens.insights.NewMerchantsDetailScreen
import com.fino.app.presentation.screens.insights.SubscriptionsDetailScreen
import com.fino.app.presentation.screens.insights.WeekendDetailScreen

private const val DETAIL_ANIM_MS = 320

private fun detailEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(DETAIL_ANIM_MS),
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn(animationSpec = tween(DETAIL_ANIM_MS))

private fun detailExit(): ExitTransition =
    fadeOut(animationSpec = tween(DETAIL_ANIM_MS / 2))

private fun detailPopEnter(): EnterTransition =
    fadeIn(animationSpec = tween(DETAIL_ANIM_MS / 2))

private fun detailPopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(DETAIL_ANIM_MS),
        targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut(animationSpec = tween(DETAIL_ANIM_MS))

/**
 * Shared transition helpers for Insights drill-in detail routes.
 * All 7 drill-in destinations reuse identical slide-in-right + fade (320ms).
 */
private val detailEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) =
    { detailEnter() }
private val detailExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) =
    { detailExit() }
private val detailPopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) =
    { detailPopEnter() }
private val detailPopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) =
    { detailPopExit() }

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Activity : Screen("activity?tab={tab}") {
        fun createRoute(tab: String? = null): String =
            if (tab.isNullOrBlank()) "activity" else "activity?tab=$tab"
    }
    object Cards : Screen("cards")
    object Analytics : Screen("insights")
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

    // Insights drill-in routes (Phase A)
    object SubscriptionsDetail : Screen("insights/subscriptions")
    object MerchantDetail : Screen("insights/merchant/{merchantKey}") {
        fun createRoute(merchantKey: String): String {
            val encoded = java.net.URLEncoder.encode(merchantKey, "UTF-8")
            return "insights/merchant/$encoded"
        }
    }
    object BillDetail : Screen("insights/bill/{billId}") {
        fun createRoute(billId: Long) = "insights/bill/$billId"
    }
    object DayDetail : Screen("insights/day/{epochDay}") {
        fun createRoute(epochDay: Long) = "insights/day/$epochDay"
    }
    object CompareDetail : Screen("insights/compare")
    object WeekendDetail : Screen("insights/weekend")
    object NewMerchantsDetail : Screen("insights/new_merchants")
}

@Composable
fun FinoNavigation(
    pendingDeepLink: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(pendingDeepLink) {
        val route = pendingDeepLink ?: return@LaunchedEffect
        runCatching { navController.navigate(route) }
        onDeepLinkConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToActivity = { navController.navigate(Screen.Activity.createRoute()) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToUpcomingBills = { navController.navigate(Screen.Activity.createRoute("upcoming")) },
                onAddRecurringBill = { navController.navigate(Screen.AddRecurringBill.createRoute()) },
                onEditRecurringBill = { ruleId -> navController.navigate(Screen.AddRecurringBill.createRoute(ruleId)) },
                onEditTransaction = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) },
                onNavigateToEvents = { navController.navigate(Screen.Activity.createRoute("events")) },
                onNavigateToEventDetail = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                onNavigateToReviewUncategorized = { navController.navigate(Screen.ReviewUncategorized.route) },
                onNavigateToPeriodTransactions = { startDate, endDate, periodLabel ->
                    navController.navigate(Screen.PeriodTransactions.createRoute(startDate, endDate, periodLabel))
                },
                onNavigateToTypeTransactions = { transactionType, label ->
                    navController.navigate(Screen.TypeTransactions.createRoute(transactionType, label))
                },
                onNavigateToSettings = { navController.navigate(Screen.SettingsScreen.route) }
            )
        }

        composable(Screen.Cards.route) {
            CardsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToActivity = { navController.navigate(Screen.Activity.createRoute()) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onAddCard = { navController.navigate(Screen.AddEditCreditCard.createRoute()) },
                onEditCard = { cardId -> navController.navigate(Screen.AddEditCreditCard.createRoute(cardId)) },
                onNavigateToEMITracker = { navController.navigate(Screen.EMITracker.route) },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
            )
        }

        composable(
            route = Screen.Activity.route,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val tabArg = backStackEntry.arguments?.getString("tab")
            ActivityScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onTransactionClick = { transactionId -> navController.navigate(Screen.EditTransaction.createRoute(transactionId)) },
                onAddBill = { navController.navigate(Screen.AddRecurringBill.createRoute()) },
                onEditBill = { ruleId -> navController.navigate(Screen.AddRecurringBill.createRoute(ruleId)) },
                onEditCreditCardBill = { cardId -> navController.navigate(Screen.EditCreditCardBill.createRoute(cardId)) },
                onScanPatterns = { navController.navigate(Screen.PatternSuggestions.route) },
                onCreateEvent = { navController.navigate(Screen.CreateEvent.route) },
                onEventClick = { eventId -> navController.navigate(Screen.EventDetail.createRoute(eventId)) },
                initialTab = activityTabFromArg(tabArg)
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToCards = { navController.navigate(Screen.Cards.route) },
                onNavigateToActivity = { navController.navigate(Screen.Activity.createRoute()) },
                onNavigateToRewards = { navController.navigate(Screen.Rewards.route) },
                onNavigateToComparison = { navController.navigate(Screen.Comparison.route) },
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(Screen.CategoryTransactions.createRoute(categoryId, categoryName))
                },
                onPaymentMethodClick = { method, filter ->
                    navController.navigate(Screen.PaymentMethodTransactions.createRoute(method, filter))
                },
                onAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                onNavigateToSubscriptions = { navController.navigate(Screen.SubscriptionsDetail.route) },
                onNavigateToMerchant = { merchantKey ->
                    navController.navigate(Screen.MerchantDetail.createRoute(merchantKey))
                },
                onNavigateToBill = { cardId ->
                    navController.navigate(Screen.BillDetail.createRoute(cardId))
                },
                onNavigateToDay = { epochDay ->
                    navController.navigate(Screen.DayDetail.createRoute(epochDay))
                },
                onNavigateToCompare = { navController.navigate(Screen.CompareDetail.route) },
                onNavigateToWeekend = { navController.navigate(Screen.WeekendDetail.route) },
                onNavigateToNewMerchants = { navController.navigate(Screen.NewMerchantsDetail.route) }
            )
        }

        // Insights drill-in routes (Phase A) — all share detailTransitions()
        composable(
            route = Screen.SubscriptionsDetail.route,
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            SubscriptionsDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MerchantDetail.route,
            arguments = listOf(navArgument("merchantKey") { type = NavType.StringType }),
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            MerchantDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BillDetail.route,
            arguments = listOf(navArgument("billId") { type = NavType.LongType }),
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            BillDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DayDetail.route,
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType }),
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            DayDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CompareDetail.route,
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            CompareDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WeekendDetail.route,
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            WeekendDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NewMerchantsDetail.route,
            enterTransition = detailEnterTransition,
            exitTransition = detailExitTransition,
            popEnterTransition = detailPopEnterTransition,
            popExitTransition = detailPopExitTransition
        ) {
            NewMerchantsDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenMerchant = { merchantKey ->
                    navController.navigate(Screen.MerchantDetail.createRoute(merchantKey))
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
                },
                onOpenMerchant = { merchantKey ->
                    navController.navigate(Screen.MerchantDetail.createRoute(merchantKey))
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
                onNavigateToMerchantMappings = { navController.navigate(Screen.ManageMerchantMappings.route) },
                onNavigateToMilestones = { navController.navigate(Screen.Rewards.route) }
            )
        }
    }
}
