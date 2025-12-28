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

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add excludeFromMainTotals column to events table
            db.execSQL("ALTER TABLE events ADD COLUMN excludeFromMainTotals INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create EMI table - schema must match EMIEntity exactly (no DEFAULT values, no indices)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS emis (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    creditCardId INTEGER,
                    description TEXT NOT NULL,
                    merchantName TEXT,
                    originalAmount REAL NOT NULL,
                    monthlyAmount REAL NOT NULL,
                    tenure INTEGER NOT NULL,
                    paidCount INTEGER NOT NULL,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER NOT NULL,
                    nextDueDate INTEGER NOT NULL,
                    interestRate REAL,
                    processingFee REAL,
                    status TEXT NOT NULL,
                    notes TEXT,
                    createdAt INTEGER NOT NULL
                )
            """)

            // Create Loans table - schema must match LoanEntity exactly (no DEFAULT values, no indices)
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS loans (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL,
                    bankName TEXT NOT NULL,
                    accountNumber TEXT,
                    description TEXT NOT NULL,
                    principalAmount REAL NOT NULL,
                    interestRate REAL NOT NULL,
                    monthlyEMI REAL NOT NULL,
                    tenure INTEGER NOT NULL,
                    paidCount INTEGER NOT NULL,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER NOT NULL,
                    nextDueDate INTEGER NOT NULL,
                    outstandingPrincipal REAL,
                    status TEXT NOT NULL,
                    notes TEXT,
                    createdAt INTEGER NOT NULL
                )
            """)
        }
    }

    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create pattern_suggestions table for automatic recurring bill detection
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS pattern_suggestions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    merchantPattern TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    averageAmount REAL NOT NULL,
                    frequency TEXT NOT NULL,
                    typicalDayOfPeriod INTEGER NOT NULL,
                    occurrenceCount INTEGER NOT NULL,
                    confidence REAL NOT NULL,
                    nextExpected INTEGER NOT NULL,
                    categoryId INTEGER,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    source TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    dismissedAt INTEGER
                )
            """)
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pattern_suggestions_merchantPattern ON pattern_suggestions(merchantPattern)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_pattern_suggestions_status ON pattern_suggestions(status)")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create event_sub_categories table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS event_sub_categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    emoji TEXT NOT NULL DEFAULT '📦',
                    budgetAmount REAL,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_sub_categories_eventId ON event_sub_categories(eventId)")

            // Create event_vendors table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS event_vendors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    eventId INTEGER NOT NULL,
                    subCategoryId INTEGER,
                    name TEXT NOT NULL,
                    description TEXT,
                    phone TEXT,
                    email TEXT,
                    quotedAmount REAL,
                    notes TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE,
                    FOREIGN KEY(subCategoryId) REFERENCES event_sub_categories(id) ON DELETE SET NULL
                )
            """)
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_vendors_eventId ON event_vendors(eventId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_event_vendors_subCategoryId ON event_vendors(subCategoryId)")

            // Create family_members table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS family_members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    relationship TEXT,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL
                )
            """)

            // Add new columns to transactions table
            db.execSQL("ALTER TABLE transactions ADD COLUMN eventSubCategoryId INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE transactions ADD COLUMN eventVendorId INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE transactions ADD COLUMN paidBy TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE transactions ADD COLUMN isAdvancePayment INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE transactions ADD COLUMN dueDate INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE transactions ADD COLUMN expenseNotes TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE transactions ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT 'PAID'")

            // Create indexes for new transaction columns
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_eventSubCategoryId ON transactions(eventSubCategoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_eventVendorId ON transactions(eventVendorId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_paymentStatus ON transactions(paymentStatus)")
        }
    }

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
            .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, FinoDatabase.MIGRATION_11_12)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed all default data on database creation
                    seedDefaultData(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Ensure all required data exists on every database open
                    // This handles cases where database exists but was never seeded
                    seedCategoriesIfEmpty(db)
                    seedUserStatsIfEmpty(db)
                    seedAchievementsIfEmpty(db)
                    seedMerchantMappingsIfEmpty(db)
                    seedEventTypesIfEmpty(db)
                    seedFamilyMembersIfEmpty(db)
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

    private fun seedEventTypesIfEmpty(db: SupportSQLiteDatabase) {
        try {
            val cursor = db.query("SELECT COUNT(*) FROM event_types")
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()

            if (count == 0) {
                seedEventTypes(db)
            }
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to check/seed event types", e)
        }
    }

    private fun seedEventTypes(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (1, 'Trip', '✈️', 1, 1, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (2, 'Wedding', '💒', 1, 2, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (3, 'Renovation', '🏠', 1, 3, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (4, 'Party', '🎉', 1, 4, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (5, 'Festival', '🪔', 1, 5, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (6, 'Medical', '🏥', 1, 6, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (7, 'Education', '🎓', 1, 7, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (8, 'Shopping Spree', '🛍️', 1, 8, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO event_types (id, name, emoji, isSystem, sortOrder, isActive, createdAt) VALUES (9, 'Other', '📌', 1, 99, 1, $now)")
        Log.d("DatabaseModule", "Seeded default event types")
    }

    private fun seedFamilyMembersIfEmpty(db: SupportSQLiteDatabase) {
        try {
            val cursor = db.query("SELECT COUNT(*) FROM family_members")
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()

            if (count == 0) {
                seedFamilyMembers(db)
            }
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to check/seed family members", e)
        }
    }

    private fun seedFamilyMembers(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()
        // Seed default family members - "Self" is marked as default
        db.execSQL("INSERT OR IGNORE INTO family_members (id, name, relationship, isDefault, sortOrder, createdAt) VALUES (1, 'Self', 'Self', 1, 1, $now)")
        db.execSQL("INSERT OR IGNORE INTO family_members (id, name, relationship, isDefault, sortOrder, createdAt) VALUES (2, 'Spouse', 'Spouse', 0, 2, $now)")
        db.execSQL("INSERT OR IGNORE INTO family_members (id, name, relationship, isDefault, sortOrder, createdAt) VALUES (3, 'Father', 'Father', 0, 3, $now)")
        db.execSQL("INSERT OR IGNORE INTO family_members (id, name, relationship, isDefault, sortOrder, createdAt) VALUES (4, 'Mother', 'Mother', 0, 4, $now)")
        Log.d("DatabaseModule", "Seeded default family members")
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

    private fun seedMerchantMappings(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        db.beginTransaction()
        try {
            // Category 1: Food & Delivery
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SWIGGY', 'Swiggy', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('ZOMATO', 'Zomato', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('DOMINOS', 'Dominos Pizza', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('PIZZAHUT', 'Pizza Hut', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('MCDONALD', 'McDonalds', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('KFC', 'KFC', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BURGERKING', 'Burger King', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SUBWAY', 'Subway', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('STARBUCKS', 'Starbucks', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('CCD', 'Cafe Coffee Day', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('DUNKINDONUTS', 'Dunkin Donuts', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BIKANERVALA', 'Bikanervala', 1, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('HALDIRAM', 'Haldirams', 1, 1.0, 0, 0, $now, $now)")

            // Category 2: Transport
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('UBER', 'Uber', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('OLA', 'Ola Cabs', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('RAPIDO', 'Rapido', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BOUNCE', 'Bounce', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('YULU', 'Yulu', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('METRO', 'Metro Card', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('IRCTC', 'IRCTC', 2, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('REDBUS', 'RedBus', 2, 1.0, 0, 0, $now, $now)")

            // Category 3: Shopping
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('AMAZON', 'Amazon', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('FLIPKART', 'Flipkart', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('MYNTRA', 'Myntra', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('AJIO', 'Ajio', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('NYKAA', 'Nykaa', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('MEESHO', 'Meesho', 3, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SNAPDEAL', 'Snapdeal', 3, 1.0, 0, 0, $now, $now)")

            // Category 5: Entertainment
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('NETFLIX', 'Netflix', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('AMAZONPRIME', 'Amazon Prime', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('HOTSTAR', 'Disney+ Hotstar', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SPOTIFY', 'Spotify', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('YOUTUBEPREMIUM', 'YouTube Premium', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BOOKMYSHOW', 'BookMyShow', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('PVRCINEMAS', 'PVR Cinemas', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SONYLIV', 'SonyLIV', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('ZEE', 'Zee5', 5, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('STEAM', 'Steam', 5, 1.0, 0, 0, $now, $now)")

            // Category 6: Bills & Utilities
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('JIO', 'Jio', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('AIRTEL', 'Airtel', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('VI', 'Vi (Vodafone Idea)', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('VODAFONE', 'Vodafone', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BSNL', 'BSNL', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('TATASKY', 'Tata Sky', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('DISHSTV', 'Dish TV', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('ELECTRICITY', 'Electricity Bill', 6, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BROADBAND', 'Broadband', 6, 1.0, 0, 0, $now, $now)")

            // Category 9: Groceries
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BIGBASKET', 'BigBasket', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('BLINKIT', 'Blinkit', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('GROFERS', 'Grofers', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('ZEPTO', 'Zepto', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('DUNZO', 'Dunzo', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('DMART', 'DMart', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('JIOMART', 'JioMart', 9, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('INSTAMART', 'Instamart', 9, 1.0, 0, 0, $now, $now)")

            // Category 8: Travel
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('MAKEMYTRIP', 'MakeMyTrip', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('GOIBIBO', 'Goibibo', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('CLEARTRIP', 'Cleartrip', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('INDIGO', 'IndiGo', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('SPICEJET', 'SpiceJet', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('AIRINDIA', 'Air India', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('VISTARA', 'Vistara', 8, 1.0, 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO merchant_mappings (rawMerchantName, normalizedName, categoryId, confidence, matchCount, isFuzzyMatch, createdAt, lastUsedAt) VALUES ('OYO', 'OYO Rooms', 8, 1.0, 0, 0, $now, $now)")

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseModule", "Failed to seed merchant mappings", e)
        } finally {
            db.endTransaction()
        }
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

    @Provides
    fun provideEventSubCategoryDao(database: FinoDatabase): EventSubCategoryDao = database.eventSubCategoryDao()

    @Provides
    fun provideEventVendorDao(database: FinoDatabase): EventVendorDao = database.eventVendorDao()

    @Provides
    fun provideFamilyMemberDao(database: FinoDatabase): FamilyMemberDao = database.familyMemberDao()

    @Provides
    fun provideEMIDao(database: FinoDatabase): EMIDao = database.emiDao()

    @Provides
    fun provideLoanDao(database: FinoDatabase): LoanDao = database.loanDao()

    @Provides
    fun providePatternSuggestionDao(database: FinoDatabase): PatternSuggestionDao = database.patternSuggestionDao()
}
