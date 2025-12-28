package com.fino.app.data.local.dao

import androidx.room.*
import com.fino.app.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RecurringRuleEntity): Long

    @Update
    suspend fun update(rule: RecurringRuleEntity)

    @Delete
    suspend fun delete(rule: RecurringRuleEntity)

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getById(id: Long): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextExpected")
    fun getActiveRulesFlow(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextExpected")
    suspend fun getActiveRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE merchantPattern LIKE :pattern AND isActive = 1 LIMIT 1")
    suspend fun findByMerchantPattern(pattern: String): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE nextExpected BETWEEN :startDate AND :endDate AND isActive = 1")
    suspend fun getUpcomingRules(startDate: Long, endDate: Long): List<RecurringRuleEntity>

    @Query("UPDATE recurring_rules SET lastOccurrence = :date, nextExpected = :nextDate, occurrenceCount = occurrenceCount + 1 WHERE id = :id")
    suspend fun recordOccurrence(id: Long, date: Long, nextDate: Long)

    @Query("SELECT COUNT(*) FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRuleCount(): Int

    @Query("UPDATE recurring_rules SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)
}
