# Fino Implementation Plan

## Milestone Dependency Graph

```
M1 (Core Foundation)
 └─► M2 (SMS Parsing)
      └─► M3 (Merchant Learning)
           └─► M4 (Fuzzy Matching)
                └─► M5 (Credit Cards) ─► M6 (Recurring)
                     └─► M7 (Budgeting)
                          └─► M8 (Analytics)
                               └─► M9 (Gamification & Polish)
```

---

## Milestone 1: Core Foundation

**Goal:** Set up project infrastructure with database, basic UI shell, and manual transaction entry.

### Tasks:
1. Initialize Android project with Gradle configuration
2. Configure Hilt dependency injection
3. Set up Room database with all entities
4. Create category hierarchy with default categories
5. Build basic navigation shell (Home, Cards, Analytics, Settings)
6. Implement manual transaction entry screen
7. Create category picker with hierarchical display
8. Build transaction list with date grouping

### Test Scenarios:
- Database CRUD operations for all entities
- Category hierarchy traversal
- Transaction insertion and retrieval
- Date-based transaction grouping

### Edge Cases:
- Empty transaction list
- Very long merchant names
- Negative amounts (refunds)
- Transactions at midnight (date boundary)

### Success Criteria:
- [ ] Room database initializes without errors
- [ ] All default categories are seeded
- [ ] Manual transaction can be added with category
- [ ] Transaction list displays with correct grouping
- [ ] Navigation between screens works

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Room schema conflicts | Medium | Medium | Use migrations or fallback |
| Compose UI issues | Low | Low | Follow Material 3 guidelines |

### Rollback Plan:
Git revert to last working commit. Database: uninstall app.

---

## Milestone 2: SMS Reading & Parsing

**Goal:** Automatically read SMS messages and parse transaction details with regex patterns.

**Dependencies:** Milestone 1

### Tasks:
1. Implement SMS permission request flow
2. Create SmsReceiver BroadcastReceiver
3. Build regex patterns for UPI transactions (HDFC, SBI, ICICI, Axis)
4. Build regex patterns for Credit Card transactions
5. Implement amount parsing (Rs., INR, commas)
6. Implement date parsing (multiple formats)
7. Create uncategorized transaction queue
8. Add confidence scoring to parsed results
9. Filter non-transaction SMS (OTP, promo, balance)

### Test Scenarios:
- Parse each bank's UPI format correctly
- Parse each bank's CC format correctly
- Handle amounts with/without decimals
- Handle amounts with Indian comma format (1,25,000)
- Reject promotional SMS
- Reject OTP messages

### Edge Cases:
- SMS with special characters (CAFÉ, T.NAGAR)
- Very large amounts (99,99,999)
- Missing date in SMS
- Duplicate SMS received
- SMS from unknown sender format

### Success Criteria:
- [ ] SMS permission granted on first launch
- [ ] UPI transactions from 4 banks parsed correctly
- [ ] CC transactions from 4 banks parsed correctly
- [ ] Non-transaction SMS ignored
- [ ] Confidence score reflects parsing certainty
- [ ] Parsed transactions appear in uncategorized queue

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| New SMS format not matched | High | Medium | Fallback to ML Kit |
| Permission denied by user | Medium | High | Graceful degradation to manual |

### Rollback Plan:
Disable SMS receiver, fall back to manual entry only.

---

## Milestone 3: Merchant Learning

**Goal:** Learn merchant → category associations and auto-categorize repeat transactions.

**Dependencies:** Milestone 2

### Tasks:
1. Create MerchantMapping repository
2. Store mapping when user categorizes transaction
3. Look up mapping for new transactions
4. Auto-apply high-confidence mappings
5. Increment confidence on repeated confirmations
6. Update lastUsedAt timestamp
7. Handle merchant name normalization (uppercase, trim)

### Test Scenarios:
- First categorization creates mapping
- Second occurrence auto-categorizes
- Confidence increases with use
- Case-insensitive matching works

### Edge Cases:
- Same merchant, different categories (Swiggy: groceries vs restaurant)
- Merchant name variations (AMAZON vs AMAZON PRIME)
- User changes category after auto-categorization

### Success Criteria:
- [ ] Categorizing creates merchant mapping
- [ ] Repeat merchant auto-categorizes
- [ ] Confidence score increases with each confirmation
- [ ] User can override auto-categorization

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Wrong auto-categorization | Medium | Low | Allow easy correction |

