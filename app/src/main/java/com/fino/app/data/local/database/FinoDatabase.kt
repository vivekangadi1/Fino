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
        PatternSuggestionEntity::class,
        AccountEntity::class,
        BillEntity::class,
        EventMemberEntity::class,
        CashbackRewardEntity::class,
        NoticesEntity::class
    ],
    version = 13,
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
    abstract fun accountDao(): AccountDao
    abstract fun billDao(): BillDao
    abstract fun eventMemberDao(): EventMemberDao
    abstract fun cashbackRewardDao(): CashbackRewardDao
    abstract fun noticesDao(): NoticesDao

    companion object {
        const val DATABASE_NAME = "fino_database"

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN paidDate INTEGER")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN paidAmount REAL")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN userAdjustedDue REAL")
                database.execSQL("ALTER TABLE credit_cards ADD COLUMN userAdjustedDueDate INTEGER")
            }
        }

        /**
         * Migration 12 → 13: Account/Bill/EventMember/CashbackReward/Notices tables,
         * transactions.accountId, events.autoTagTransactions; backfills accounts from
         * distinct payment signatures and seeds bills from existing credit cards.
         */
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()

                // accounts
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL,
                        institution TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        maskedNumber TEXT,
                        paymentMethod TEXT,
                        balance REAL,
                        currency TEXT NOT NULL DEFAULT 'INR',
                        syncSource TEXT NOT NULL DEFAULT 'SMS',
                        lastSyncedAt INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_type ON accounts(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_institution ON accounts(institution)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_paymentMethod ON accounts(paymentMethod)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_accounts_paymentMethod_institution_maskedNumber " +
                        "ON accounts(paymentMethod, institution, maskedNumber)"
                )

                // bills
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS bills (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        accountId INTEGER,
                        creditCardId INTEGER,
                        cycleStart INTEGER NOT NULL,
                        cycleEnd INTEGER NOT NULL,
                        dueDate INTEGER NOT NULL,
                        totalDue REAL NOT NULL,
                        minDue REAL,
                        paidAt INTEGER,
                        paidAmount REAL,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        source TEXT NOT NULL DEFAULT 'CC_STATEMENT',
                        payeeVpa TEXT,
                        payeeName TEXT,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bills_accountId ON bills(accountId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bills_creditCardId ON bills(creditCardId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bills_dueDate ON bills(dueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_bills_status ON bills(status)")

                // event_members (with FK cascade to events)
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS event_members (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        eventId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        avatarSeed TEXT NOT NULL,
                        sharePercent REAL NOT NULL DEFAULT 0,
                        isPayer INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(eventId) REFERENCES events(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_event_members_eventId ON event_members(eventId)")

                // cashback_rewards
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cashback_rewards (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        accountId INTEGER,
                        amount REAL NOT NULL,
                        period TEXT NOT NULL,
                        creditedAt INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        description TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cashback_rewards_period ON cashback_rewards(period)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_cashback_rewards_accountId ON cashback_rewards(accountId)")

                // notices
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notices (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        period TEXT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        isWarn INTEGER NOT NULL DEFAULT 0,
                        routeJson TEXT,
                        chartDataJson TEXT,
                        rankOrder INTEGER NOT NULL DEFAULT 0,
                        computedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notices_period ON notices(period)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notices_period_rankOrder ON notices(period, rankOrder)")

                // Columns on existing tables
                database.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions(accountId)")
                database.execSQL("ALTER TABLE events ADD COLUMN autoTagTransactions INTEGER NOT NULL DEFAULT 0")

                // Backfill accounts from distinct (paymentMethod, bankName, cardLastFour)
                database.execSQL(
                    """
                    INSERT INTO accounts (type, institution, displayName, maskedNumber, paymentMethod, syncSource, createdAt)
                    SELECT DISTINCT
                        CASE
                            WHEN paymentMethod = 'CREDIT_CARD' THEN 'CARD'
                            WHEN paymentMethod = 'UPI' THEN 'BANK'
                            WHEN paymentMethod = 'WALLET' THEN 'WALLET'
                            ELSE 'BANK'
                        END AS type,
                        COALESCE(bankName, 'Unknown') AS institution,
                        CASE
                            WHEN cardLastFour IS NOT NULL THEN COALESCE(bankName, 'Card') || ' ••' || cardLastFour
                            WHEN bankName IS NOT NULL AND paymentMethod = 'UPI' THEN bankName || ' UPI'
                            ELSE COALESCE(bankName, 'Account')
                        END AS displayName,
                        cardLastFour AS maskedNumber,
                        paymentMethod,
                        'SMS' AS syncSource,
                        $now AS createdAt
                    FROM transactions
                    WHERE paymentMethod IS NOT NULL
                      AND bankName IS NOT NULL
                    """.trimIndent()
                )

                // Backfill transaction.accountId using composite match
                database.execSQL(
                    """
                    UPDATE transactions
                    SET accountId = (
                        SELECT a.id FROM accounts a
                        WHERE a.paymentMethod = transactions.paymentMethod
                          AND a.institution = transactions.bankName
                          AND (
                            (a.maskedNumber IS NULL AND transactions.cardLastFour IS NULL)
                            OR a.maskedNumber = transactions.cardLastFour
                          )
                        LIMIT 1
                    )
                    WHERE paymentMethod IS NOT NULL AND bankName IS NOT NULL
                    """.trimIndent()
                )

                // Backfill bills from credit cards with active due balances
                database.execSQL(
                    """
                    INSERT INTO bills (
                        accountId, creditCardId, cycleStart, cycleEnd, dueDate,
                        totalDue, minDue, paidAt, paidAmount, status, source,
                        payeeVpa, payeeName, updatedAt
                    )
                    SELECT
                        (SELECT a.id FROM accounts a
                         WHERE a.paymentMethod = 'CREDIT_CARD'
                           AND a.institution = cc.bankName
                           AND a.maskedNumber = cc.lastFourDigits
                         LIMIT 1) AS accountId,
                        cc.id AS creditCardId,
                        COALESCE(cc.previousDueDate, $now) - (30 * 24 * 60 * 60 * 1000) AS cycleStart,
                        COALESCE(cc.previousDueDate, $now) AS cycleEnd,
                        COALESCE(cc.userAdjustedDueDate, cc.previousDueDate, $now) AS dueDate,
                        COALESCE(cc.userAdjustedDue, cc.previousDue) AS totalDue,
                        cc.minimumDue AS minDue,
                        cc.paidDate AS paidAt,
                        cc.paidAmount AS paidAmount,
                        CASE
                            WHEN cc.isPaid = 1 THEN 'PAID'
                            WHEN COALESCE(cc.previousDueDate, $now) < $now THEN 'OVERDUE'
                            ELSE 'PENDING'
                        END AS status,
                        'CC_STATEMENT' AS source,
                        NULL AS payeeVpa,
                        cc.bankName AS payeeName,
                        $now AS updatedAt
                    FROM credit_cards cc
                    WHERE cc.isActive = 1 AND (cc.previousDue > 0 OR cc.userAdjustedDue > 0)
                    """.trimIndent()
                )
            }
        }
    }
}
