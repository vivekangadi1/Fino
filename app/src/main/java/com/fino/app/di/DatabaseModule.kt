package com.fino.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fino.app.data.local.dao.*
import com.fino.app.data.local.database.FinoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): FinoDatabase {
        return Room.databaseBuilder(
            context,
            FinoDatabase::class.java,
            FinoDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed all default data on database creation
                    seedDefaultData(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Ensure categories exist on every database open
                    // This handles cases where database exists but was never seeded
                    seedCategoriesIfEmpty(db)
                    seedUserStatsIfEmpty(db)
                    seedAchievementsIfEmpty(db)
                }
            })
            .build()
    }

    private fun seedDefaultData(db: SupportSQLiteDatabase) {
        seedCategories(db)
        seedUserStats(db)
        seedAchievements(db)
    }

    private fun seedCategoriesIfEmpty(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT COUNT(*) FROM categories")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            seedCategories(db)
        }
    }

    private fun seedUserStatsIfEmpty(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT COUNT(*) FROM user_stats")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            seedUserStats(db)
        }
    }

    private fun seedAchievementsIfEmpty(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT COUNT(*) FROM achievements")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            seedAchievements(db)
        }
    }

    private fun seedCategories(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (1, 'Food', '🍔', NULL, 1, NULL, 1, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (2, 'Transport', '🚗', NULL, 1, NULL, 2, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (3, 'Shopping', '🛍️', NULL, 1, NULL, 3, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (4, 'Health', '💊', NULL, 1, NULL, 4, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (5, 'Entertainment', '🎬', NULL, 1, NULL, 5, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (6, 'Bills', '📱', NULL, 1, NULL, 6, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (7, 'Education', '📚', NULL, 1, NULL, 7, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (8, 'Travel', '✈️', NULL, 1, NULL, 8, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (9, 'Groceries', '🛒', NULL, 1, NULL, 9, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (10, 'Personal', '💅', NULL, 1, NULL, 10, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (11, 'Salary', '💰', NULL, 1, NULL, 11, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (12, 'Pet', '🐾', NULL, 1, NULL, 12, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (13, 'Insurance', '🛡️', NULL, 1, NULL, 13, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (14, 'Investments', '📈', NULL, 1, NULL, 14, 1)")
        db.execSQL("INSERT OR IGNORE INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (15, 'Other', '📦', NULL, 1, NULL, 15, 1)")
    }

    private fun seedUserStats(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO user_stats (id, currentStreak, longestStreak, totalTransactionsLogged, totalXp, currentLevel, lastActiveDate, createdAt) VALUES (1, 0, 0, 0, 0, 1, NULL, ${System.currentTimeMillis()})")
    }

    private fun seedAchievements(db: SupportSQLiteDatabase) {
        // AchievementEntity schema: id (String), name, description, emoji, xpReward, requirement, type, unlockedAt, progress
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('txn_1', 'First Steps', 'Log your first transaction', '🎯', 10, 1, 'TRANSACTION_COUNT', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('txn_10', 'Getting Started', 'Log 10 transactions', '📝', 25, 10, 'TRANSACTION_COUNT', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('streak_3', 'Streak Starter', 'Maintain a 3-day streak', '🔥', 15, 3, 'STREAK', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('streak_7', 'Week Warrior', 'Maintain a 7-day streak', '💪', 50, 7, 'STREAK', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('budget_1', 'Budget Planner', 'Create your first budget', '📊', 20, 1, 'BUDGET', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('card_1', 'Card Collector', 'Add your first credit card', '💳', 15, 1, 'CREDIT_CARD', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('txn_100', 'Century Club', 'Log 100 transactions', '💯', 100, 100, 'TRANSACTION_COUNT', NULL, 0)")
        db.execSQL("INSERT OR IGNORE INTO achievements (id, name, description, emoji, xpReward, requirement, type, unlockedAt, progress) VALUES ('streak_30', 'Month Master', 'Maintain a 30-day streak', '🏆', 200, 30, 'STREAK', NULL, 0)")
    }

    @Provides
    fun provideTransactionDao(database: FinoDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: FinoDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideMerchantMappingDao(database: FinoDatabase): MerchantMappingDao {
        return database.merchantMappingDao()
    }

    @Provides
    fun provideCreditCardDao(database: FinoDatabase): CreditCardDao {
        return database.creditCardDao()
    }

    @Provides
    fun provideRecurringRuleDao(database: FinoDatabase): RecurringRuleDao {
        return database.recurringRuleDao()
    }

    @Provides
    fun provideBudgetDao(database: FinoDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideUserStatsDao(database: FinoDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    @Provides
    fun provideAchievementDao(database: FinoDatabase): AchievementDao {
        return database.achievementDao()
    }
}
