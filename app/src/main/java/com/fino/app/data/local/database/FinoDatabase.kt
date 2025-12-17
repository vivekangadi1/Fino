package com.fino.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fino.app.data.local.dao.*
import com.fino.app.data.local.entity.*

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        MerchantMappingEntity::class,
        CreditCardEntity::class,
        RecurringRuleEntity::class,
        BudgetEntity::class,
        UserStatsEntity::class,
        AchievementEntity::class
    ],
    version = 3,  // Bumped to 3: removed FK constraints, using soft references
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinoDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun merchantMappingDao(): MerchantMappingDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun budgetDao(): BudgetDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val DATABASE_NAME = "fino_database"
    }
}
