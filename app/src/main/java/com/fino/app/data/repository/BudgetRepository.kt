package com.fino.app.data.repository

import com.fino.app.data.local.dao.BudgetDao
import com.fino.app.data.local.entity.BudgetEntity
import com.fino.app.domain.model.Budget
import com.fino.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val dao: BudgetDao
) {

    fun getBudgetsForMonthFlow(yearMonth: YearMonth): Flow<List<Budget>> {
        val monthStr = DateUtils.yearMonthToString(yearMonth)
        return dao.getBudgetsForMonthFlow(monthStr).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getBudgetsForMonth(yearMonth: YearMonth): List<Budget> {
        val monthStr = DateUtils.yearMonthToString(yearMonth)
        return dao.getBudgetsForMonth(monthStr).map { it.toDomain() }
    }

    suspend fun getBudgetForCategory(categoryId: Long, yearMonth: YearMonth = YearMonth.now()): Budget? {
        val monthStr = DateUtils.yearMonthToString(yearMonth)
        return dao.getBudgetForCategoryAndMonth(categoryId, monthStr)?.toDomain()
    }

    suspend fun getMonthlyBudget(yearMonth: YearMonth = YearMonth.now()): Double {
        val monthStr = DateUtils.yearMonthToString(yearMonth)
        return dao.getTotalBudgetForMonth(monthStr) ?: 0.0
    }

    suspend fun insert(budget: Budget): Long {
        return dao.insert(budget.toEntity())
    }

    suspend fun update(budget: Budget) {
        dao.update(budget.toEntity())
    }

    suspend fun delete(budget: Budget) {
        dao.delete(budget.toEntity())
    }

    suspend fun getLatestBudgetForCategory(categoryId: Long): Budget? {
        return dao.getLatestBudgetForCategory(categoryId)?.toDomain()
    }

    private fun BudgetEntity.toDomain(): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            monthlyLimit = monthlyLimit,
            month = DateUtils.stringToYearMonth(yearMonth),
            alertAt75 = alertAt75,
            alertAt100 = alertAt100,
            createdAt = DateUtils.fromEpochMillis(createdAt)
        )
    }

    private fun Budget.toEntity(): BudgetEntity {
        return BudgetEntity(
            id = id,
            categoryId = categoryId,
            monthlyLimit = monthlyLimit,
            yearMonth = DateUtils.yearMonthToString(month),
            alertAt75 = alertAt75,
            alertAt100 = alertAt100,
            createdAt = DateUtils.toEpochMillis(createdAt)
        )
    }
}
