package com.fino.app.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create event_types table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS event_types (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    emoji TEXT NOT NULL,
                    isSystem INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_types_isSystem ON event_types(isSystem)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_types_isActive ON event_types(isActive)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_types_sortOrder ON event_types(sortOrder)")

            // Seed system event types
            val now = System.currentTimeMillis()
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (1, 'Trip', '✈️', 1, 1, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (2, 'Wedding', '💒', 1, 2, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (3, 'Renovation', '🏠', 1, 3, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (4, 'Party', '🎉', 1, 4, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (5, 'Festival', '🪔', 1, 5, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (6, 'Medical', '🏥', 1, 6, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (7, 'Education', '🎓', 1, 7, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (8, 'Shopping Spree', '🛍️', 1, 8, 1, $now)")
            db.execSQL("INSERT INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (9, 'Other', '📌', 1, 99, 1, $now)")

            // Create events table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    description TEXT,
                    emoji TEXT NOT NULL,
                    eventTypeId INTEGER NOT NULL,
                    budgetAmount REAL,
                    alertAt75 INTEGER NOT NULL DEFAULT 1,
                    alertAt100 INTEGER NOT NULL DEFAULT 1,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_eventTypeId ON events(eventTypeId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_status ON events(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_startDate ON events(startDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_endDate ON events(endDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_events_isActive ON events(isActive)")

            // Add eventId column to transactions
            db.execSQL("ALTER TABLE transactions ADD COLUMN eventId INTEGER DEFAULT NULL")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_eventId ON transactions(eventId)")
        }
    }

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
            .addMigrations(MIGRATION_6_7)
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
                    seedMerchantMappingsIfEmpty(db)
                }
            })
            .build()
    }

    private fun seedDefaultData(db: SupportSQLiteDatabase) {
        seedCategories(db)
        seedUserStats(db)
        seedAchievements(db)
        seedMerchantMappings(db)
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

    private fun seedMerchantMappingsIfEmpty(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT COUNT(*) FROM merchant_mappings")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            seedMerchantMappings(db)
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

    @Provides
    fun provideEventDao(database: FinoDatabase): EventDao = database.eventDao()

    @Provides
    fun provideEventTypeDao(database: FinoDatabase): EventTypeDao = database.eventTypeDao()
}
