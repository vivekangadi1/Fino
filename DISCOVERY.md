# Fino Discovery Document

## Project Overview

**App Name:** Fino
**Tagline:** "Track Smart. Spend Wise."
**Platform:** Android (Kotlin Native)
**Distribution:** Sideload/APK (not Play Store)
**Min SDK:** 26 (Android 8.0)
**Target SDK:** 34 (Android 14)

Fino is an intelligent expense tracking app that:
- Automatically reads SMS transaction messages
- Learns user categorization patterns
- Tracks credit card bills
- Detects recurring subscriptions
- Provides gamified budget management
- Keeps all data completely local and private

---

## Architecture Overview

### Tech Stack
- **Language:** Kotlin 1.9.22
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room 2.6.1
- **DI:** Hilt 2.50
- **Navigation:** Navigation Compose 2.7.7
- **Charts:** Vico 1.13.1
- **Background:** WorkManager 2.9.0
- **Security:** Biometric, EncryptedSharedPreferences

### Architecture Pattern
- Clean Architecture with MVVM
- Repository pattern for data access
- Use Cases for business logic
- Dependency Injection with Hilt

### Layer Structure
```
presentation/     - UI (Compose), ViewModels
domain/           - Models, Use Cases, Repository interfaces
data/             - Room entities, DAOs, Repository implementations
service/          - SMS parsing, notifications, backup
ml/               - Merchant matching, categorization
gamification/     - XP, levels, streaks, achievements
```

---

## Data Models

### Transaction
Core transaction record with 18 fields:
- id, amount, type (DEBIT/CREDIT)
- merchantName, merchantNormalized
- categoryId, subcategoryId, creditCardId
- isRecurring, recurringRuleId
- rawSmsBody, smsSender, parsedConfidence
- needsReview, transactionDate, createdAt
- source (SMS/MANUAL), reference

### Category
Hierarchical categories with 8 fields:
- id, name, emoji
- parentId (null for top-level)
- isSystem, budgetLimit
- sortOrder, isActive

### MerchantMapping
Learned associations with 10 fields:
- id, rawMerchantName, normalizedName
- categoryId, subcategoryId
- confidence (0.0-1.0), matchCount
- isFuzzyMatch, createdAt, lastUsedAt

### CreditCard
Credit card tracking with 13 fields:
- id, bankName, cardName, lastFourDigits
- creditLimit, billingCycleDay, dueDateDay
- currentUnbilled, previousDue, previousDueDate
- minimumDue, isActive, createdAt

### RecurringRule
Subscription detection with 13 fields:
- id, merchantPattern, categoryId
- expectedAmount, amountVariance
- frequency (WEEKLY/MONTHLY/YEARLY)
- dayOfPeriod, lastOccurrence, nextExpected
- occurrenceCount, isActive, isUserConfirmed, createdAt

### Budget
Monthly category budgets with 7 fields:
- id, categoryId, monthlyLimit
- month (YearMonth), alertAt75, alertAt100, createdAt

### UserStats
Gamification stats (singleton) with 8 fields:
- id (always 1), currentStreak, longestStreak
- totalTransactionsLogged, totalXp, currentLevel
- lastActiveDate, createdAt

### Achievement
Unlockable achievements with 9 fields:
- id, name, description, emoji
- xpReward, requirement, type
- unlockedAt (null if locked), progress

---

## Category Hierarchy

### Top-Level Categories (11)
1. Food & Dining (ID: 1)
2. Pet Care (ID: 10)
3. Housing & Utilities (ID: 20)
4. Subscriptions (ID: 30)
5. Transport (ID: 40)
6. Health & Medical (ID: 50)
7. Shopping (ID: 60)
8. Financial (ID: 70)
9. Entertainment (ID: 80)
10. Education (ID: 90)
11. Transfers & Others (ID: 100)

### Subcategories (~40 total)
- Food: Groceries, Meat & Seafood, Restaurants, Food Delivery, Snacks
- Pet: Pet Food, Vet & Medical, Grooming, Accessories
- Housing: Rent, Electricity, Water, Gas, Internet, Maintenance
- Subscriptions: Mobile Recharge, Streaming, Cloud Storage, Apps & Games
- Transport: Fuel, Cab & Auto, Public Transport, Parking, Vehicle Maintenance
- Health: Doctor, Pharmacy, Lab Tests, Health Insurance
- Shopping: Online, Clothing, Electronics, Home & Kitchen, Personal Care
- Financial: EMI, Insurance, Investments, Credit Card Payment, Bank Charges
- Entertainment: Movies & Events, Gaming, Sports & Fitness
- Education: Courses, Books, Supplies
- Transfers: Family & Friends, Gifts, ATM Withdrawal, Donations, Misc

---

## SMS Patterns Supported

### UPI Transactions
| Bank | Format Example |
|------|----------------|
| HDFC | "Paid Rs.350.00 to MERCHANT on DD-MM-YY using UPI. UPI Ref: 123456. -HDFC Bank" |
| SBI | "Rs.1200 debited from A/c XX1234 to VPA merchant@upi on DD-MM-YY. UPI Ref 123456 -SBI" |
| ICICI | "INR 499.00 debited from A/c XX1234 on DD-MM-YY for UPI to merchant@ybl. Ref 123456" |
| Axis | "INR 899.00 debited from A/c no. XX4321 on DD-Mon-YY for UPI-MERCHANT. UPI Ref: 123456" |

