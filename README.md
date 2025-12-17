# Fino - Smart Expense Tracker

A privacy-first Android expense tracker that automatically reads and categorizes SMS transactions, learns your spending patterns, and gamifies financial wellness.

## Features

### Automatic SMS Parsing
- **Indian Bank Support**: HDFC, SBI, ICICI, Axis
- **Transaction Types**: UPI, Credit Card, IMPS/NEFT
- **Smart Detection**: Automatically detects subscriptions (Netflix, Spotify, Amazon Prime, etc.)
- **Bill Tracking**: Parses credit card bill SMS for due dates and amounts

### Intelligent Categorization
- **11 Categories** with ~40 subcategories
- **Merchant Learning**: Remembers how you categorize merchants
- **Fuzzy Matching**: Suggests categories for similar merchant names
- **Confidence Scoring**: Higher confidence = auto-apply, lower = ask for confirmation

### Credit Card Tracking
- Track multiple credit cards
- Parse bill SMS automatically
- View upcoming dues and payment dates
- Statement cycle tracking

### Recurring Expense Detection
- Detect weekly, monthly, and yearly patterns
- Subscription management
- Renewal date predictions
- Amount variance tolerance (5%)

### Budgeting
- Set monthly budgets by category
- Real-time spending vs budget tracking
- Alert at 75% and 100% thresholds
- Visual progress indicators

### Analytics
- Monthly spending breakdown (pie chart)
- Category analysis
- Trend tracking (month-over-month)
- CSV export

### Gamification
- **8 Levels**: Budding Saver → Fino Legend
- **20+ Achievements**: Streak, transaction count, budget, etc.
- **Daily Streaks**: Track consecutive days of logging
- **XP Rewards**: Earn XP for categorizing, staying under budget, etc.

### Privacy & Security
- **100% Local Data**: No cloud sync, no data collection
- **Biometric Lock**: Fingerprint/Face ID protection
- **Encrypted Backup**: Export/import your data securely

## Requirements

- Android 8.0+ (API 26)
- SMS permission for automatic parsing
- Biometric hardware (optional, for lock feature)

## Tech Stack

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room 2.6.1
- **DI**: Hilt 2.50
- **Charts**: Vico 1.13.1
- **Build**: Gradle 8.5, AGP 8.2.2

## Project Structure

```
app/src/main/java/com/fino/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room entities
│   │   └── database/     # Database definition
│   └── repository/       # Repository implementations
├── domain/
│   └── model/            # Domain models & enums
├── gamification/         # XP, levels, streaks, achievements
├── ml/
│   └── matcher/          # Fuzzy matching
├── presentation/
│   └── ui/               # Compose UI components
├── service/
│   └── parser/           # SMS parsers
└── util/                 # Utilities
```

## Building

```bash
# Clone the repository
git clone https://github.com/yourusername/fino.git
cd fino

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

## Testing

The project follows strict TDD methodology with 78+ unit tests:

```bash
# Run all tests
./gradlew test

# Run specific test suite
./gradlew test --tests "SmsParserTest"

# Generate coverage report
./gradlew jacocoTestReport
```

### Test Coverage

| Component | Tests |
|-----------|-------|
| SmsParser | 30 |
| FuzzyMatcher | 8 |
| MerchantMatcher | 10 |
| StreakTracker | 6 |
| LevelCalculator | 11 |
| AchievementTracker | 4 |
| AmountFormatter | 9 |

## SMS Patterns Supported

### UPI Transactions
```
HDFC: "Paid Rs.350.00 to MERCHANT on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"
SBI:  "Rs.1200 debited from A/c XX1234 to VPA merchant@upi on 14-12-24. UPI Ref 123456 -SBI"
ICICI: "INR 499.00 debited from A/c XX1234 on 14-12-24 for UPI to merchant. Ref 123456"
```

### Credit Card Transactions
```
HDFC: "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24"
ICICI: "Alert: ICICI Card ending 8976 used for INR 5550.00 at CROMA on 14-Dec-24"
SBI: "Your SBI Card ending 3456 was used for Rs.649 at NETFLIX.COM on 14/12/2024"
```

### Credit Card Bills
```
"Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"
```

## Categories

| ID | Category | Subcategories |
|----|----------|---------------|
| 1 | Food & Dining | Restaurants, Groceries, Coffee, Delivery |
| 2 | Pet Care | Food, Vet, Grooming, Supplies |
| 3 | Housing | Rent, Utilities, Maintenance, Insurance |
| 4 | Subscriptions | Streaming, Software, News, Gaming |
| 5 | Transport | Fuel, Public Transit, Ride Share, Parking |
| 6 | Health | Medical, Pharmacy, Fitness, Insurance |
| 7 | Shopping | Clothing, Electronics, Home, Beauty |
| 8 | Financial | EMI, Insurance, Investment, Tax |
| 9 | Entertainment | Movies, Events, Hobbies, Travel |
| 10 | Education | Courses, Books, Software |
| 11 | Transfers | P2P, Bank, ATM |

## Gamification

### Levels
| Level | Name | XP Required |
|-------|------|-------------|
| 1 | Budding Saver | 0 |
| 2 | Money Tracker | 100 |
| 3 | Smart Spender | 300 |
| 4 | Budget Boss | 600 |
| 5 | Money Master | 1000 |
| 6 | Finance Ninja | 1500 |
| 7 | Wealth Wizard | 2200 |
| 8 | Fino Legend | 3000 |

### XP Actions
| Action | XP |
|--------|-----|
| Categorize transaction | 5 |
| Confirm fuzzy match | 10 |
| Set budget | 20 |
| Add credit card | 25 |
| Complete onboarding | 50 |

## License

MIT License - see [LICENSE](LICENSE) file.

## Contributing

Contributions welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

## Acknowledgments

- Material 3 Design System
- Vico Charts Library
- JetBrains for Kotlin
