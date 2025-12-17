package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getById(id: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND yearMonth = :yearMonth LIMIT 1")
    suspend fun getBudgetForCategoryAndMonth(categoryId: Long, yearMonth: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    fun getBudgetsForMonthFlow(yearMonth: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth")
    suspend fun getBudgetsForMonth(yearMonth: String): List<BudgetEntity>

    @Query("SELECT SUM(monthlyLimit) FROM budgets WHERE yearMonth = :yearMonth")
    suspend fun getTotalBudgetForMonth(yearMonth: String): Double?

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId ORDER BY yearMonth DESC LIMIT 1")
    suspend fun getLatestBudgetForCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT COUNT(*) FROM budgets WHERE yearMonth = :yearMonth")
    suspend fun getBudgetCountForMonth(yearMonth: String): Int
}
