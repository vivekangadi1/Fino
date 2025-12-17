# TDD Development Prompt: Fino - Smart Expense Tracker

<project_overview>
## Project Summary

**App Name:** Fino  
**Tagline:** "Track Smart. Spend Wise."  
**Platform:** Android (Kotlin Native)  
**Distribution:** Sideload/APK (not Play Store)

Fino is an intelligent expense tracking app that automatically reads SMS transaction messages, learns user categorization patterns, tracks credit card bills, detects recurring subscriptions, and provides gamified budget management—all while keeping data completely local and private.

This prompt follows a strict Test-Driven Development (TDD) methodology. Execute each phase sequentially, completing all outputs before moving to the next phase. Do not skip phases or combine them.
</project_overview>

---

<context_and_motivation>
## Why This Methodology Matters

This TDD approach ensures:
1. **Quality**: Tests define expected behavior before code exists, catching bugs early
2. **Confidence**: Passing tests prove the implementation works correctly
3. **Documentation**: Tests serve as living documentation of system behavior
4. **Maintainability**: Refactoring is safe when tests verify nothing breaks
5. **Incremental Progress**: Small, testable increments reduce risk and complexity

The structured phases prevent common pitfalls like writing untestable code, missing edge cases, or breaking existing functionality.
</context_and_motivation>

---

<autonomous_operation_rules>
## Autonomous Operation Rules

Follow these rules throughout all phases:

### DO:
- ✅ Always start with Phase 0 discovery/analysis before any code
- ✅ Always write tests BEFORE implementation code
- ✅ Always run tests after EACH change to verify status
- ✅ Always use REAL data in tests (actual SMS formats, realistic amounts)
- ✅ Always fix ONE issue at a time, not multiple
- ✅ Always verify no regressions after changes
- ✅ Always document decisions, assumptions, and changes
- ✅ Always create the specified output files for each phase
- ✅ Make reasonable assumptions when information is unclear, then document them
- ✅ Keep working through failures until resolved

### DO NOT:
- ❌ Write implementation code before tests exist
- ❌ Skip the discovery/analysis phase
- ❌ Use oversimplified test data that doesn't match production reality
- ❌ Change multiple things at once (isolate changes)
- ❌ Skip running tests after changes
- ❌ Leave failing tests unresolved before moving on
- ❌ Break existing functionality to add new features
- ❌ Skip documentation
- ❌ Hard-code values to pass specific tests instead of implementing general solutions
- ❌ Create unnecessary abstractions or over-engineer solutions
</autonomous_operation_rules>

---

<decision_framework>
## Decision Framework

Use this framework when you encounter blockers or uncertainty:

### When Stuck:
1. Document the blocker clearly in progress.txt
2. Search existing code for similar patterns
3. Make a reasonable assumption based on context
4. Implement based on the assumption
5. Test it
6. If it works → Move on, document the assumption made
7. If it fails → Try a different approach, document what didn't work

