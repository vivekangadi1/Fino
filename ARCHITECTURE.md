# Fino - Architecture Document

## Overview

Fino follows **Clean Architecture** principles with **MVVM** (Model-View-ViewModel) pattern for the presentation layer. The app is designed to be fully offline-first with local data storage.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│    (Compose UI, ViewModels, Navigation, Theme)          │
├─────────────────────────────────────────────────────────┤
│                      Domain Layer                        │
│    (Use Cases, Domain Models, Business Logic)           │
├─────────────────────────────────────────────────────────┤
│                       Data Layer                         │
│    (Repositories, DAOs, Entities, Parsers)              │
└─────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer

**Components:**
- `MainActivity.kt` - Single activity with Compose
- `FinoNavigation.kt` - Navigation graph with 7 screens
- `Theme.kt`, `Color.kt`, `Type.kt` - Material 3 theming
- ViewModels (to be implemented per screen)

**Navigation Destinations:**
- Home - Dashboard with recent transactions
- Cards - Credit card management
- Analytics - Spending charts and trends
- Settings - App configuration
- Onboarding - First-time user flow
- TransactionEntry - Manual transaction input
- TransactionList - Full transaction history

### 2. Domain Layer

**Domain Models:**
- `Transaction` - Core transaction record
- `Category` - Hierarchical category structure
- `MerchantMapping` - Learned merchant associations
- `CreditCard` - Credit card details
- `RecurringRule` - Subscription detection
- `Budget` - Monthly category budgets
- `UserStats` - Gamification stats (singleton)
- `Achievement` - Unlockable achievements
- `Level` - Level definition
- `LevelProgress` - Progress to next level
- `Challenge` - Weekly challenges

**Enums:**
- `TransactionType` - DEBIT, CREDIT
- `TransactionSource` - SMS, MANUAL, EMAIL
- `RecurringFrequency` - WEEKLY, MONTHLY, YEARLY
- `BudgetStatus` - SAFE, WARNING, OVER_BUDGET
- `AchievementType` - STREAK, TRANSACTION_COUNT, etc.
- `MatchType` - EXACT, FUZZY, NONE

### 3. Data Layer

**Database:**
- Room database with 8 entities
- Type converters for enums and dates
- Indices on frequently queried columns

**Entities:**
```
TransactionEntity
├── id: Long (PK)
├── amount: Double
├── type: TransactionType
├── categoryId: Long (FK, indexed)
├── merchantName: String (indexed)
├── transactionDate: Long (indexed)
├── needsReview: Boolean (indexed)
└── ... (25 total fields)
```

**DAOs:**
- `TransactionDao` - CRUD + custom queries
- `CategoryDao` - Category hierarchy
- `MerchantMappingDao` - Merchant learning
- `CreditCardDao` - Card management
- `RecurringRuleDao` - Subscription patterns
- `BudgetDao` - Budget tracking
- `UserStatsDao` - Gamification (singleton)
- `AchievementDao` - Achievement tracking

**Repositories:**
- `TransactionRepository`
- `MerchantMappingRepository`
- `CreditCardRepository`
- `BudgetRepository`

## Data Flow

### SMS Processing Flow

```
SMS Received
    │
    ▼
BroadcastReceiver
    │
    ▼
SmsParser.parse(smsBody)
    │
    ├── isNonTransactionSms() → filter OTP, promo
    │
    ├── UpiTransactionParser.parse()
    │   └── 5 regex patterns for HDFC, SBI, ICICI, Axis
    │
    └── CreditCardTransactionParser.parse()
        └── 11 regex patterns + subscription detection
    │
    ▼
ParsedTransaction
    │
    ▼
MerchantMatcher.findMatch()
    │
    ├── Exact match → Auto-categorize
    │
    ├── Fuzzy match (>95%) → Auto-categorize
    │
    ├── Fuzzy match (70-95%) → Show confirmation dialog
    │
    └── No match → Add to uncategorized queue
    │
    ▼
TransactionRepository.insert()
    │
    ▼
XP Reward + Achievement Check
```

### Merchant Learning Flow

```
User Categorizes Transaction
    │
    ▼
MerchantMatcher.createMapping()
    │
    ▼
MerchantMappingRepository.insert()
    │
    └── rawMerchantName → categoryId
        confidence = 1.0 (user set)
    │
    ▼
Next occurrence of same merchant
    │
    ▼
MerchantMatcher.findMatch()
    │
    └── EXACT match → Auto-categorize
        increment matchCount
        update confidence
```