### Rollback Plan:
Clear merchant_mappings table, start fresh.

---

## Milestone 4: Fuzzy Matching

**Goal:** Suggest categories for similar merchant names using Levenshtein distance.

**Dependencies:** Milestone 3

### Tasks:
1. Implement Levenshtein distance algorithm
2. Calculate string similarity score (0.0-1.0)
3. Find best match from existing mappings
4. Create confirmation dialog for fuzzy matches
5. Learn from user confirmations
6. Learn from user rejections (don't suggest again)
7. Define thresholds (0.7 for suggestion, 0.95 for auto)

### Test Scenarios:
- Identical strings return 1.0
- Similar strings (CHICKEN SHOP, CHICKEN STORE) return >0.7
- Different strings (AMAZON, NETFLIX) return <0.3
- Case insensitivity works
- Extra whitespace normalized

### Edge Cases:
- Very short merchant names (2-3 chars)
- Very long merchant names (50+ chars)
- All-caps vs mixed case
- Multiple good matches

### Success Criteria:
- [ ] Fuzzy matches suggested for similar merchants
- [ ] Confirmation dialog shows suggestion
- [ ] Confirmed match creates new mapping
- [ ] Rejected match doesn't create mapping

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Too many false positives | Medium | Low | Adjust threshold |
| Slow with many mappings | Low | Low | Limit comparison set |

### Rollback Plan:
Disable fuzzy matching, require exact matches only.

---

## Milestone 5: Credit Card Tracking

**Goal:** Track credit cards, link transactions, and parse bill SMS.

**Dependencies:** Milestone 4

### Tasks:
1. Create CreditCard entity and DAO
2. Build Add Card screen
3. Link credit card transactions by last 4 digits
4. Parse credit card bill SMS
5. Update card with bill details (due, minimum, date)
6. Calculate upcoming dues
7. Show credit card summary on Cards screen
8. Bill payment reminder notifications

### Test Scenarios:
- Add card with all details
- Transaction links to correct card
- Bill SMS updates card details
- Multiple cards tracked separately

### Edge Cases:
- Two cards with same last 4 digits (different banks)
- Card transaction before card added
- Bill SMS without minimum due

### Success Criteria:
- [ ] Credit card can be added
- [ ] Transactions link to cards automatically
- [ ] Bill SMS updates due amounts
- [ ] Cards screen shows all cards with status
- [ ] Due dates calculated correctly

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Bill SMS format varies | High | Medium | Multiple patterns |
| Wrong card linked | Low | Medium | Show confirmation |

### Rollback Plan:
Delete card data, transactions remain unlinked.

---

## Milestone 6: Recurring Detection

**Goal:** Automatically detect subscriptions and recurring expenses.

**Dependencies:** Milestone 5

### Tasks:
1. Create RecurringRule entity and DAO
2. Detect patterns from transaction history
3. Identify weekly/monthly/yearly frequency
4. Calculate expected day of period
5. Handle amount variance (10% tolerance)
6. Predict next occurrence date
7. Create subscription management UI
8. Flag transactions as recurring

### Test Scenarios:
- Monthly Netflix detected after 3 occurrences
- Weekly grocery detected
- Yearly Amazon Prime detected
- Amount variance within tolerance matches

### Edge Cases:
- Irregular subscription timing
- Price changes mid-subscription
- Subscription cancelled
- One-time purchase at same merchant

### Success Criteria:
- [ ] Recurring expenses auto-detected
- [ ] Next renewal date predicted
- [ ] Subscriptions listed on dedicated screen
- [ ] User can confirm/reject detection

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| False positives | Medium | Low | Require user confirmation |
| Missed subscriptions | Medium | Low | Lower detection threshold |

### Rollback Plan:
Clear recurring_rules, disable auto-detection.

---

## Milestone 7: Budgeting

**Goal:** Set and track category budgets with alerts.

**Dependencies:** Milestone 6

### Tasks:
1. Create Budget entity and DAO
2. Build budget setting screen per category
3. Calculate spending vs budget for current month
4. Show progress bars on home screen
5. Implement 75% warning alert
6. Implement 100% over-budget alert
7. Calculate budget health including upcoming bills
8. Historical budget tracking

### Test Scenarios:
- Budget created for category
- Spending correctly calculated
- Alert triggers at 75%
- Alert triggers at 100%
- Budget health includes credit card dues

### Edge Cases:
- No budget set for category
- Budget set mid-month
- Negative remaining (over budget)
- Category with subcategories

### Success Criteria:
- [ ] Budget can be set per category
- [ ] Progress shown accurately
- [ ] Alerts trigger at thresholds
- [ ] Budget health considers upcoming bills

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Confusing UI | Low | Medium | User testing |

### Rollback Plan:
Clear budget data, disable budget feature.

---

## Milestone 8: Analytics

**Goal:** Provide spending insights with charts and export capability.

**Dependencies:** Milestone 7

### Tasks:
1. Monthly spending pie chart by category (Vico)
2. Weekly/monthly trend line chart
3. Category deep-dive with transaction list
4. Month-over-month comparison
5. Top merchants list
6. Export transactions to CSV
7. Date range selector
8. Quick filters (this week, this month, custom)

### Test Scenarios:
- Pie chart renders with category breakdown
- Trend shows spending over time
- CSV export contains all fields
- Date filtering works correctly

### Edge Cases:
- No transactions in period
- All transactions in one category
- Very many transactions (1000+)

### Success Criteria:
- [ ] Pie chart displays category breakdown
- [ ] Trend analysis shows comparison
- [ ] Export creates valid CSV
- [ ] Charts render smoothly

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Chart library issues | Low | Medium | Fallback to simple bars |
| Large data performance | Low | Medium | Pagination, caching |

### Rollback Plan:
Disable charts, show simple lists only.

---

## Milestone 9: Gamification & Polish

**Goal:** Add engagement features, onboarding, security, and polish.

**Dependencies:** Milestone 8

### Tasks:
1. XP reward system for actions
2. Level progression (8 levels)
3. Streak tracking for consecutive days
4. Achievement unlocking (20+ achievements)
5. Onboarding flow for new users
6. Biometric app lock
7. Backup to local file
8. Restore from backup
9. Settings screen polish
10. Performance optimization
11. Error handling improvements
12. Final UI polish

### Test Scenarios:
- XP awarded for categorization
- Level up triggers
- Streak increases on consecutive days
- Streak resets on missed day
- Achievement unlocks at threshold
- Backup creates valid file
- Restore recovers all data

### Edge Cases:
- First-time user experience
- Biometric not available
- Backup file corrupted
- Very long streak (365+ days)

### Success Criteria:
- [ ] XP system working
- [ ] Levels calculated correctly
- [ ] Streaks tracked accurately
- [ ] Achievements unlock properly
- [ ] Onboarding guides new users
- [ ] Biometric lock works
- [ ] Backup/restore functional

### Risks:
| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Biometric API issues | Low | Low | Fallback to PIN |
| Backup corruption | Low | High | Validation, checksums |

### Rollback Plan:
Disable gamification, basic app still works.

---

## Critical Path

```
M1 → M2 → M3 → M4 → M5 → M7 → M8 → M9
                     └─► M6 ─┘
```

Milestones 5 and 6 can be developed in parallel.

---

## Risk Register

| ID | Risk | Phase | Likelihood | Impact | Mitigation |
|----|------|-------|------------|--------|------------|
| R1 | SMS format changes | M2 | High | Medium | Multiple patterns, ML fallback |
| R2 | Permission denied | M2 | Medium | High | Graceful degradation |
| R3 | Wrong categorization | M3-4 | Medium | Low | Easy correction |
| R4 | Database corruption | All | Low | High | Regular backups |
| R5 | Performance issues | M8 | Low | Medium | Lazy loading, caching |
| R6 | Chart rendering | M8 | Low | Low | Fallback to lists |
| R7 | Biometric failure | M9 | Low | Low | PIN fallback |

---

## Test Summary

| Milestone | Unit Tests | Integration | UI Tests |
|-----------|------------|-------------|----------|
| M1 | 15 | 5 | 5 |
| M2 | 30 | 5 | 3 |
| M3 | 10 | 3 | 2 |
| M4 | 10 | 3 | 2 |
| M5 | 10 | 5 | 3 |
| M6 | 8 | 3 | 2 |
| M7 | 8 | 3 | 3 |
| M8 | 5 | 3 | 5 |
| M9 | 15 | 5 | 5 |
| **Total** | **111** | **35** | **30** |

---

## Phase 1 Status: COMPLETE

Next: Phase 2 - Write failing tests (~85 tests)