### When Tests Fail:
1. Read the error message carefully and completely
2. Identify root cause: Is the test wrong, or is the implementation wrong?
3. Fix the actual problem (don't patch symptoms)
4. Re-run the specific test
5. If still failing → Try a different approach
6. Document what didn't work and why

### When Choosing Between Approaches:
1. Prefer minimal changes over rewrites
2. Prefer backward-compatible solutions
3. Follow existing patterns and conventions in the codebase
4. Choose testable solutions over clever ones
5. Consider long-term maintainability
</decision_framework>

---

<state_tracking>
## State Tracking Files

Maintain these files throughout development to track progress across context windows:

### tests.json (Structured Test Status)
```json
{
  "last_updated": "2024-12-15T10:30:00Z",
  "current_phase": "PHASE_2_TESTS",
  "summary": {
    "total": 0,
    "passing": 0,
    "failing": 0,
    "not_started": 0
  },
  "test_suites": [
    {
      "name": "SmsParserTest",
      "file": "test/java/com/fino/app/service/parser/SmsParserTest.kt",
      "tests": [
        {"name": "parse_HDFC_UPI_debit_SMS_extracts_correct_amount", "status": "not_started"},
        {"name": "parse_HDFC_UPI_debit_SMS_extracts_merchant_name", "status": "not_started"}
      ]
    }
  ]
}
```

### progress.txt (Freeform Progress Notes)
```text
# Fino Development Progress

## Current Session
- Date: 2024-12-15
- Phase: 2 (Tests First - RED)
- Focus: SMS Parser tests

## Completed
- [x] Phase 0: Discovery complete
- [x] Phase 1: Planning complete
- [ ] Phase 2: Tests (in progress)

## Current Task
Writing SMS parser tests for UPI transaction format

## Next Steps
1. Complete credit card transaction parser tests
2. Complete bill parser tests
3. Run all tests to confirm they FAIL

## Blockers
None currently

## Assumptions Made
- Assuming all amounts in SMS are in INR (₹)
- Assuming date formats: DD-MM-YY, DD-MM-YYYY, DD-Mon-YY

## Decisions
- Using regex-based parsing as primary approach
- ML Kit for fallback on unrecognized formats
```

Update these files after completing significant work. This enables seamless continuation if context window refreshes.
</state_tracking>

---

<templates>
## Required Templates

Use these templates for documentation throughout the project:

### Problem Analysis Template
```markdown
# Problem Analysis: [Title]

## Problem Statement
[What is the issue or feature requirement?]

## Current State
[What exists now? What's the starting point?]

## Expected State
[What should the end result look like?]

## Root Cause Analysis
[For bugs: Why is this happening? For features: What gaps exist?]

## Proposed Solution
[How will you solve this?]

## Files Affected
- [List all files that will be created or modified]

## Test Strategy
- [What tests will you write?]
- [What edge cases will you cover?]

## Success Criteria
- [ ] [Specific, measurable criterion 1]
- [ ] [Specific, measurable criterion 2]

## Risks
- [What could go wrong?]
- [How will you mitigate?]

## Rollback Plan
[How to undo if something breaks]
```

### Progress Tracking Template
```markdown
# Progress: [Task/Milestone Name]

## Status: [NOT STARTED | IN PROGRESS | BLOCKED | COMPLETE]

## Phases Completed
- [ ] Phase 0: Discovery
- [ ] Phase 1: Planning
- [ ] Phase 2: Tests (RED)
- [ ] Phase 3: Implementation (GREEN)
- [ ] Phase 4: Refactor
- [ ] Phase 5: Verification
- [ ] Phase 6: Documentation

## Test Results
- Total tests: X
- Passing: X
- Failing: X
- Coverage: X%

## Issues Encountered
[List any issues and how they were resolved]

## Decisions Made
[List decisions and rationale]

## Time Spent
[Actual time vs estimated]

## Completion Timestamp
[When was this completed?]
```

### Bug Fix Template
```markdown
# Bug Fix: [Error/Issue Title]

## Error Details
- Component: [Where does this occur?]
- Error Type: [Exception type, HTTP status, etc.]
- Error Message: [Exact error message]
- Frequency: [Always, sometimes, rare]

## Reproduction Steps
1. [Step 1]
2. [Step 2]
3. [Expected vs Actual behavior]

## Root Cause
[What is causing this bug?]

## Fix Applied
[What code changes were made?]

## Tests Added
- [Test 1: Description]
- [Test 2: Description]

## Verification
- [ ] Bug no longer occurs with reproduction steps
- [ ] New tests pass
- [ ] All existing tests still pass
- [ ] No regression in related functionality
- [ ] Manual testing confirms fix
```
</templates>

---

# PHASE 0: DISCOVERY & ANALYSIS

<phase_0_instructions>
## Task: Understand Before Acting

Before writing any code, thoroughly analyze and document the project requirements.

### Instructions:

1. **Create the project directory structure** as specified below
2. **Define all data models** with complete field specifications
3. **Document SMS message patterns** that the parser must handle
4. **Define the category hierarchy** with IDs, names, emojis, and relationships
5. **Specify the gamification system** including levels, achievements, and XP rules
6. **List all dependencies** needed in build.gradle.kts
7. **Document architecture decisions** and their rationale

### Project Structure to Create:

```
fino/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fino/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── TransactionDao.kt
│   │   │   │   │   │   │   ├── CategoryDao.kt
│   │   │   │   │   │   │   ├── MerchantMappingDao.kt
│   │   │   │   │   │   │   ├── CreditCardDao.kt
│   │   │   │   │   │   │   ├── RecurringRuleDao.kt
│   │   │   │   │   │   │   ├── BudgetDao.kt
│   │   │   │   │   │   │   ├── UserStatsDao.kt
│   │   │   │   │   │   │   └── AchievementDao.kt
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   ├── TransactionEntity.kt
│   │   │   │   │   │   │   ├── CategoryEntity.kt
│   │   │   │   │   │   │   ├── MerchantMappingEntity.kt
│   │   │   │   │   │   │   ├── CreditCardEntity.kt
│   │   │   │   │   │   │   ├── RecurringRuleEntity.kt
│   │   │   │   │   │   │   ├── BudgetEntity.kt
│   │   │   │   │   │   │   ├── UserStatsEntity.kt
│   │   │   │   │   │   │   └── AchievementEntity.kt
│   │   │   │   │   │   └── database/
│   │   │   │   │   │       └── FinoDatabase.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── TransactionRepository.kt
│   │   │   │   │   │   ├── CategoryRepository.kt
│   │   │   │   │   │   ├── MerchantMappingRepository.kt
│   │   │   │   │   │   ├── CreditCardRepository.kt
│   │   │   │   │   │   ├── RecurringRuleRepository.kt
│   │   │   │   │   │   ├── BudgetRepository.kt
│   │   │   │   │   │   └── GamificationRepository.kt
│   │   │   │   │   └── preferences/
│   │   │   │   │       └── FinoPreferences.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   │   ├── Category.kt
│   │   │   │   │   │   ├── MerchantMapping.kt
│   │   │   │   │   │   ├── CreditCard.kt
│   │   │   │   │   │   ├── RecurringRule.kt
│   │   │   │   │   │   ├── Budget.kt
│   │   │   │   │   │   ├── UserStats.kt
│   │   │   │   │   │   ├── Achievement.kt
│   │   │   │   │   │   └── Enums.kt
│   │   │   │   │   ├── usecase/
│   │   │   │   │   │   ├── CategorizeTransactionUseCase.kt
│   │   │   │   │   │   ├── CalculateBudgetUseCase.kt
│   │   │   │   │   │   ├── DetectRecurringUseCase.kt
│   │   │   │   │   │   ├── CalculateCreditCardDueUseCase.kt
│   │   │   │   │   │   ├── ProcessSmsUseCase.kt
│   │   │   │   │   │   └── GamificationUseCase.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── [Repository interfaces]
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   └── HomeComponents.kt
│   │   │   │   │   │   ├── cards/
│   │   │   │   │   │   │   ├── CardsScreen.kt
│   │   │   │   │   │   │   └── CardsComponents.kt
│   │   │   │   │   │   ├── analytics/
│   │   │   │   │   │   │   ├── AnalyticsScreen.kt
│   │   │   │   │   │   │   └── AnalyticsComponents.kt
│   │   │   │   │   │   ├── settings/
│   │   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   │   └── SettingsComponents.kt
│   │   │   │   │   │   ├── onboarding/
│   │   │   │   │   │   │   ├── OnboardingScreen.kt
│   │   │   │   │   │   │   └── OnboardingComponents.kt
│   │   │   │   │   │   ├── transaction/
│   │   │   │   │   │   │   ├── TransactionEntryScreen.kt
│   │   │   │   │   │   │   ├── TransactionListScreen.kt
│   │   │   │   │   │   │   └── CategoryPickerSheet.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── CommonComponents.kt
│   │   │   │   │   │       ├── AmountDisplay.kt
│   │   │   │   │   │       ├── ProgressIndicators.kt
│   │   │   │   │   │       └── GamificationComponents.kt
│   │   │   │   │   ├── viewmodel/
│   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   ├── CardsViewModel.kt
│   │   │   │   │   │   ├── AnalyticsViewModel.kt
│   │   │   │   │   │   ├── SettingsViewModel.kt
│   │   │   │   │   │   ├── TransactionViewModel.kt
│   │   │   │   │   │   └── OnboardingViewModel.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── FinoNavigation.kt
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── sms/
│   │   │   │   │   │   ├── SmsReceiver.kt
│   │   │   │   │   │   ├── SmsReaderService.kt
│   │   │   │   │   │   └── SmsPermissionHelper.kt
│   │   │   │   │   ├── parser/
│   │   │   │   │   │   ├── SmsParser.kt
│   │   │   │   │   │   ├── UpiTransactionParser.kt
│   │   │   │   │   │   ├── CreditCardTransactionParser.kt
│   │   │   │   │   │   ├── CreditCardBillParser.kt
│   │   │   │   │   │   ├── ParserFactory.kt
│   │   │   │   │   │   └── ParsedTransaction.kt
│   │   │   │   │   ├── notification/
│   │   │   │   │   │   └── NotificationService.kt
│   │   │   │   │   └── backup/
│   │   │   │   │       └── BackupService.kt
│   │   │   │   ├── ml/
│   │   │   │   │   ├── categorizer/
│   │   │   │   │   │   └── CategorySuggester.kt
│   │   │   │   │   └── matcher/
│   │   │   │   │       ├── MerchantMatcher.kt
│   │   │   │   │       └── FuzzyMatcher.kt
│   │   │   │   ├── gamification/
│   │   │   │   │   ├── XpCalculator.kt
│   │   │   │   │   ├── StreakTracker.kt
│   │   │   │   │   ├── AchievementTracker.kt
│   │   │   │   │   ├── LevelCalculator.kt
│   │   │   │   │   └── ChallengeManager.kt
│   │   │   │   ├── security/
│   │   │   │   │   ├── BiometricHelper.kt
│   │   │   │   │   └── EncryptionHelper.kt
│   │   │   │   ├── di/
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   ├── util/
│   │   │   │   │   ├── AmountFormatter.kt
│   │   │   │   │   ├── DateUtils.kt
│   │   │   │   │   └── Extensions.kt
│   │   │   │   └── FinoApplication.kt
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   │   └── java/com/fino/app/
│   │   │       ├── data/
│   │   │       │   ├── local/
│   │   │       │   │   ├── TransactionDaoTest.kt
│   │   │       │   │   ├── CategoryDaoTest.kt
│   │   │       │   │   ├── MerchantMappingDaoTest.kt
│   │   │       │   │   ├── CreditCardDaoTest.kt
│   │   │       │   │   └── FinoDatabaseTest.kt
│   │   │       │   └── repository/
│   │   │       │       ├── TransactionRepositoryTest.kt
│   │   │       │       └── CreditCardRepositoryTest.kt
│   │   │       ├── service/
│   │   │       │   └── parser/
│   │   │       │       ├── SmsParserTest.kt
│   │   │       │       ├── UpiTransactionParserTest.kt
│   │   │       │       ├── CreditCardTransactionParserTest.kt
│   │   │       │       ├── CreditCardBillParserTest.kt
│   │   │       │       └── RecurringDetectorTest.kt
│   │   │       ├── ml/
│   │   │       │   ├── MerchantMatcherTest.kt
│   │   │       │   ├── CategorySuggesterTest.kt
│   │   │       │   └── FuzzyMatcherTest.kt
│   │   │       ├── domain/
│   │   │       │   └── usecase/
│   │   │       │       ├── CategorizeTransactionUseCaseTest.kt
│   │   │       │       ├── CalculateBudgetUseCaseTest.kt
│   │   │       │       ├── DetectRecurringUseCaseTest.kt
│   │   │       │       └── CalculateCreditCardDueUseCaseTest.kt
│   │   │       ├── gamification/
│   │   │       │   ├── XpCalculatorTest.kt
│   │   │       │   ├── StreakTrackerTest.kt
│   │   │       │   ├── AchievementTrackerTest.kt
│   │   │       │   └── LevelCalculatorTest.kt
│   │   │       ├── presentation/
│   │   │       │   └── viewmodel/
│   │   │       │       ├── HomeViewModelTest.kt
│   │   │       │       ├── TransactionViewModelTest.kt
│   │   │       │       └── AnalyticsViewModelTest.kt
│   │   │       └── util/
│   │   │           └── AmountFormatterTest.kt
│   │   └── androidTest/
│   │       └── java/com/fino/app/
│   │           ├── DatabaseMigrationTest.kt
│   │           ├── FullFlowIntegrationTest.kt
│   │           └── ui/
│   │               ├── HomeScreenTest.kt
│   │               ├── TransactionEntryTest.kt
│   │               └── OnboardingFlowTest.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── tests.json
├── progress.txt
├── DISCOVERY.md
├── PLAN.md
└── README.md
```

### Data Models to Define:

Document each entity with all fields, types, constraints, and relationships:

```kotlin
// Transaction Entity - Core transaction record
data class Transaction(
    val id: Long = 0,
    val amount: Double,                    // Transaction amount in INR
    val type: TransactionType,             // DEBIT or CREDIT
    val merchantName: String,              // Raw merchant name from SMS
    val merchantNormalized: String? = null,// Cleaned/learned name
    val categoryId: Long? = null,          // FK to Category
    val subcategoryId: Long? = null,       // FK to Category (subcategory)
    val creditCardId: Long? = null,        // FK to CreditCard (null if not CC)
    val isRecurring: Boolean = false,      // Flagged as recurring
    val recurringRuleId: Long? = null,     // FK to RecurringRule
    val rawSmsBody: String? = null,        // Original SMS text
    val smsSender: String? = null,         // SMS sender address
    val parsedConfidence: Float = 0f,      // Parser confidence 0.0-1.0
    val needsReview: Boolean = true,       // Needs user categorization
    val transactionDate: LocalDateTime,    // When transaction occurred
    val createdAt: LocalDateTime,          // When record was created
    val source: TransactionSource          // SMS, MANUAL, EMAIL
)

enum class TransactionType { DEBIT, CREDIT }
enum class TransactionSource { SMS, MANUAL, EMAIL }

// Category Entity - Hierarchical categories
data class Category(
    val id: Long = 0,
    val name: String,                      // Display name
    val emoji: String,                     // Category icon
    val parentId: Long? = null,            // FK to parent Category (null if top-level)
    val isSystem: Boolean = true,          // System-defined vs user-created
    val budgetLimit: Double? = null,       // Optional monthly budget
    val sortOrder: Int = 0,                // Display order
    val isActive: Boolean = true           // Soft delete flag
)

// MerchantMapping Entity - Learned merchant-to-category associations
data class MerchantMapping(
    val id: Long = 0,
    val rawMerchantName: String,           // As appears in SMS (normalized uppercase)
    val normalizedName: String,            // User-friendly display name
    val categoryId: Long,                  // FK to Category
    val subcategoryId: Long? = null,       // FK to Category (subcategory)
    val confidence: Float = 0.5f,          // Confidence score, increases with usage
    val matchCount: Int = 1,               // Times this mapping was confirmed
    val isFuzzyMatch: Boolean = false,     // Created via fuzzy matching
    val createdAt: LocalDateTime,
    val lastUsedAt: LocalDateTime
)

// CreditCard Entity - Credit card tracking
data class CreditCard(
    val id: Long = 0,
    val bankName: String,                  // "HDFC", "ICICI", "SBI"
    val cardName: String? = null,          // "Regalia", "Amazon Pay"
    val lastFourDigits: String,            // "4523"
    val creditLimit: Double? = null,       // Card limit
    val billingCycleDay: Int? = null,      // Day of month bill generates (1-31)
    val dueDateDay: Int? = null,           // Day of month payment due (1-31)
    val currentUnbilled: Double = 0.0,     // Current unbilled amount
    val previousDue: Double = 0.0,         // Last statement due amount
    val previousDueDate: LocalDate? = null,// Due date for previous bill
    val minimumDue: Double? = null,        // Minimum payment due
    val isActive: Boolean = true,
    val createdAt: LocalDateTime
)

// RecurringRule Entity - Subscription/recurring expense detection
data class RecurringRule(
    val id: Long = 0,
    val merchantPattern: String,           // Merchant name pattern to match
    val categoryId: Long,                  // FK to Category
    val expectedAmount: Double,            // Expected transaction amount
    val amountVariance: Float = 0.1f,      // Acceptable variance (10% default)
    val frequency: RecurringFrequency,     // WEEKLY, MONTHLY, YEARLY
    val dayOfPeriod: Int? = null,          // Expected day (1-31 for monthly, 1-7 for weekly)
    val lastOccurrence: LocalDate? = null,
    val nextExpected: LocalDate? = null,
    val occurrenceCount: Int = 0,          // Times this has occurred
    val isActive: Boolean = true,
    val isUserConfirmed: Boolean = false,  // User manually confirmed this rule
    val createdAt: LocalDateTime
)

enum class RecurringFrequency { WEEKLY, MONTHLY, YEARLY }

// Budget Entity - Monthly category budgets
data class Budget(
    val id: Long = 0,
    val categoryId: Long,                  // FK to Category
    val monthlyLimit: Double,              // Budget limit
    val month: YearMonth,                  // Which month this budget applies to
    val alertAt75: Boolean = true,         // Alert at 75% spent
    val alertAt100: Boolean = true,        // Alert at 100% spent
    val createdAt: LocalDateTime
)

// UserStats Entity - Gamification stats
data class UserStats(
    val id: Long = 1,                      // Single row, always id=1
    val currentStreak: Int = 0,            // Current categorization streak (days)
    val longestStreak: Int = 0,            // Best streak ever
    val totalTransactionsLogged: Int = 0,  // Lifetime transactions
    val totalXp: Int = 0,                  // Total XP earned
    val currentLevel: Int = 1,             // Current level (1-8)
    val lastActiveDate: LocalDate? = null, // Last day user categorized something
    val createdAt: LocalDateTime
)

// Achievement Entity - Unlockable achievements
data class Achievement(
    val id: String,                        // Unique ID like "streak_7"
    val name: String,                      // Display name
    val description: String,               // How to earn it
    val emoji: String,                     // Achievement icon
    val xpReward: Int,                     // XP awarded when unlocked
    val requirement: Int,                  // Threshold to unlock
    val type: AchievementType,             // Category of achievement
    val unlockedAt: LocalDateTime? = null, // When user earned it (null if locked)
    val progress: Int = 0                  // Current progress toward requirement
)

enum class AchievementType {
    STREAK,              // Consecutive day streaks
    TRANSACTION_COUNT,   // Total transactions logged
    CATEGORY_COUNT,      // Transactions in specific category
    BUDGET,              // Budget-related achievements
    UNDER_BUDGET,        // Staying under budget
    CREDIT_CARD,         // Credit card tracking
    RECURRING,           // Recurring expense detection
    SPECIAL              // Special/misc achievements
}
```

### SMS Patterns to Support:

Document all SMS formats the parser must handle. Use REAL examples:

```text
# =============================================================================
# UPI TRANSACTIONS
# =============================================================================

# HDFC Bank UPI
"Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"
"Paid Rs.1,25,000.00 to LANDLORD JOHN on 01-12-24 using UPI. UPI Ref: 433218765433. -HDFC Bank"

# SBI UPI
"Rs.1200 debited from A/c XX1234 to VPA swiggy@upi on 14-12-24. UPI Ref 433218765432 -SBI"
"Rs.499.50 debited from A/c XX5678 to VPA amazonpay@upi on 14-12-24. UPI Ref 433218765433 -SBI"

# ICICI UPI
"Dear Customer, Rs.499 has been debited from your account **1234 for UPI txn to PAYTM. Ref: 12345678"
"INR 2,500.00 debited from A/c XX9876 on 14-12-24 for UPI to merchant@ybl. Ref 987654321 -ICICI"

# Axis Bank UPI
"INR 899.00 debited from A/c no. XX4321 on 14-Dec-24 for UPI-ZOMATO. UPI Ref: 433256789012"

# =============================================================================
# CREDIT CARD TRANSACTIONS
# =============================================================================

# HDFC Credit Card
"HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"
"HDFC Bank Credit Card XX4523 has been used for Rs.5,550.00 at CROMA ELECTRONICS on 14-12-24 at 10:15:22"
"Alert: Your HDFC Credit Card XX4523 has been used for Rs.649.00 at NETFLIX.COM on 14-12-24"

# ICICI Credit Card
"Alert: ICICI Card ending 8976 used for INR 5550.00 at CROMA ELECTRONICS on 14-Dec-24"
"Alert: ICICI Card ending 8976 used for INR 649 at NETFLIX.COM on 14-Dec-24"
"ICICI Credit Card XX8976 was used for Rs.1,299 at FLIPKART on 14/12/2024 12:45:30"

# SBI Credit Card
"Your SBI Card ending 3456 was used for Rs.649 at NETFLIX.COM on 14/12/2024"
"SBI Card XX3456 txn of Rs.2,999 at AMAZON.IN on 14-Dec-2024. Not you? Call 1800xxxx"

# Axis Credit Card
"Your Axis Bank Credit Card ending 7890 was used for Rs.899 at SWIGGY on 14-Dec-24"

# =============================================================================
# CREDIT CARD BILLS
# =============================================================================

# HDFC Bill
"Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
"HDFC Credit Card XX4523 Bill: Total Due Rs.25,430, Min Due Rs.1,272, Due Date 05-Jan-25. Pay now."

# ICICI Bill
"ICICI Card bill generated. Amount: Rs.3200. Due: 12-Jan-25. Pay now to avoid charges."
"Your ICICI Credit Card XX8976 bill is Rs.8,750. Min Due: Rs.438. Due Date: 12-Jan-25"

# SBI Bill
"SBI Card XX3456 Statement: Total Due Rs.15,600, Min Due Rs.780, Due by 20-Jan-25"

# =============================================================================
# SUBSCRIPTIONS / RECURRING
# =============================================================================

# Google Play
"Google Play charged Rs.129 to your card ending 4523 for YouTube Premium subscription"
"Rs.299 charged by Google Play for Google One storage to card XX4523"

# Netflix
"Your Netflix subscription of Rs.649 has been renewed using card XX8976"
"Netflix: Rs.649 charged to your card ending 8976 for monthly subscription"

# Amazon Prime
"Amazon Prime membership renewed. Rs.1499 charged to card ending 4523."
"Rs.1,499 charged for Amazon Prime annual subscription to card XX4523"

# Spotify
"Spotify Premium renewed. Rs.119 charged to card XX8976"

# =============================================================================
# ATM WITHDRAWALS
# =============================================================================

"Rs.2000 withdrawn from ATM at HDFC ATM ANNA NAGAR using card XX1234 on 14-12-24"
"Cash withdrawal of Rs.5000 at SBI ATM. Card XX5678. Avl Bal: Rs.25,432"
"ATM WDL Rs.10,000 at ICICI ATM T NAGAR. A/c XX9876. Bal: Rs.45,230"

# =============================================================================
# BANK TRANSFERS (IMPS/NEFT)
# =============================================================================

"Rs.5000 transferred to JOHN DOE (XX5678) from your A/c XX1234. IMPS Ref: 433212345678"
"NEFT of Rs.25,000 to JANE DOE successful. Ref: NEFT123456789. A/c XX1234"
"Rs.10,000 credited to your A/c XX1234 from SALARY ACCOUNT. IMPS Ref: 433298765432"

# =============================================================================
# NON-TRANSACTION SMS (SHOULD BE IGNORED)
# =============================================================================

"Your OTP is 123456. Valid for 5 minutes. Do not share with anyone."
"Get 50% cashback on your next transaction! Use code SAVE50. T&C apply."
"Dear Customer, your account balance is Rs.45,230 as on 14-12-24"
"Your HDFC Bank Fixed Deposit of Rs.1,00,000 matures on 20-Dec-24"
"Reminder: Your credit card payment of Rs.12,450 is due on 05-Jan-25"
"Thank you for shopping at Big Bazaar. Visit again!"
```

### Category Hierarchy:

```kotlin
val DEFAULT_CATEGORIES = listOf(
    // ========== FOOD & DINING (ID: 1-9) ==========
    Category(id = 1, name = "Food & Dining", emoji = "🍽️", parentId = null, isSystem = true, sortOrder = 1),
    Category(id = 2, name = "Groceries", emoji = "🥬", parentId = 1, isSystem = true, sortOrder = 1),
    Category(id = 3, name = "Meat & Seafood", emoji = "🍗", parentId = 1, isSystem = true, sortOrder = 2),
    Category(id = 4, name = "Restaurants", emoji = "🍕", parentId = 1, isSystem = true, sortOrder = 3),
    Category(id = 5, name = "Food Delivery", emoji = "🛵", parentId = 1, isSystem = true, sortOrder = 4),
    Category(id = 6, name = "Snacks & Beverages", emoji = "☕", parentId = 1, isSystem = true, sortOrder = 5),

    // ========== PET CARE (ID: 10-19) ==========
    Category(id = 10, name = "Pet Care", emoji = "🐾", parentId = null, isSystem = true, sortOrder = 2),
    Category(id = 11, name = "Pet Food", emoji = "🦴", parentId = 10, isSystem = true, sortOrder = 1),
    Category(id = 12, name = "Vet & Medical", emoji = "💊", parentId = 10, isSystem = true, sortOrder = 2),
    Category(id = 13, name = "Pet Grooming", emoji = "✂️", parentId = 10, isSystem = true, sortOrder = 3),
    Category(id = 14, name = "Pet Accessories", emoji = "🎾", parentId = 10, isSystem = true, sortOrder = 4),

    // ========== HOUSING & UTILITIES (ID: 20-29) ==========
    Category(id = 20, name = "Housing & Utilities", emoji = "🏠", parentId = null, isSystem = true, sortOrder = 3),
    Category(id = 21, name = "Rent", emoji = "🔑", parentId = 20, isSystem = true, sortOrder = 1),
    Category(id = 22, name = "Electricity", emoji = "⚡", parentId = 20, isSystem = true, sortOrder = 2),
    Category(id = 23, name = "Water", emoji = "💧", parentId = 20, isSystem = true, sortOrder = 3),
    Category(id = 24, name = "Gas", emoji = "🔥", parentId = 20, isSystem = true, sortOrder = 4),
    Category(id = 25, name = "Internet", emoji = "📶", parentId = 20, isSystem = true, sortOrder = 5),
    Category(id = 26, name = "Maintenance", emoji = "🔧", parentId = 20, isSystem = true, sortOrder = 6),

    // ========== SUBSCRIPTIONS & DIGITAL (ID: 30-39) ==========
    Category(id = 30, name = "Subscriptions", emoji = "📱", parentId = null, isSystem = true, sortOrder = 4),
    Category(id = 31, name = "Mobile Recharge", emoji = "📞", parentId = 30, isSystem = true, sortOrder = 1),
    Category(id = 32, name = "Streaming", emoji = "📺", parentId = 30, isSystem = true, sortOrder = 2),
    Category(id = 33, name = "Cloud Storage", emoji = "☁️", parentId = 30, isSystem = true, sortOrder = 3),
    Category(id = 34, name = "Apps & Games", emoji = "🎮", parentId = 30, isSystem = true, sortOrder = 4),
    Category(id = 35, name = "News & Magazines", emoji = "📰", parentId = 30, isSystem = true, sortOrder = 5),

    // ========== TRANSPORT (ID: 40-49) ==========
    Category(id = 40, name = "Transport", emoji = "🚗", parentId = null, isSystem = true, sortOrder = 5),
    Category(id = 41, name = "Fuel", emoji = "⛽", parentId = 40, isSystem = true, sortOrder = 1),
    Category(id = 42, name = "Cab & Auto", emoji = "🚕", parentId = 40, isSystem = true, sortOrder = 2),
    Category(id = 43, name = "Public Transport", emoji = "🚇", parentId = 40, isSystem = true, sortOrder = 3),
    Category(id = 44, name = "Parking", emoji = "🅿️", parentId = 40, isSystem = true, sortOrder = 4),
    Category(id = 45, name = "Vehicle Maintenance", emoji = "🔧", parentId = 40, isSystem = true, sortOrder = 5),

    // ========== HEALTH & MEDICAL (ID: 50-59) ==========
    Category(id = 50, name = "Health & Medical", emoji = "🏥", parentId = null, isSystem = true, sortOrder = 6),
    Category(id = 51, name = "Doctor", emoji = "👨‍⚕️", parentId = 50, isSystem = true, sortOrder = 1),
    Category(id = 52, name = "Pharmacy", emoji = "💊", parentId = 50, isSystem = true, sortOrder = 2),
    Category(id = 53, name = "Lab Tests", emoji = "🔬", parentId = 50, isSystem = true, sortOrder = 3),
    Category(id = 54, name = "Health Insurance", emoji = "🛡️", parentId = 50, isSystem = true, sortOrder = 4),

    // ========== SHOPPING (ID: 60-69) ==========
    Category(id = 60, name = "Shopping", emoji = "🛒", parentId = null, isSystem = true, sortOrder = 7),
    Category(id = 61, name = "Online Shopping", emoji = "📦", parentId = 60, isSystem = true, sortOrder = 1),
    Category(id = 62, name = "Clothing", emoji = "👕", parentId = 60, isSystem = true, sortOrder = 2),
    Category(id = 63, name = "Electronics", emoji = "📱", parentId = 60, isSystem = true, sortOrder = 3),
    Category(id = 64, name = "Home & Kitchen", emoji = "🏡", parentId = 60, isSystem = true, sortOrder = 4),
    Category(id = 65, name = "Personal Care", emoji = "🧴", parentId = 60, isSystem = true, sortOrder = 5),

    // ========== FINANCIAL (ID: 70-79) ==========
    Category(id = 70, name = "Financial", emoji = "💰", parentId = null, isSystem = true, sortOrder = 8),
    Category(id = 71, name = "EMI", emoji = "📅", parentId = 70, isSystem = true, sortOrder = 1),
    Category(id = 72, name = "Insurance", emoji = "🛡️", parentId = 70, isSystem = true, sortOrder = 2),
    Category(id = 73, name = "Investments", emoji = "📈", parentId = 70, isSystem = true, sortOrder = 3),
    Category(id = 74, name = "Credit Card Payment", emoji = "💳", parentId = 70, isSystem = true, sortOrder = 4),
    Category(id = 75, name = "Bank Charges", emoji = "🏦", parentId = 70, isSystem = true, sortOrder = 5),

    // ========== ENTERTAINMENT (ID: 80-89) ==========
    Category(id = 80, name = "Entertainment", emoji = "🎬", parentId = null, isSystem = true, sortOrder = 9),
    Category(id = 81, name = "Movies & Events", emoji = "🎟️", parentId = 80, isSystem = true, sortOrder = 1),
    Category(id = 82, name = "Gaming", emoji = "🎮", parentId = 80, isSystem = true, sortOrder = 2),
    Category(id = 83, name = "Sports & Fitness", emoji = "🏋️", parentId = 80, isSystem = true, sortOrder = 3),

    // ========== EDUCATION (ID: 90-99) ==========
    Category(id = 90, name = "Education", emoji = "📚", parentId = null, isSystem = true, sortOrder = 10),
    Category(id = 91, name = "Courses", emoji = "🎓", parentId = 90, isSystem = true, sortOrder = 1),
    Category(id = 92, name = "Books", emoji = "📖", parentId = 90, isSystem = true, sortOrder = 2),
    Category(id = 93, name = "Supplies", emoji = "✏️", parentId = 90, isSystem = true, sortOrder = 3),

    // ========== TRANSFERS & OTHERS (ID: 100-109) ==========
    Category(id = 100, name = "Transfers & Others", emoji = "💸", parentId = null, isSystem = true, sortOrder = 11),
    Category(id = 101, name = "Family & Friends", emoji = "👨‍👩‍👧‍👦", parentId = 100, isSystem = true, sortOrder = 1),
    Category(id = 102, name = "Gifts", emoji = "🎁", parentId = 100, isSystem = true, sortOrder = 2),
    Category(id = 103, name = "ATM Withdrawal", emoji = "🏧", parentId = 100, isSystem = true, sortOrder = 3),
    Category(id = 104, name = "Donations", emoji = "❤️", parentId = 100, isSystem = true, sortOrder = 4),
    Category(id = 105, name = "Miscellaneous", emoji = "📌", parentId = 100, isSystem = true, sortOrder = 5),
)
```

### Gamification System:

```kotlin
// ========== LEVELS ==========
val LEVELS = listOf(
    Level(level = 1, name = "Budding Saver", minXp = 0, maxXp = 99),
    Level(level = 2, name = "Money Tracker", minXp = 100, maxXp = 299),
    Level(level = 3, name = "Smart Spender", minXp = 300, maxXp = 599),
    Level(level = 4, name = "Budget Boss", minXp = 600, maxXp = 999),
    Level(level = 5, name = "Money Master", minXp = 1000, maxXp = 1499),
    Level(level = 6, name = "Finance Ninja", minXp = 1500, maxXp = 2199),
    Level(level = 7, name = "Wealth Wizard", minXp = 2200, maxXp = 2999),
    Level(level = 8, name = "Fino Legend", minXp = 3000, maxXp = null), // Max level
)

// ========== ACHIEVEMENTS ==========
val ACHIEVEMENTS = listOf(
    // Streak Achievements
    Achievement("streak_3", "Getting Started", "3-day categorization streak", "🔥", 25, 3, AchievementType.STREAK),
    Achievement("streak_7", "Week Warrior", "7-day categorization streak", "🔥", 50, 7, AchievementType.STREAK),
    Achievement("streak_14", "Fortnight Fighter", "14-day categorization streak", "🔥", 100, 14, AchievementType.STREAK),
    Achievement("streak_30", "Monthly Master", "30-day categorization streak", "🔥", 200, 30, AchievementType.STREAK),
    Achievement("streak_100", "Century Club", "100-day categorization streak", "💯", 500, 100, AchievementType.STREAK),
    Achievement("streak_365", "Year of Discipline", "365-day categorization streak", "🏆", 1000, 365, AchievementType.STREAK),

    // Transaction Count Achievements
    Achievement("txn_10", "First Steps", "Log 10 transactions", "📝", 20, 10, AchievementType.TRANSACTION_COUNT),
    Achievement("txn_50", "Getting Serious", "Log 50 transactions", "📝", 50, 50, AchievementType.TRANSACTION_COUNT),
    Achievement("txn_100", "Century Tracker", "Log 100 transactions", "💯", 100, 100, AchievementType.TRANSACTION_COUNT),
    Achievement("txn_500", "Expense Expert", "Log 500 transactions", "🏆", 300, 500, AchievementType.TRANSACTION_COUNT),
    Achievement("txn_1000", "Fino Veteran", "Log 1000 transactions", "⭐", 500, 1000, AchievementType.TRANSACTION_COUNT),

    // Category-Specific Achievements
    Achievement("pet_25", "Pet Parent", "Log 25 pet expenses", "🐾", 50, 25, AchievementType.CATEGORY_COUNT),
    Achievement("pet_50", "Pet Parent Pro", "Log 50 pet expenses", "🐾", 100, 50, AchievementType.CATEGORY_COUNT),
    Achievement("food_50", "Foodie", "Log 50 food expenses", "🍽️", 50, 50, AchievementType.CATEGORY_COUNT),
    Achievement("food_100", "Foodie Pro", "Log 100 food expenses", "🍽️", 100, 100, AchievementType.CATEGORY_COUNT),
    Achievement("fuel_25", "Road Tripper", "Log 25 fuel expenses", "⛽", 50, 25, AchievementType.CATEGORY_COUNT),
    Achievement("fuel_50", "Road Warrior", "Log 50 fuel expenses", "⛽", 100, 50, AchievementType.CATEGORY_COUNT),

    // Budget Achievements
    Achievement("budget_first", "Budget Beginner", "Set your first budget", "💰", 30, 1, AchievementType.BUDGET),
    Achievement("budget_5", "Budget Planner", "Set 5 category budgets", "💰", 75, 5, AchievementType.BUDGET),
    Achievement("under_budget_1", "Under Control", "Stay under budget for 1 month", "✅", 100, 1, AchievementType.UNDER_BUDGET),
    Achievement("under_budget_3", "Budget Boss", "Stay under budget for 3 months", "👑", 300, 3, AchievementType.UNDER_BUDGET),
    Achievement("under_budget_6", "Budget Master", "Stay under budget for 6 months", "🏆", 500, 6, AchievementType.UNDER_BUDGET),

    // Credit Card Achievements
    Achievement("cc_first", "Card Keeper", "Add your first credit card", "💳", 50, 1, AchievementType.CREDIT_CARD),
    Achievement("cc_3", "Card Collector", "Track 3 credit cards", "💳", 100, 3, AchievementType.CREDIT_CARD),
    Achievement("cc_paid_1", "Bill Payer", "Pay credit card bill on time", "✅", 50, 1, AchievementType.CREDIT_CARD),

    // Recurring Achievements
    Achievement("recurring_3", "Pattern Spotter", "Identify 3 recurring expenses", "🔄", 50, 3, AchievementType.RECURRING),
    Achievement("recurring_5", "Subscription Tracker", "Identify 5 recurring expenses", "🔄", 75, 5, AchievementType.RECURRING),
    Achievement("recurring_10", "Subscription Master", "Identify 10 recurring expenses", "🔄", 150, 10, AchievementType.RECURRING),

    // Special Achievements
    Achievement("night_owl", "Night Owl", "Log a transaction after midnight", "🦉", 25, 1, AchievementType.SPECIAL),
    Achievement("early_bird", "Early Bird", "Log a transaction before 6 AM", "🐦", 25, 1, AchievementType.SPECIAL),
    Achievement("weekend_warrior", "Weekend Warrior", "Log transactions every weekend for a month", "🎉", 75, 4, AchievementType.SPECIAL),
    Achievement("speed_demon", "Speed Demon", "Categorize 10 transactions in under 1 minute", "⚡", 50, 10, AchievementType.SPECIAL),
)

// ========== XP REWARDS ==========
val XP_REWARDS = mapOf(
    "categorize_transaction" to 5,         // Categorize any transaction
    "categorize_same_day" to 3,            // Bonus for categorizing on same day as transaction
    "confirm_fuzzy_match" to 10,           // Confirm a fuzzy match suggestion
    "reject_fuzzy_match" to 5,             // Reject and correct a fuzzy match
    "add_manual_transaction" to 8,         // Add transaction manually
    "set_budget" to 20,                    // Set a new budget
    "stay_under_budget_week" to 10,        // Under budget for a week
    "review_weekly_summary" to 15,         // View weekly summary
    "first_transaction_of_day" to 5,       // First categorization of the day
    "add_credit_card" to 25,               // Add a new credit card
    "identify_recurring" to 15,            // System identifies a recurring expense
    "complete_onboarding" to 50,           // Complete onboarding flow
)

// ========== WEEKLY CHALLENGES (Examples) ==========
val WEEKLY_CHALLENGES = listOf(
    Challenge("spend_less_food", "Spend 10% less on Food this week", categoryId = 1, targetReduction = 0.10f, xpReward = 100),
    Challenge("categorize_all", "Categorize all transactions within 24 hours", xpReward = 75),
    Challenge("no_food_delivery", "No food delivery for a week", categoryId = 5, targetAmount = 0.0, xpReward = 100),
    Challenge("track_every_day", "Log at least 1 transaction every day this week", xpReward = 50),
)
```

### Dependencies (build.gradle.kts):

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.fino.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fino.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // WorkManager for background processing
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // Security
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // ML Kit for text recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Charts for analytics
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Testing - Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("androidx.room:room-testing:2.6.1")

    // Testing - Android Instrumented Tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.50")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.50")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```
</phase_0_instructions>

<phase_0_output>
## Phase 0 Output Requirements

Create the following files:

1. **DISCOVERY.md** - Complete analysis document containing:
   - Project structure (created)
   - All entity definitions with fields and types
   - SMS patterns documented
   - Category hierarchy
   - Gamification system specification
   - Architecture decisions and rationale
   - Dependencies list

2. **tests.json** - Initialize with empty test structure

3. **progress.txt** - Initialize with Phase 0 status

4. **Project files** - Create the directory structure and placeholder files

After completing Phase 0, update progress.txt to show Phase 0 complete, then proceed to Phase 1.
</phase_0_output>

---

# PHASE 1: PLANNING

<phase_1_instructions>
## Task: Create Detailed Implementation Plan

Break the project into small, testable milestones. Each milestone must be independently valuable and verifiable.

### Instructions:

1. **Define 9 milestones** with clear scope boundaries
2. **For each milestone**, specify:
   - Goal (one sentence)
   - Tasks to complete
   - Success criteria (testable checkboxes)
   - Estimated duration
   - Dependencies on other milestones
   - Risks and mitigations
   - Rollback plan

3. **Define test scenarios** for each milestone
4. **Identify edge cases** that must be handled

### Milestone Structure:

```markdown
## Milestone X: [Name]

**Goal:** [One sentence describing the outcome]

**Duration:** [Estimated time]

**Dependencies:** [Which milestones must be complete first]

### Tasks:
1. [Task 1]
2. [Task 2]
...

### Test Scenarios:
- [Scenario 1]
- [Scenario 2]
...

### Edge Cases:
- [Edge case 1]
- [Edge case 2]
...

### Success Criteria:
- [ ] [Criterion 1]
- [ ] [Criterion 2]
...

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| [Risk 1] | [H/M/L] | [H/M/L] | [Mitigation] |

### Rollback Plan:
[How to undo if this milestone breaks something]
```

### Required Milestones:

**Milestone 1: Core Foundation**
- Project setup, database schema, basic UI shell
- Manual transaction entry with category picker
- No SMS, no ML, no gamification yet

**Milestone 2: SMS Reading & Parsing**
- SMS permission handling
- BroadcastReceiver for new SMS
- Rule-based parser for transaction SMS
- Uncategorized transaction queue

**Milestone 3: Merchant Learning**
- Save merchant → category mappings
- Auto-categorize repeat merchants
- Confidence scoring

**Milestone 4: Fuzzy Matching**
- Levenshtein distance matching
- Confirmation dialog for fuzzy matches
- Learn from user confirmations

**Milestone 5: Credit Card Tracking**
- Add/edit credit cards
- Detect credit card transactions
- Parse bill SMS
- Calculate upcoming dues

**Milestone 6: Recurring Detection**
- Detect weekly/monthly/yearly patterns
- Subscription management UI
- Next renewal predictions

**Milestone 7: Budgeting**
- Set category budgets
- Track spending vs budget
- Budget alerts
- Budget health indicator

**Milestone 8: Analytics**
- Monthly spending breakdown
- Trend analysis
- Category deep-dive
- Export to CSV

**Milestone 9: Gamification & Polish**
- XP and level system
- Streak tracking
- Achievements
- Onboarding flow
- Biometric lock
- Backup/restore
</phase_1_instructions>

<phase_1_output>
## Phase 1 Output Requirements

Create **PLAN.md** containing:

1. All 9 milestones with full detail
2. Dependency graph showing milestone order
3. Total estimated duration
4. Critical path identified
5. Risk register with all identified risks

Update **progress.txt** to show Phase 1 complete.

After completing Phase 1, proceed to Phase 2.
</phase_1_output>

---

# PHASE 2: TESTS FIRST (RED)

<phase_2_instructions>
## Task: Write Failing Tests Before Implementation

Write comprehensive tests for each component. Tests must FAIL initially because no implementation exists yet.

### Instructions:

1. **Follow existing test patterns** - Use JUnit 4 for unit tests, standard Kotlin test conventions
2. **Write tests that describe EXPECTED behavior** - Not current behavior
3. **Use REAL data** - Actual SMS formats, realistic amounts, real category IDs
4. **Cover all scenarios**:
   - Happy path (valid inputs, expected flow)
   - Invalid inputs (null, empty, malformed)
   - Boundary conditions (min/max values, edge cases)
   - Error scenarios (network failures, permission denied)
   - Large/complex data (long SMS, many transactions)
   - Special characters (unicode, symbols in merchant names)

5. **Run tests and CONFIRM they FAIL** - Failing tests prove they're testing the right thing

### Test File Structure:

```
test/java/com/fino/app/
├── data/
│   ├── local/
│   │   ├── TransactionDaoTest.kt
│   │   ├── CategoryDaoTest.kt
│   │   └── ...
│   └── repository/
│       ├── TransactionRepositoryTest.kt
│       └── ...
├── service/
│   └── parser/
│       ├── SmsParserTest.kt
│       ├── UpiTransactionParserTest.kt
│       ├── CreditCardTransactionParserTest.kt
│       ├── CreditCardBillParserTest.kt
│       └── RecurringDetectorTest.kt
├── ml/
│   ├── MerchantMatcherTest.kt
│   └── FuzzyMatcherTest.kt
├── domain/
│   └── usecase/
│       ├── CategorizeTransactionUseCaseTest.kt
│       ├── CalculateBudgetUseCaseTest.kt
│       └── ...
├── gamification/
│   ├── XpCalculatorTest.kt
│   ├── StreakTrackerTest.kt
│   ├── AchievementTrackerTest.kt
│   └── LevelCalculatorTest.kt
├── presentation/
│   └── viewmodel/
│       ├── HomeViewModelTest.kt
│       └── ...
└── util/
    └── AmountFormatterTest.kt
```

### Required Test Cases:

#### SMS Parser Tests (SmsParserTest.kt)

```kotlin
class SmsParserTest {

    private lateinit var parser: SmsParser

    @Before
    fun setup() {
        parser = SmsParser()
    }

    // ==================== UPI TRANSACTION TESTS ====================

    @Test
    fun `parse HDFC UPI debit SMS - extracts correct amount`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertNotNull(result)
        assertEquals(350.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts merchant name`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals("MY CHICKEN SHOP", result!!.merchantName)
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts transaction date`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals(LocalDate.of(2024, 12, 14), result!!.transactionDate.toLocalDate())
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts UPI reference`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals("433218765432", result!!.reference)
    }

    @Test
    fun `parse SBI UPI debit SMS - different format`() {
        val sms = "Rs.1200 debited from A/c XX1234 to VPA swiggy@upi on 14-12-24. UPI Ref 433218765432 -SBI"
        
        val result = parser.parse(sms)
        
        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("swiggy@upi", result.merchantName)
    }

    @Test
    fun `parse UPI SMS with INR instead of Rs`() {
        val sms = "INR 499.00 debited from A/c XX1234 on 14-12-24 for UPI to merchant@ybl. Ref 987654321 -ICICI"
        
        val result = parser.parse(sms)
        
        assertEquals(499.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse UPI SMS with comma in amount`() {
        val sms = "Paid Rs.1,25,000.00 to LANDLORD JOHN on 01-12-24 using UPI. UPI Ref: 433218765433. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals(125000.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse UPI SMS with decimal amount`() {
        val sms = "Rs.499.50 debited from A/c XX5678 to VPA amazonpay@upi on 14-12-24. UPI Ref 433218765433 -SBI"
        
        val result = parser.parse(sms)
        
        assertEquals(499.50, result!!.amount, 0.01)
    }

    // ==================== CREDIT CARD TRANSACTION TESTS ====================

    @Test
    fun `parse HDFC credit card transaction - extracts amount`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"
        
        val result = parser.parse(sms)
        
        assertNotNull(result)
        assertEquals(2340.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse HDFC credit card transaction - extracts card last four digits`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"
        
        val result = parser.parse(sms)
        
        assertEquals("4523", result!!.cardLastFour)
    }

    @Test
    fun `parse HDFC credit card transaction - extracts bank name`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"
        
        val result = parser.parse(sms)
        
        assertEquals("HDFC", result!!.bankName)
    }

    @Test
    fun `parse ICICI credit card transaction - different format`() {
        val sms = "Alert: ICICI Card ending 8976 used for INR 5550.00 at CROMA ELECTRONICS on 14-Dec-24"
        
        val result = parser.parse(sms)
        
        assertEquals(5550.0, result!!.amount, 0.01)
        assertEquals("CROMA ELECTRONICS", result.merchantName)
        assertEquals("8976", result.cardLastFour)
        assertEquals("ICICI", result.bankName)
    }

    @Test
    fun `parse SBI credit card transaction - yet another format`() {
        val sms = "Your SBI Card ending 3456 was used for Rs.649 at NETFLIX.COM on 14/12/2024"
        
        val result = parser.parse(sms)
        
        assertEquals(649.0, result!!.amount, 0.01)
        assertEquals("NETFLIX.COM", result.merchantName)
        assertEquals("3456", result.cardLastFour)
        assertEquals("SBI", result.bankName)
    }

    // ==================== CREDIT CARD BILL TESTS ====================

    @Test
    fun `parse HDFC credit card bill - extracts total due`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
        
        val result = parser.parseBill(sms)
        
        assertNotNull(result)
        assertEquals(12450.0, result!!.totalDue, 0.01)
    }

    @Test
    fun `parse HDFC credit card bill - extracts minimum due`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
        
        val result = parser.parseBill(sms)
        
        assertEquals(625.0, result!!.minimumDue, 0.01)
    }

    @Test
    fun `parse HDFC credit card bill - extracts due date`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
        
        val result = parser.parseBill(sms)
        
        assertEquals(LocalDate.of(2025, 1, 5), result!!.dueDate)
    }

    @Test
    fun `parse HDFC credit card bill - extracts card last four`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
        
        val result = parser.parseBill(sms)
        
        assertEquals("4523", result!!.cardLastFour)
    }

    @Test
    fun `parse ICICI credit card bill - different format`() {
        val sms = "ICICI Card bill generated. Amount: Rs.3200. Due: 12-Jan-25. Pay now to avoid charges."
        
        val result = parser.parseBill(sms)
        
        assertEquals(3200.0, result!!.totalDue, 0.01)
        assertEquals(LocalDate.of(2025, 1, 12), result.dueDate)
    }

    // ==================== SUBSCRIPTION DETECTION TESTS ====================

    @Test
    fun `parse Google Play subscription - flags as likely subscription`() {
        val sms = "Google Play charged Rs.129 to your card ending 4523 for YouTube Premium subscription"
        
        val result = parser.parse(sms)
        
        assertTrue(result!!.isLikelySubscription)
        assertEquals("YouTube Premium", result.merchantName)
        assertEquals(129.0, result.amount, 0.01)
    }

    @Test
    fun `parse Netflix subscription - flags as likely subscription`() {
        val sms = "Your Netflix subscription of Rs.649 has been renewed using card XX8976"
        
        val result = parser.parse(sms)
        
        assertTrue(result!!.isLikelySubscription)
        assertEquals("Netflix", result.merchantName)
        assertEquals(649.0, result.amount, 0.01)
    }

    @Test
    fun `parse Amazon Prime subscription - flags as likely subscription`() {
        val sms = "Amazon Prime membership renewed. Rs.1499 charged to card ending 4523."
        
        val result = parser.parse(sms)
        
        assertTrue(result!!.isLikelySubscription)
        assertTrue(result.merchantName.contains("Amazon Prime", ignoreCase = true))
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `return null for OTP SMS`() {
        val sms = "Your OTP is 123456. Valid for 5 minutes. Do not share with anyone."
        
        val result = parser.parse(sms)
        
        assertNull(result)
    }

    @Test
    fun `return null for promotional SMS`() {
        val sms = "Get 50% cashback on your next transaction! Use code SAVE50. T&C apply."
        
        val result = parser.parse(sms)
        
        assertNull(result)
    }

    @Test
    fun `return null for balance inquiry SMS`() {
        val sms = "Dear Customer, your account balance is Rs.45,230 as on 14-12-24"
        
        val result = parser.parse(sms)
        
        assertNull(result)
    }

    @Test
    fun `return null for payment reminder SMS`() {
        val sms = "Reminder: Your credit card payment of Rs.12,450 is due on 05-Jan-25"
        
        val result = parser.parse(sms)
        
        assertNull(result)
    }

    @Test
    fun `handle merchant name with special characters`() {
        val sms = "Paid Rs.500.00 to CAFÉ COFFEE DAY - T.NAGAR on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals("CAFÉ COFFEE DAY - T.NAGAR", result!!.merchantName)
    }

    @Test
    fun `handle very large amount`() {
        val sms = "Paid Rs.99,99,999.00 to PROPERTY DEALER on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals(9999999.0, result!!.amount, 0.01)
    }

    @Test
    fun `handle amount without decimal`() {
        val sms = "Paid Rs.500 to SHOP on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"
        
        val result = parser.parse(sms)
        
        assertEquals(500.0, result!!.amount, 0.01)
    }

    // ==================== CONFIDENCE SCORING ====================

    @Test
    fun `high confidence for well-formatted SMS`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"
        
        val result = parser.parse(sms)
        
        assertTrue(result!!.confidence >= 0.9f)
    }

    @Test
    fun `lower confidence for ambiguous SMS`() {
        val sms = "Transaction of Rs.500 completed"
        
        val result = parser.parse(sms)
        
        if (result != null) {
            assertTrue(result.confidence < 0.7f)
        }
    }
}
```

#### Merchant Matcher Tests (MerchantMatcherTest.kt)

```kotlin
class MerchantMatcherTest {

    private lateinit var matcher: MerchantMatcher
    private lateinit var mockRepository: MerchantMappingRepository

    @Before
    fun setup() {
        mockRepository = mock()
        matcher = MerchantMatcher(mockRepository)
    }

    @Test
    fun `exact match - returns mapping with high confidence`() {
        val mapping = MerchantMapping(
            id = 1,
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            confidence = 0.95f,
            matchCount = 10,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        whenever(mockRepository.findByRawName("MY CHICKEN SHOP")).thenReturn(mapping)
        
        val result = matcher.findMatch("MY CHICKEN SHOP")
        
        assertEquals(MatchType.EXACT, result.matchType)
        assertEquals(mapping, result.mapping)
        assertTrue(result.confidence >= 0.95f)
        assertFalse(result.requiresConfirmation)
    }

    @Test
    fun `fuzzy match - returns mapping with medium confidence and requires confirmation`() {
        val mapping = MerchantMapping(
            id = 1,
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            confidence = 0.9f,
            matchCount = 10,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        whenever(mockRepository.findByRawName("CHICKEN CORNER ANNA NAGAR")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(listOf(mapping))
        
        val result = matcher.findMatch("CHICKEN CORNER ANNA NAGAR")
        
        assertEquals(MatchType.FUZZY, result.matchType)
        assertTrue(result.confidence in 0.5f..0.85f)
        assertTrue(result.requiresConfirmation)
    }

    @Test
    fun `no match - returns null mapping`() {
        whenever(mockRepository.findByRawName("TOTALLY NEW MERCHANT")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(emptyList())
        
        val result = matcher.findMatch("TOTALLY NEW MERCHANT")
        
        assertEquals(MatchType.NONE, result.matchType)
        assertNull(result.mapping)
    }

    @Test
    fun `calculate similarity - identical strings return 1`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN SHOP")
        
        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `calculate similarity - similar strings return high score`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "MY CHICKEN STORE")
        
        assertTrue(score >= 0.7f)
    }

    @Test
    fun `calculate similarity - different strings return low score`() {
        val score = matcher.calculateSimilarity("MY CHICKEN SHOP", "AMAZON PRIME")
        
        assertTrue(score < 0.3f)
    }

    @Test
    fun `calculate similarity - case insensitive`() {
        val score = matcher.calculateSimilarity("my chicken shop", "MY CHICKEN SHOP")
        
        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `calculate similarity - handles extra spaces`() {
        val score = matcher.calculateSimilarity("MY  CHICKEN   SHOP", "MY CHICKEN SHOP")
        
        assertEquals(1.0f, score, 0.01f)
    }

    @Test
    fun `fuzzy match - identifies similar merchants across branches`() {
        val mapping = MerchantMapping(
            rawMerchantName = "SWIGGY",
            normalizedName = "Swiggy",
            categoryId = 5,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        whenever(mockRepository.findByRawName("SWIGGY DELIVERY PARTNER")).thenReturn(null)
        whenever(mockRepository.findAllMappings()).thenReturn(listOf(mapping))
        
        val result = matcher.findMatch("SWIGGY DELIVERY PARTNER")
        
        assertEquals(MatchType.FUZZY, result.matchType)
        assertTrue(result.confidence >= 0.6f)
    }

    @Test
    fun `confirm fuzzy match - creates new mapping`() {
        val existingMapping = MerchantMapping(
            id = 1,
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            confidence = 0.8f,
            matchCount = 5,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        
        matcher.confirmFuzzyMatch("CHICKEN CORNER", existingMapping)
        
        verify(mockRepository).insertMapping(argThat { 
            rawMerchantName == "CHICKEN CORNER" && 
            categoryId == existingMapping.categoryId &&
            isFuzzyMatch == true
        })
    }

    @Test
    fun `reject fuzzy match - does not create mapping`() {
        val existingMapping = MerchantMapping(
            rawMerchantName = "MY CHICKEN SHOP",
            normalizedName = "My Chicken Shop",
            categoryId = 3,
            createdAt = LocalDateTime.now(),
            lastUsedAt = LocalDateTime.now()
        )
        
        matcher.rejectFuzzyMatch("CHICKEN CORNER", existingMapping)
        
        verify(mockRepository, never()).insertMapping(any())
    }
}
```

#### Gamification Tests (StreakTrackerTest.kt, etc.)

```kotlin
// StreakTrackerTest.kt
class StreakTrackerTest {

    private lateinit var streakTracker: StreakTracker
    private lateinit var mockUserStatsDao: UserStatsDao

    @Before
    fun setup() {
        mockUserStatsDao = mock()
        streakTracker = StreakTracker(mockUserStatsDao)
    }

    @Test
    fun `first activity - starts streak at 1`() {
        val stats = UserStats(currentStreak = 0, lastActiveDate = null)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        val newStreak = streakTracker.recordActivity(LocalDate.now())
        
        assertEquals(1, newStreak)
    }

    @Test
    fun `consecutive day - increases streak`() {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStats(currentStreak = 5, lastActiveDate = yesterday)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        val newStreak = streakTracker.recordActivity(LocalDate.now())
        
        assertEquals(6, newStreak)
    }

    @Test
    fun `same day activity - does not increase streak`() {
        val today = LocalDate.now()
        val stats = UserStats(currentStreak = 5, lastActiveDate = today)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        val newStreak = streakTracker.recordActivity(today)
        
        assertEquals(5, newStreak)
    }

    @Test
    fun `missed one day - resets streak to 1`() {
        val twoDaysAgo = LocalDate.now().minusDays(2)
        val stats = UserStats(currentStreak = 10, lastActiveDate = twoDaysAgo)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        val newStreak = streakTracker.recordActivity(LocalDate.now())
        
        assertEquals(1, newStreak)
    }

    @Test
    fun `new streak exceeds longest - updates longest streak`() {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStats(currentStreak = 15, longestStreak = 15, lastActiveDate = yesterday)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        streakTracker.recordActivity(LocalDate.now())
        
        verify(mockUserStatsDao).updateStats(argThat { longestStreak == 16 })
    }

    @Test
    fun `streak below longest - does not update longest`() {
        val yesterday = LocalDate.now().minusDays(1)
        val stats = UserStats(currentStreak = 5, longestStreak = 30, lastActiveDate = yesterday)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        
        streakTracker.recordActivity(LocalDate.now())
        
        verify(mockUserStatsDao).updateStats(argThat { longestStreak == 30 })
    }
}

// LevelCalculatorTest.kt
class LevelCalculatorTest {

    private lateinit var calculator: LevelCalculator

    @Before
    fun setup() {
        calculator = LevelCalculator()
    }

    @Test
    fun `0 XP is level 1`() {
        assertEquals(1, calculator.calculateLevel(0))
    }

    @Test
    fun `99 XP is still level 1`() {
        assertEquals(1, calculator.calculateLevel(99))
    }

    @Test
    fun `100 XP is level 2`() {
        assertEquals(2, calculator.calculateLevel(100))
    }

    @Test
    fun `299 XP is still level 2`() {
        assertEquals(2, calculator.calculateLevel(299))
    }

    @Test
    fun `300 XP is level 3`() {
        assertEquals(3, calculator.calculateLevel(300))
    }

    @Test
    fun `3000 XP is max level 8`() {
        assertEquals(8, calculator.calculateLevel(3000))
    }

    @Test
    fun `10000 XP is still max level 8`() {
        assertEquals(8, calculator.calculateLevel(10000))
    }

    @Test
    fun `progress to next level - from level 1 at 50 XP`() {
        val progress = calculator.getProgressToNextLevel(50)
        
        assertEquals(1, progress.currentLevel)
        assertEquals(2, progress.nextLevel)
        assertEquals(50, progress.currentXp)
        assertEquals(100, progress.xpForNextLevel)
        assertEquals(0.5f, progress.progressPercent, 0.01f)
    }

    @Test
    fun `progress at max level - shows 100 percent`() {
        val progress = calculator.getProgressToNextLevel(5000)
        
        assertEquals(8, progress.currentLevel)
        assertNull(progress.nextLevel)
        assertEquals(1.0f, progress.progressPercent, 0.01f)
    }

    @Test
    fun `get level name for level 1`() {
        assertEquals("Budding Saver", calculator.getLevelName(1))
    }

    @Test
    fun `get level name for level 8`() {
        assertEquals("Fino Legend", calculator.getLevelName(8))
    }
}

// AchievementTrackerTest.kt
class AchievementTrackerTest {

    private lateinit var tracker: AchievementTracker
    private lateinit var mockAchievementDao: AchievementDao
    private lateinit var mockUserStatsDao: UserStatsDao

    @Before
    fun setup() {
        mockAchievementDao = mock()
        mockUserStatsDao = mock()
        tracker = AchievementTracker(mockAchievementDao, mockUserStatsDao)
    }

    @Test
    fun `unlock streak_7 at 7 day streak`() {
        val stats = UserStats(currentStreak = 7, totalXp = 100)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(false)
        
        val unlocked = tracker.checkAndUnlock()
        
        assertTrue(unlocked.any { it.id == "streak_7" })
        verify(mockAchievementDao).unlock("streak_7", any())
    }

    @Test
    fun `do not unlock already unlocked achievement`() {
        val stats = UserStats(currentStreak = 7, totalXp = 100)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(true)
        
        val unlocked = tracker.checkAndUnlock()
        
        assertFalse(unlocked.any { it.id == "streak_7" })
    }

    @Test
    fun `unlock txn_100 at 100 transactions`() {
        val stats = UserStats(totalTransactionsLogged = 100, totalXp = 200)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        whenever(mockAchievementDao.isUnlocked("txn_100")).thenReturn(false)
        
        val unlocked = tracker.checkAndUnlock()
        
        assertTrue(unlocked.any { it.id == "txn_100" })
    }

    @Test
    fun `award XP when achievement unlocked`() {
        val stats = UserStats(currentStreak = 7, totalXp = 100)
        whenever(mockUserStatsDao.getUserStats()).thenReturn(stats)
        whenever(mockAchievementDao.isUnlocked("streak_7")).thenReturn(false)
        
        tracker.checkAndUnlock()
        
        // streak_7 gives 50 XP
        verify(mockUserStatsDao).updateStats(argThat { totalXp == 150 })
    }
}
```

#### Budget Calculator Tests

```kotlin
class CalculateBudgetUseCaseTest {

    private lateinit var useCase: CalculateBudgetUseCase
    private lateinit var mockTransactionRepo: TransactionRepository
    private lateinit var mockBudgetRepo: BudgetRepository
    private lateinit var mockCreditCardRepo: CreditCardRepository

    @Before
    fun setup() {
        mockTransactionRepo = mock()
        mockBudgetRepo = mock()
        mockCreditCardRepo = mock()
        useCase = CalculateBudgetUseCase(mockTransactionRepo, mockBudgetRepo, mockCreditCardRepo)
    }

    @Test
    fun `calculate monthly spending - sums debit transactions only`() {
        val transactions = listOf(
            Transaction(amount = 350.0, type = TransactionType.DEBIT),
            Transaction(amount = 1200.0, type = TransactionType.DEBIT),
            Transaction(amount = 500.0, type = TransactionType.DEBIT),
            Transaction(amount = 5000.0, type = TransactionType.CREDIT) // Should be excluded
        )
        whenever(mockTransactionRepo.getTransactionsForMonth(any())).thenReturn(transactions)
        
        val result = useCase.calculateMonthlySpending(YearMonth.now())
        
        assertEquals(2050.0, result, 0.01)
    }

    @Test
    fun `budget health - calculates remaining after bills`() {
        whenever(mockBudgetRepo.getMonthlyBudget()).thenReturn(60000.0)
        whenever(mockTransactionRepo.getMonthlySpending()).thenReturn(45000.0)
        whenever(mockCreditCardRepo.getUpcomingBills(any())).thenReturn(listOf(
            CreditCardBill(amount = 12450.0, dueDate = LocalDate.now().plusDays(10)),
            CreditCardBill(amount = 3200.0, dueDate = LocalDate.now().plusDays(15))
        ))
        
        val health = useCase.calculateBudgetHealth()
        
        assertEquals(60000.0, health.totalBudget, 0.01)
        assertEquals(45000.0, health.spent, 0.01)
        assertEquals(15650.0, health.upcomingBills, 0.01)
        assertEquals(-650.0, health.remaining, 0.01) // Over budget!
        assertEquals(BudgetStatus.OVER_BUDGET, health.status)
    }

    @Test
    fun `budget health - safe status when under budget`() {
        whenever(mockBudgetRepo.getMonthlyBudget()).thenReturn(60000.0)
        whenever(mockTransactionRepo.getMonthlySpending()).thenReturn(30000.0)
        whenever(mockCreditCardRepo.getUpcomingBills(any())).thenReturn(listOf(
            CreditCardBill(amount = 10000.0, dueDate = LocalDate.now().plusDays(10))
        ))
        
        val health = useCase.calculateBudgetHealth()
        
        assertEquals(20000.0, health.remaining, 0.01)
        assertEquals(BudgetStatus.SAFE, health.status)
    }

    @Test
    fun `budget health - warning status at 75 percent`() {
        whenever(mockBudgetRepo.getMonthlyBudget()).thenReturn(60000.0)
        whenever(mockTransactionRepo.getMonthlySpending()).thenReturn(45000.0)
        whenever(mockCreditCardRepo.getUpcomingBills(any())).thenReturn(emptyList())
        
        val health = useCase.calculateBudgetHealth()
        
        assertEquals(BudgetStatus.WARNING, health.status)
    }

    @Test
    fun `category budget progress - calculates percentage`() {
        val budget = Budget(categoryId = 1, monthlyLimit = 10000.0, month = YearMonth.now())
        whenever(mockBudgetRepo.getBudgetForCategory(1)).thenReturn(budget)
        whenever(mockTransactionRepo.getSpendingForCategory(1, any())).thenReturn(7500.0)
        
        val progress = useCase.getCategoryBudgetProgress(1)
        
        assertEquals(10000.0, progress.limit, 0.01)
        assertEquals(7500.0, progress.spent, 0.01)
        assertEquals(0.75f, progress.progressPercent, 0.01f)
        assertEquals(BudgetStatus.WARNING, progress.status)
    }
}
```

#### Amount Formatter Tests

```kotlin
class AmountFormatterTest {

    private lateinit var formatter: AmountFormatter

    @Before
    fun setup() {
        formatter = AmountFormatter()
    }

    @Test
    fun `format small amount with rupee symbol`() {
        assertEquals("₹350", formatter.format(350.0))
    }

    @Test
    fun `format amount with paise`() {
        assertEquals("₹350.50", formatter.format(350.50))
    }

    @Test
    fun `format thousands with comma`() {
        assertEquals("₹1,234", formatter.format(1234.0))
    }

    @Test
    fun `format lakhs in compact notation`() {
        assertEquals("₹1.2L", formatter.formatCompact(120000.0))
    }

    @Test
    fun `format thousands in compact notation`() {
        assertEquals("₹45.2K", formatter.formatCompact(45230.0))
    }

    @Test
    fun `format sub-thousand not compact`() {
        assertEquals("₹999", formatter.formatCompact(999.0))
    }

    @Test
    fun `format crores in compact notation`() {
        assertEquals("₹1.5Cr", formatter.formatCompact(15000000.0))
    }

    @Test
    fun `format zero`() {
        assertEquals("₹0", formatter.format(0.0))
    }

    @Test
    fun `format negative amount`() {
        assertEquals("-₹500", formatter.format(-500.0))
    }
}
```

### Expected Test Results (RED Phase):

After writing all tests and before implementing any code, run:

```bash
./gradlew test
```

**Expected Output:**
```
> Task :app:testDebugUnitTest

SmsParserTest > parse HDFC UPI debit SMS - extracts correct amount FAILED
SmsParserTest > parse HDFC UPI debit SMS - extracts merchant name FAILED
SmsParserTest > parse HDFC UPI debit SMS - extracts transaction date FAILED
... (all SMS parser tests fail)

MerchantMatcherTest > exact match - returns mapping with high confidence FAILED
MerchantMatcherTest > fuzzy match - returns mapping with medium confidence FAILED
... (all matcher tests fail)

StreakTrackerTest > first activity - starts streak at 1 FAILED
StreakTrackerTest > consecutive day - increases streak FAILED
... (all gamification tests fail)

CalculateBudgetUseCaseTest > calculate monthly spending - sums debit transactions only FAILED
... (all budget tests fail)

AmountFormatterTest > format small amount with rupee symbol FAILED
... (all formatter tests fail)

==========================================
TEST RESULTS:
==========================================
Total tests: 85
Passing: 0
Failing: 85

BUILD FAILED ❌

All tests MUST fail at this point.
This proves the tests are testing functionality that doesn't exist yet.
==========================================
```

This is the expected and correct outcome. Proceed to Phase 3 only after confirming all tests fail.
</phase_2_instructions>

<phase_2_output>
## Phase 2 Output Requirements

1. **Create all test files** as specified in the test structure
2. **Update tests.json** with all test cases and status "failing"
3. **Run tests** and capture output showing all tests fail
4. **Update progress.txt** with test counts and Phase 2 status

**Verification Command:**
```bash
./gradlew test 2>&1 | tee test_results_phase2.txt
```

The test output file should show all tests failing. This is correct behavior.

After confirming all tests fail, proceed to Phase 3.
</phase_2_output>

---

# PHASE 3: IMPLEMENTATION (GREEN)

<phase_3_instructions>
## Task: Write Minimal Code to Pass Tests

Implement code to make failing tests pass, one test at a time.

### Instructions:

1. **Pick ONE failing test** to focus on
2. **Analyze why it fails** - missing class? missing method? wrong return value?
3. **Write MINIMAL code** to make that specific test pass
4. **Run the test** to verify it passes
5. **If it passes** - move to the next failing test
6. **If it still fails** - debug, fix, and retry
7. **Document what fixed it** in progress.txt
8. **Repeat** until ALL tests pass

### Iteration Pattern:

```
WHILE (any test failing):
    1. Run: ./gradlew test --tests "ClassName.testMethodName"
    2. Read the failure message carefully
    3. Identify: What class/method is missing or broken?
    4. Write the minimum code to fix ONLY this failure
    5. Run the single test again
    6. IF passes:
        - Update tests.json status to "passing"
        - Commit: git commit -m "GREEN: TestName now passes"
        - Move to next failing test
    7. IF still fails:
        - Read new error message
        - Fix and retry
    8. Document in progress.txt what was implemented
```

### Implementation Order:

Follow this order to minimize dependencies:

```
1. Utility Classes (no dependencies)
   ├── AmountFormatter
   ├── DateUtils
   └── Extensions

2. Domain Models (no dependencies)
   ├── Enums
   ├── Transaction
   ├── Category
   ├── MerchantMapping
   ├── CreditCard
   ├── RecurringRule
   ├── Budget
   ├── UserStats
   └── Achievement

3. Database Layer (depends on models)
   ├── Entity classes
   ├── DAO interfaces
   └── FinoDatabase

4. Repository Layer (depends on database)
   ├── TransactionRepository
   ├── CategoryRepository
   ├── MerchantMappingRepository
   └── ...

5. Parser Layer (depends on models)
   ├── ParsedTransaction (data class)
   ├── SmsParser
   ├── UpiTransactionParser
   ├── CreditCardTransactionParser
   └── CreditCardBillParser

6. ML/Matching Layer (depends on repository)
   ├── FuzzyMatcher (string similarity)
   └── MerchantMatcher

7. Gamification Layer (depends on repository)
   ├── LevelCalculator
   ├── XpCalculator
   ├── StreakTracker
   └── AchievementTracker

8. Domain Use Cases (depends on repositories)
   ├── CategorizeTransactionUseCase
   ├── CalculateBudgetUseCase
   └── ...

9. ViewModels (depends on use cases)

10. UI Components (depends on ViewModels)
```

### Example Implementation Session:

```
Session: Implementing AmountFormatter

1. Run test:
   ./gradlew test --tests "AmountFormatterTest.format small amount with rupee symbol"
   
   Output: "class AmountFormatter not found"

2. Create AmountFormatter.kt:
   class AmountFormatter {
       fun format(amount: Double): String {
           return "₹${amount.toInt()}"
       }
   }

3. Run test again:
   PASSED ✅

4. Run next test:
   ./gradlew test --tests "AmountFormatterTest.format amount with paise"
   
   Output: Expected "₹350.50" but was "₹350"

5. Update AmountFormatter.kt:
   fun format(amount: Double): String {
       return if (amount % 1.0 == 0.0) {
           "₹${amount.toInt()}"
       } else {
           "₹${String.format("%.2f", amount)}"
       }
   }

6. Run test again:
   PASSED ✅

7. Continue to next test...
```

### Key Implementation Hints:

#### SmsParser - Use Regex Patterns

```kotlin
class SmsParser {
    
    private val patterns = listOf(
        // HDFC UPI: "Paid Rs.350.00 to MERCHANT on DD-MM-YY"
        ParsePattern(
            regex = Regex(
                """Paid Rs\.?([0-9,]+\.?\d*)\s+to\s+(.+?)\s+on\s+(\d{2}-\d{2}-\d{2,4}).*?UPI.*?Ref[:\s]*(\d+)""",
                RegexOption.IGNORE_CASE
            ),
            extractor = { match ->
                ParsedTransaction(
                    amount = parseAmount(match.groupValues[1]),
                    merchantName = match.groupValues[2].trim(),
                    transactionDate = parseDate(match.groupValues[3]),
                    reference = match.groupValues[4],
                    type = TransactionType.DEBIT,
                    confidence = 0.95f
                )
            }
        ),
        // Add more patterns...
    )
    
    fun parse(smsBody: String): ParsedTransaction? {
        for (pattern in patterns) {
            val match = pattern.regex.find(smsBody)
            if (match != null) {
                return pattern.extractor(match)
            }
        }
        return null
    }
    
    private fun parseAmount(amountStr: String): Double {
        return amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
    }
    
    private fun parseDate(dateStr: String): LocalDateTime {
        // Handle multiple date formats: DD-MM-YY, DD-MM-YYYY, DD-Mon-YY
        // Implementation here
    }
}
```

#### FuzzyMatcher - Levenshtein Distance

```kotlin
class FuzzyMatcher {
    
    fun calculateSimilarity(a: String, b: String): Float {
        val s1 = normalize(a)
        val s2 = normalize(b)
        
        if (s1 == s2) return 1.0f
        if (s1.isEmpty() || s2.isEmpty()) return 0.0f
        
        val distance = levenshteinDistance(s1, s2)
        val maxLen = maxOf(s1.length, s2.length)
        
        return 1.0f - (distance.toFloat() / maxLen)
    }
    
    private fun normalize(s: String): String {
        return s.uppercase()
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i-1] == s2[j-1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i-1][j] + 1,      // deletion
                    dp[i][j-1] + 1,      // insertion
                    dp[i-1][j-1] + cost  // substitution
                )
            }
        }
        
        return dp[s1.length][s2.length]
    }
}
```

### Commit Strategy:

After each test passes:
```bash
git add .
git commit -m "GREEN: [TestClass].[testMethod] now passes

- Implemented [what you implemented]
- [Any notes about the implementation]"
```
</phase_3_instructions>

<phase_3_output>
## Phase 3 Output Requirements

1. **All test files** must now pass
2. **Run full test suite** and capture passing output:
   ```bash
   ./gradlew test 2>&1 | tee test_results_phase3.txt
   ```
3. **Update tests.json** - all statuses should be "passing"
4. **Update progress.txt** with implementation notes
5. **Git log** showing incremental commits

**Expected Test Output:**
```
==========================================
TEST RESULTS:
==========================================
Total tests: 85
Passing: 85
Failing: 0

BUILD SUCCESSFUL ✅
==========================================
```

After all tests pass, proceed to Phase 4.
</phase_3_output>

---

# PHASE 4: REFACTOR

<phase_4_instructions>
## Task: Improve Code Quality While Keeping Tests Green

Now that all tests pass, improve the code without breaking anything.

### Instructions:

1. **Only refactor when ALL tests are GREEN**
2. **Make ONE refactoring change at a time**
3. **Run ALL tests after each change**
4. **If any test fails** - UNDO the change and try a different approach

### Refactoring Checklist:

- [ ] Extract common code into utility classes
- [ ] Remove code duplication (DRY principle)
- [ ] Apply SOLID principles
- [ ] Improve naming (classes, methods, variables)
- [ ] Add KDoc comments to public APIs
- [ ] Optimize database queries (add indexes if needed)
- [ ] Add proper error handling with meaningful messages
- [ ] Implement proper logging
- [ ] Ensure consistent code formatting
- [ ] Remove unused imports and dead code

### Example Refactoring:

**Before:**
```kotlin
class SmsParser {
    fun parse(sms: String): ParsedTransaction? {
        val pattern1 = Regex("""...""")
        val pattern2 = Regex("""...""")
        // ... 20 more patterns inline
    }
}
```

**After:**
```kotlin
class SmsParser(
    private val patterns: List<SmsPattern> = DefaultPatterns.all()
) {
    fun parse(sms: String): ParsedTransaction? {
        return patterns.firstNotNullOfOrNull { it.tryParse(sms) }
    }
}

// Patterns extracted to separate file
object DefaultPatterns {
    fun all(): List<SmsPattern> = listOf(
        HdfcUpiPattern(),
        SbiUpiPattern(),
        IciciUpiPattern(),
        HdfcCreditCardPattern(),
        // ...
    )
}
```

**Verify after refactor:**
```bash
./gradlew test
# Must still show: 85 passing, 0 failing
```
</phase_4_instructions>

<phase_4_output>
## Phase 4 Output Requirements

1. **All tests still pass** after refactoring
2. **Code quality improved** - documented in refactoring notes
3. **Update progress.txt** with refactoring changes made

After refactoring is complete, proceed to Phase 5.
</phase_4_output>

---

# PHASE 5: VERIFICATION

<phase_5_instructions>
## Task: Comprehensive Testing and Verification

Verify everything works correctly through automated and manual testing.

### Instructions:

1. **Run full test suite**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest  # If device available
   ```

2. **Check test coverage**
   ```bash
   ./gradlew jacocoTestReport
   # Target: >80% line coverage for core classes
   ```

3. **Manual Testing Checklist**

   - [ ] App installs without crash on clean device
   - [ ] Onboarding flow completes successfully
   - [ ] SMS permission request works
   - [ ] Manual transaction entry works
   - [ ] Category picker shows hierarchy correctly
   - [ ] Quick templates create transactions
   - [ ] Transaction list shows grouped by date
   - [ ] SMS parsing works for test messages
   - [ ] Uncategorized transactions appear for review
   - [ ] One-tap categorization works
   - [ ] Merchant learning auto-categorizes repeat merchants
   - [ ] Fuzzy match dialog appears for similar merchants
   - [ ] Credit card can be added
   - [ ] Credit card transactions link to card
   - [ ] Bill SMS updates card due amount
   - [ ] Recurring subscriptions detected
   - [ ] Budget can be set per category
   - [ ] Budget progress bars update
   - [ ] Budget alerts trigger at thresholds
   - [ ] Analytics pie chart renders
   - [ ] Trend analysis shows comparison
   - [ ] Export produces valid CSV
   - [ ] XP awarded for actions
   - [ ] Level up notification works
   - [ ] Streak maintained across days
   - [ ] Achievements unlock correctly
   - [ ] Biometric lock activates
   - [ ] App lock on background works
   - [ ] Backup creates file
   - [ ] Restore recovers data
   - [ ] Dark mode/light mode follows system
   - [ ] App handles 1000+ transactions without lag

4. **Edge Case Testing**

   - [ ] No SMS permission - graceful degradation
   - [ ] Empty transaction list - shows empty state
   - [ ] Very large amounts (₹99,99,999) - displays correctly
   - [ ] Special characters in merchant name - handled
   - [ ] Multiple credit cards - all tracked separately
   - [ ] Year boundary (Dec → Jan) - dates correct
   - [ ] App killed and restored - state preserved
   - [ ] Low storage scenario - backup warns user
   - [ ] No network (for ML fallback) - works offline

5. **Performance Testing**

   - [ ] App launch time < 2 seconds
   - [ ] Transaction list scrolls smoothly
   - [ ] Category picker responds instantly
   - [ ] Analytics charts render < 1 second
   - [ ] Database queries < 100ms
</phase_5_instructions>

<phase_5_output>
## Phase 5 Output Requirements

Create **VERIFICATION.md** containing:

1. Test suite results (all passing)
2. Coverage report summary
3. Manual testing checklist (all checked)
4. Edge case results
5. Performance metrics
6. Any issues found and resolved

Update **progress.txt** with Phase 5 completion.

After verification passes, proceed to Phase 6.
</phase_5_output>

---

# PHASE 6: DOCUMENTATION

<phase_6_instructions>
## Task: Complete All Documentation

Document everything for future reference and maintenance.

### Required Documentation:

1. **README.md**
   - Project overview
   - Features list
   - Screenshots
   - Installation instructions
   - Build instructions
   - Architecture overview
   - Contributing guidelines

2. **CHANGELOG.md**
   - Version 1.0.0 initial release
   - All features implemented
   - Known limitations

3. **ARCHITECTURE.md**
   - System architecture diagram
   - Component descriptions
   - Data flow
   - Design decisions and rationale

4. **TESTING.md**
   - How to run tests
   - Test coverage goals
   - Adding new tests

5. **BACKUP_FORMAT.md**
   - Backup file structure
   - JSON schema
   - Restore process

### Code Documentation:

- KDoc comments on all public classes and methods
- Inline comments for complex logic
- README in each major package explaining purpose

### Final Verification:

- [ ] All documentation complete
- [ ] All tests passing
- [ ] Code properly commented
- [ ] Git history clean with meaningful commits
- [ ] APK builds successfully
</phase_6_instructions>

<phase_6_output>
## Phase 6 Output Requirements

1. All documentation files created
2. Code documentation complete
3. Final build verified
4. **Update progress.txt** marking project COMPLETE

### Success Criteria - Project Complete When:

- [ ] All unit tests pass (85+ tests)
- [ ] Test coverage >80% for core classes
- [ ] Manual testing checklist complete
- [ ] All documentation written
- [ ] APK builds and installs
- [ ] No critical bugs
- [ ] Code reviewed and clean
</phase_6_output>

---

<execution_instructions>
## How to Execute This Prompt

### Starting the Project:

```
Apply TDD methodology for building Fino expense tracker app.
Start with Phase 0: Discovery & Analysis.
Create the project structure and DISCOVERY.md document.
```

### Continuing After Context Refresh:

```
Continue Fino development.
Read progress.txt and tests.json to understand current state.
Resume from the current phase.
```

### Phase-Specific Commands:

```
# Phase 0
"Execute Phase 0: Create project structure and document all findings in DISCOVERY.md"

# Phase 1  
"Execute Phase 1: Create detailed PLAN.md with all 9 milestones"

# Phase 2
"Execute Phase 2: Write all failing tests. Confirm all tests FAIL before proceeding."

# Phase 3
"Execute Phase 3: Implement code to pass tests one at a time. Follow the iteration pattern."

# Phase 4
"Execute Phase 4: Refactor while keeping all tests green."

# Phase 5
"Execute Phase 5: Run full verification and create VERIFICATION.md"

# Phase 6
"Execute Phase 6: Complete all documentation and finalize the project."
```

### Important Reminders:

1. **Never skip phases** - Each phase builds on the previous
2. **Tests must fail in Phase 2** - This proves they test the right thing
3. **One test at a time in Phase 3** - Isolate changes
4. **Always run tests after changes** - Catch regressions immediately
5. **Document everything** - Future you will thank you
6. **Use state files** - tests.json and progress.txt enable seamless continuation
</execution_instructions>

---

*This prompt follows the TDD methodology defined in SKILL.md, incorporates patterns from EXAMPLES.md and TECH_REFERENCE.md, and applies Claude 4.x prompting best practices including explicit instructions, context/motivation, XML structure, state tracking, and incremental progress patterns.*
