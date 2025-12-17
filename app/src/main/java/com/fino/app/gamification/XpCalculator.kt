package com.fino.app.gamification

/**
 * Calculates XP rewards for various user actions.
 */
class XpCalculator {

    companion object {
        val XP_REWARDS = mapOf(
            XpAction.CATEGORIZE_TRANSACTION to 5,
            XpAction.CATEGORIZE_SAME_DAY to 3,
            XpAction.CONFIRM_FUZZY_MATCH to 10,
            XpAction.REJECT_FUZZY_MATCH to 5,
            XpAction.ADD_MANUAL_TRANSACTION to 8,
            XpAction.SET_BUDGET to 20,
            XpAction.STAY_UNDER_BUDGET_WEEK to 10,
            XpAction.REVIEW_WEEKLY_SUMMARY to 15,
            XpAction.FIRST_TRANSACTION_OF_DAY to 5,
            XpAction.ADD_CREDIT_CARD to 25,
            XpAction.IDENTIFY_RECURRING to 15,
            XpAction.COMPLETE_ONBOARDING to 50
        )
    }

    /**
     * Get XP reward for an action.
     */
    fun getXpForAction(action: XpAction): Int {
        return XP_REWARDS[action] ?: 0
    }

    /**
     * Calculate total XP for multiple actions.
     */
    fun calculateTotalXp(actions: List<XpAction>): Int {
        return actions.sumOf { getXpForAction(it) }
    }

    /**
     * Get XP for categorizing a transaction.
     * Includes bonus if categorized on same day.
     */
    fun getCategorizationXp(isSameDay: Boolean): Int {
        val baseXp = getXpForAction(XpAction.CATEGORIZE_TRANSACTION)
        val bonusXp = if (isSameDay) getXpForAction(XpAction.CATEGORIZE_SAME_DAY) else 0
        return baseXp + bonusXp
    }
}

/**
 * Actions that earn XP.
 */
enum class XpAction {
    CATEGORIZE_TRANSACTION,
    CATEGORIZE_SAME_DAY,
    CONFIRM_FUZZY_MATCH,
    REJECT_FUZZY_MATCH,
    ADD_MANUAL_TRANSACTION,
    SET_BUDGET,
    STAY_UNDER_BUDGET_WEEK,
    REVIEW_WEEKLY_SUMMARY,
    FIRST_TRANSACTION_OF_DAY,
    ADD_CREDIT_CARD,
    IDENTIFY_RECURRING,
    COMPLETE_ONBOARDING
}
