# Changelog

All notable changes to the Fino Smart Expense Tracker will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Email receipt parsing
- Multi-currency support
- Cloud sync (opt-in)
- Widgets

---

## [1.0.0] - 2024-12-16

### Added

#### Core Features
- Automatic SMS parsing for Indian banks (HDFC, SBI, ICICI, Axis)
- UPI transaction detection with merchant extraction
- Credit card transaction parsing
- Credit card bill parsing with due dates
- Subscription detection (Netflix, Spotify, Amazon Prime, Google Play)
- Manual transaction entry
- Transaction categorization with 11 categories and ~40 subcategories

#### Smart Categorization
- Merchant learning system - remembers your category choices
- Fuzzy matching using Levenshtein distance algorithm
- Confidence-based auto-categorization
- Uncategorized transaction queue for review

#### Credit Card Management
- Add and track multiple credit cards
- Bill amount and due date tracking
- Statement cycle management
- Payment reminders

#### Recurring Expense Detection
- Automatic pattern detection (weekly, monthly, yearly)
- Subscription identification
- Renewal date predictions
- Amount variance tolerance (5%)

#### Budgeting
- Category-based monthly budgets
- Real-time spending tracking
- 75% warning threshold
- 100% overspend alert
- Visual progress indicators

#### Analytics
- Monthly spending pie chart
- Category breakdown
- Month-over-month trends
- CSV export functionality

#### Gamification
- 8-level progression system (Budding Saver to Fino Legend)
- XP rewards for various actions
- 20+ unlockable achievements
- Daily streak tracking
- Achievement gallery

#### Privacy & Security
- 100% local data storage
- Biometric lock support (fingerprint/face)
- Encrypted backup/restore
- No network calls or analytics

### Technical Details

#### Architecture
- Clean Architecture with MVVM pattern
- Jetpack Compose with Material 3
- Room database with 8 entities
- Hilt dependency injection
- Kotlin Coroutines and Flow

#### Testing
- 78 unit tests following TDD methodology
- Test coverage for all core components:
  - SmsParser (30 tests)
  - FuzzyMatcher (8 tests)
  - MerchantMatcher (10 tests)
  - StreakTracker (6 tests)
  - LevelCalculator (11 tests)
  - AchievementTracker (4 tests)
  - AmountFormatterTest (9 tests)

#### Dependencies
- Kotlin 1.9.22
- Compose BOM 2024.02.00
- Room 2.6.1
- Hilt 2.50
- Vico Charts 1.13.1
- WorkManager 2.9.0

### Known Issues
- None at this time

### Notes
- Minimum Android version: 8.0 (API 26)
- Target Android version: 14 (API 34)
- Built and tested using TDD (Test-Driven Development) methodology

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2024-12-16 | Initial release with full feature set |

---

## Upgrade Notes

### Upgrading to 1.0.0
This is the initial release. No upgrade path required.

---

## Contributors
- Development following TDD methodology
- Built with Claude Code assistance
