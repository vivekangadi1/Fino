# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

The project uses a local JDK at `jdk-17.0.13+11`. Set JAVA_HOME before running Gradle commands:

```powershell
# Windows PowerShell
$env:JAVA_HOME = 'C:\projects\Fino\jdk-17.0.13+11'

# Build debug APK
.\gradlew.bat assembleDebug

# Run all unit tests
.\gradlew.bat testDebugUnitTest

# Run specific test class
.\gradlew.bat testDebugUnitTest --tests "com.fino.app.service.parser.SmsParserTest"

# Run tests in a package
.\gradlew.bat testDebugUnitTest --tests "com.fino.app.domain.model.*"

# Install on connected device
.\gradlew.bat installDebug
```

Test reports are generated at: `app/build/reports/tests/testDebugUnitTest/index.html`

## Architecture

**Clean Architecture with MVVM**

```
Presentation → Domain → Data
     ↓           ↓        ↓
ViewModels   Models    Repositories → Room DAOs → SQLite
```

### Source Structure

```
app/src/main/java/com/fino/app/
├── data/
│   ├── local/dao/        # Room DAOs (TransactionDao, CategoryDao, etc.)
│   ├── local/entity/     # Room entities with @Entity annotations
│   └── repository/       # Repositories wrapping DAOs, entity↔domain mapping
├── di/                   # Hilt modules (DatabaseModule, RepositoryModule, ServiceModule, AppModule)
├── domain/model/         # Domain models (Transaction, Category, UpcomingBill, etc.)
├── gamification/         # XP system (LevelCalculator, StreakTracker, AchievementTracker)
├── ml/matcher/           # Fuzzy matching (FuzzyMatcher, MerchantMatcher)
├── presentation/
│   ├── components/       # Reusable Compose components
│   ├── navigation/       # FinoNavigation.kt with Screen sealed class
│   ├── screens/          # Screen composables
│   ├── theme/            # Colors, typography, gradients
│   └── viewmodel/        # @HiltViewModel classes with UiState data classes
├── service/
│   ├── parser/           # SMS parsers (SmsParser, UpiTransactionParser, CreditCardTransactionParser)
│   └── pattern/          # PatternDetectionService for recurring bill detection
└── util/                 # Utilities (AmountFormatter, etc.)
```

### Key Patterns

**ViewModels**: Each screen has a ViewModel with a `UiState` data class and `StateFlow`:
```kotlin
data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
```

**Entity↔Domain Mapping**: Entities have `toDomain()` extensions; domain models have `toEntity()`:
```kotlin
fun TransactionEntity.toDomain(): Transaction
fun Transaction.toEntity(): TransactionEntity
```

**Date Storage**: Room stores dates as `Long` (epoch milliseconds). Use converters:
```kotlin
localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
```

### Navigation

Routes defined in `FinoNavigation.kt` as sealed class:
- Home, Cards, Analytics, Rewards, Settings
- AddTransaction, UpcomingBills, AddRecurringBill

### Dependency Injection

Hilt modules in `di/` package:
- `DatabaseModule`: Provides FinoDatabase and all DAOs
- `RepositoryModule`: Provides repository instances
- `ServiceModule`: Provides PatternDetectionService
- `AppModule`: Provides SmsParser, FuzzyMatcher, MerchantMatcher, gamification services

## Testing

**Framework**: JUnit 4 + Mockito-Kotlin + Coroutines Test + Turbine (for Flow testing)

**Test Structure**: Mirror main source structure under `app/src/test/java/`

**ViewModel Testing Pattern**:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test description`() = runTest {
        // Setup mocks with whenever()
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert on uiState.first()
    }
}
```

**Mocking suspend functions in @Before**: Wrap in `runBlocking`:
```kotlin
@Before
fun setup() {
    runBlocking {
        whenever(mockRepo.getSuspendData()).thenReturn(testData)
    }
}
```

## SMS Parsing

`SmsParser` handles Indian bank SMS formats:
- UPI transactions (HDFC, SBI, ICICI, Axis)
- Credit card transactions
- Credit card bills

Parsers use regex patterns with confidence scoring. Fuzzy matching (Levenshtein distance) categorizes merchants:
- ≥95% similarity: auto-apply category
- 70-95%: suggest with confirmation
- <70%: no match

## UI Theme

Dark theme with gradient accents. Key colors in `presentation/theme/`:
- `DarkBackground`, `DarkSurface`, `DarkSurfaceVariant`
- `Primary` (purple gradient), `Secondary` (cyan gradient)
- `ExpenseRed`, `IncomeGreen`, `Warning`, `Info`

Use `FinoGradients` object for gradient brushes.