### Fuzzy Matching Flow

```
New Merchant Name
    │
    ▼
FuzzyMatcher.calculateSimilarity()
    │
    └── Levenshtein Distance Algorithm
        1. Normalize strings (uppercase, trim, collapse spaces)
        2. Calculate edit distance
        3. Similarity = 1 - (distance / maxLength)
    │
    ▼
Similarity Score
    │
    ├── >= 0.95 → Auto-apply (high confidence)
    │
    ├── >= 0.70 → Suggest with confirmation dialog
    │
    └── < 0.70 → No match
```

## Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Fino App                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐        │
│  │   Compose    │   │  ViewModels  │   │  Navigation  │        │
│  │     UI       │◄──┤              │   │    Graph     │        │
│  └──────────────┘   └──────┬───────┘   └──────────────┘        │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────┐      │
│  │                    Use Cases                           │      │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │      │
│  │  │ Categorize   │  │  Calculate   │  │   Process    │ │      │
│  │  │ Transaction  │  │   Budget     │  │     SMS      │ │      │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │      │
│  └───────────────────────────────────────────────────────┘      │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────┐      │
│  │                   Repositories                         │      │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │      │
│  │  │ Transaction  │  │  Merchant    │  │   Budget     │ │      │
│  │  │   Repo       │  │   Mapping    │  │    Repo      │ │      │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │      │
│  └───────────────────────────────────────────────────────┘      │
│                            │                                     │
│  ┌─────────────────────────┼─────────────────────────────┐      │
│  │                    Room Database                       │      │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │      │
│  │  │     DAOs     │  │   Entities   │  │  Converters  │ │      │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │      │
│  └───────────────────────────────────────────────────────┘      │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    Services                              │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │    │
│  │  │  SmsParser   │  │ FuzzyMatcher │  │ Achievement  │   │    │
│  │  │ (20+ regex)  │  │ (Levenshtein)│  │   Tracker    │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Dependency Injection

Using Hilt for dependency injection:

```
@HiltAndroidApp
class FinoApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinoDatabase

    @Provides
    fun provideTransactionDao(db: FinoDatabase): TransactionDao
    // ... other DAOs
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideSmsParser(): SmsParser

    @Provides @Singleton
    fun provideFuzzyMatcher(): FuzzyMatcher
    // ... other singletons
}
```

## Key Design Decisions

### 1. Local-Only Data
- All data stored in Room database
- No network calls
- Privacy by design

### 2. Regex-Based Parsing
- 20+ regex patterns for SMS parsing
- Confidence scoring for each pattern
- Fallback chain of parsers

### 3. Fuzzy Matching with Confirmation
- Levenshtein distance algorithm
- Auto-apply threshold: 95%
- Confirmation threshold: 70%
- User decisions improve future accuracy

### 4. Singleton User Stats
- Single row in database (id=1)
- Atomic updates for XP, streaks
- Simple level calculation

### 5. Gamification System
- 8 fixed levels with XP thresholds
- 20+ achievements with categories
- Daily streak tracking with grace period

## Threading Model

```
Main Thread
├── Compose UI rendering
├── User interactions
└── Navigation

Background (Dispatchers.IO)
├── Room database operations
├── SMS parsing
└── File I/O (backup/restore)

Background (Dispatchers.Default)
├── Heavy computation (fuzzy matching on large datasets)
└── JSON serialization
```

## Error Handling

```kotlin
// Repository layer - handle DB errors
suspend fun insertTransaction(transaction: Transaction): Result<Long> {
    return try {
        val id = dao.insert(transaction.toEntity())
        Result.success(id)
    } catch (e: SQLiteException) {
        Result.failure(e)
    }
}

// ViewModel layer - expose to UI
sealed class UiState<T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: String) : UiState<T>()
    class Loading<T> : UiState<T>()
}
```

## Security Considerations

1. **SMS Permission**: Only reads, never sends
2. **Biometric Lock**: Optional fingerprint/face authentication
3. **Encrypted Backup**: AES encryption for backup files
4. **No Analytics**: No tracking or telemetry
5. **ProGuard**: Code obfuscation for release builds

## Performance Optimizations

1. **Indexed Columns**: categoryId, transactionDate, merchantName, needsReview
2. **Lazy Loading**: Paged lists for transactions
3. **Flow-based Queries**: Reactive updates without polling
4. **Efficient Regex**: Compiled patterns, fail-fast matching
5. **Batch Operations**: Bulk inserts for initial SMS scan