### Credit Card Transactions
| Bank | Format Example |
|------|----------------|
| HDFC | "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at MERCHANT on DD-MM-YY" |
| ICICI | "Alert: ICICI Card ending 8976 used for INR 5550.00 at MERCHANT on DD-Mon-YY" |
| SBI | "Your SBI Card ending 3456 was used for Rs.649 at MERCHANT on DD/MM/YYYY" |
| Axis | "Your Axis Bank Credit Card ending 7890 was used for Rs.899 at MERCHANT on DD-Mon-YY" |

### Credit Card Bills
| Bank | Format Example |
|------|----------------|
| HDFC | "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: DD-Mon-YY" |
| ICICI | "Your ICICI Credit Card XX8976 bill is Rs.8,750. Min Due: Rs.438. Due Date: DD-Mon-YY" |
| SBI | "SBI Card XX3456 Statement: Total Due Rs.15,600, Min Due Rs.780, Due by DD-Mon-YY" |

### Subscriptions Detected
- Google Play (YouTube Premium, Google One)
- Netflix
- Amazon Prime
- Spotify

### Non-Transaction SMS (Ignored)
- OTP messages
- Promotional offers
- Balance inquiries
- Payment reminders
- Thank you messages

---

## Gamification System

### Levels (8)
| Level | Name | XP Range |
|-------|------|----------|
| 1 | Budding Saver | 0-99 |
| 2 | Money Tracker | 100-299 |
| 3 | Smart Spender | 300-599 |
| 4 | Budget Boss | 600-999 |
| 5 | Money Master | 1000-1499 |
| 6 | Finance Ninja | 1500-2199 |
| 7 | Wealth Wizard | 2200-2999 |
| 8 | Fino Legend | 3000+ |

### XP Rewards
| Action | XP |
|--------|-----|
| Categorize transaction | 5 |
| Same-day categorization bonus | 3 |
| Confirm fuzzy match | 10 |
| Reject fuzzy match | 5 |
| Add manual transaction | 8 |
| Set budget | 20 |
| Stay under budget (week) | 10 |
| Review weekly summary | 15 |
| First transaction of day | 5 |
| Add credit card | 25 |
| Identify recurring expense | 15 |
| Complete onboarding | 50 |

### Achievements (20+)
**Streak:** 3-day, 7-day, 14-day, 30-day, 100-day, 365-day
**Transactions:** 10, 50, 100, 500, 1000
**Budget:** First budget, 5 budgets, 1/3/6 months under budget
**Credit Card:** First card, 3 cards
**Recurring:** 3, 5, 10 detected

---

## Project Structure

```
fino/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fino/app/
│   │   │   │   ├── data/local/{dao,entity,database}
│   │   │   │   ├── data/repository/
│   │   │   │   ├── domain/{model,usecase}
│   │   │   │   ├── presentation/{ui,viewmodel,navigation,theme}
│   │   │   │   ├── service/{sms,parser,notification,backup}
│   │   │   │   ├── ml/{matcher}
│   │   │   │   ├── gamification/
│   │   │   │   ├── di/
│   │   │   │   ├── util/
│   │   │   │   └── FinoApplication.kt
│   │   │   ├── res/{values,drawable,xml}
│   │   │   └── AndroidManifest.xml
│   │   ├── test/java/com/fino/app/ (unit tests)
│   │   └── androidTest/ (instrumented tests)
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── tests.json
├── progress.txt
├── DISCOVERY.md
└── PLAN.md
```

---

## Dependencies

### Core
- androidx.core:core-ktx:1.12.0
- androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
- androidx.activity:activity-compose:1.8.2

### Compose
- androidx.compose:compose-bom:2024.02.00
- Material 3, Navigation Compose

### Database
- androidx.room:room-runtime:2.6.1
- androidx.room:room-ktx:2.6.1

### DI
- com.google.dagger:hilt-android:2.50
- androidx.hilt:hilt-navigation-compose:1.1.0

### Background
- androidx.work:work-runtime-ktx:2.9.0
- androidx.hilt:hilt-work:1.1.0

### Security
- androidx.biometric:biometric:1.1.0
- androidx.security:security-crypto:1.1.0-alpha06

### Charts
- com.patrykandpatrick.vico:compose-m3:1.13.1

### Testing
- junit:junit:4.13.2
- org.mockito.kotlin:mockito-kotlin:5.2.1
- com.google.truth:truth:1.1.5
- app.cash.turbine:turbine:1.0.0

---

## Key Design Decisions

1. **Local-only data:** No cloud sync, all data on device
2. **Regex-based parsing:** Primary approach for SMS parsing
3. **Fuzzy matching:** Levenshtein distance with 0.7 threshold
4. **Hierarchical categories:** Parent-child structure for organization
5. **Gamification:** Incentivize consistent usage
6. **Material 3:** Modern, accessible design
7. **Clean Architecture:** Separation of concerns for testability

---

## Files Created: ~70+ files

Phase 0 Status: COMPLETE
Next: Phase 1 (Planning) - Create PLAN.md with 9 milestones
