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
                    // Seed default data synchronously on database creation

                    // Seed default categories
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (1, 'Food', '🍔', NULL, 1, NULL, 1, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (2, 'Transport', '🚗', NULL, 1, NULL, 2, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (3, 'Shopping', '🛍️', NULL, 1, NULL, 3, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (4, 'Health', '💊', NULL, 1, NULL, 4, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (5, 'Entertainment', '🎬', NULL, 1, NULL, 5, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (6, 'Bills', '📱', NULL, 1, NULL, 6, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (7, 'Education', '📚', NULL, 1, NULL, 7, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (8, 'Travel', '✈️', NULL, 1, NULL, 8, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (9, 'Groceries', '🛒', NULL, 1, NULL, 9, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (10, 'Personal', '💅', NULL, 1, NULL, 10, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (11, 'Salary', '💰', NULL, 1, NULL, 11, 1)")
                    db.execSQL("INSERT INTO categories (id, name, emoji, parentId, isSystem, budgetLimit, sortOrder, isActive) VALUES (12, 'Other', '📦', NULL, 1, NULL, 12, 1)")

                    // Seed default user stats
                    db.execSQL("INSERT INTO user_stats (id, currentStreak, longestStreak, totalTransactionsLogged, totalXp, currentLevel, lastActiveDate, createdAt) VALUES (1, 0, 0, 0, 0, 1, NULL, ${System.currentTimeMillis()})")

                    // Seed default achievements
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (1, 'First Steps', 'Log your first transaction', '🎯', 1, 0, 0, NULL, 'transactions')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (2, 'Getting Started', 'Log 10 transactions', '📝', 10, 0, 0, NULL, 'transactions')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (3, 'Streak Starter', 'Maintain a 3-day streak', '🔥', 3, 0, 0, NULL, 'streaks')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (4, 'Week Warrior', 'Maintain a 7-day streak', '💪', 7, 0, 0, NULL, 'streaks')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (5, 'Budget Planner', 'Create your first budget', '📊', 1, 0, 0, NULL, 'budgets')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (6, 'Card Collector', 'Add your first credit card', '💳', 1, 0, 0, NULL, 'cards')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (7, 'Century Club', 'Log 100 transactions', '💯', 100, 0, 0, NULL, 'transactions')")
                    db.execSQL("INSERT INTO achievements (id, name, description, iconEmoji, requiredProgress, currentProgress, isUnlocked, unlockedAt, category) VALUES (8, 'Month Master', 'Maintain a 30-day streak', '🏆', 30, 0, 0, NULL, 'streaks')")
                }
            })
            .build()
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
