package com.fino.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        AchievementEntity::class,
        EventEntity::class,
        EventTypeEntity::class,
        EventSubCategoryEntity::class,
        EventVendorEntity::class,
        FamilyMemberEntity::class,
        EMIEntity::class,
        LoanEntity::class,
        PatternSuggestionEntity::class
    ],
    version = 12,  // Bumped to 12: added credit card payment tracking and user override fields
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
    abstract fun eventDao(): EventDao
    abstract fun eventTypeDao(): EventTypeDao
    abstract fun eventSubCategoryDao(): EventSubCategoryDao
    abstract fun eventVendorDao(): EventVendorDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun emiDao(): EMIDao
    abstract fun loanDao(): LoanDao
    abstract fun patternSuggestionDao(): PatternSuggestionDao

    companion object {
        const val DATABASE_NAME = "fino_database"

        /**
         * Migration from version 11 to 12:
         * Adds credit card payment tracking and user override fields
         */
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Payment tracking fields
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN paidDate INTEGER")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN paidAmount REAL")
                // User override fields for manual adjustments
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN userAdjustedDue REAL")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN userAdjustedDueDate INTEGER")
            }
        }
    }
}
