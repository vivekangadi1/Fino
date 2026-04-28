# TDD Prompt: Recurring Bills, Autopay Detection & Upcoming Bill Prediction

---

<context_and_motivation>
## Context & Motivation

**Project:** Fino - Android expense tracking app with SMS-based transaction parsing.

**Problem:** Recurring bills and autopay transactions are not appearing in the "upcoming bills" section. Users cannot see predicted expenses for next month based on their recurring payment patterns.

**Why this matters:** A key value proposition of Fino is helping users understand their future financial obligations. Without recurring bill detection, users are surprised by autopay charges and cannot plan their monthly budget effectively.

**Your mission:** 
1. Analyze ALL historical SMS to identify recurring transaction patterns
2. Detect autopay/auto-debit setups
3. Identify subscription services (Netflix, Spotify, YouTube Premium, etc.)
4. Identify recurring bills (electricity, broadband, mobile, rent, EMI)
5. Predict and display upcoming bills for next month
6. Ensure these appear in the app's upcoming bills UI
</context_and_motivation>

---

<problem_statement>
## Problem Statement

**Current State:**
- SMS parser may detect individual transactions
- Recurring patterns are NOT being identified
- Autopay transactions are NOT flagged
- Upcoming bills section is empty or incomplete
- Users cannot see predicted next month expenses

**Expected State:**
- All recurring transactions identified from history
- Autopay/auto-debit transactions flagged
- Subscriptions detected and tracked
- Upcoming bills section shows:
  - Bill name/merchant
  - Expected amount
  - Expected date
  - Payment method (autopay vs manual)
  - Frequency (weekly/monthly/quarterly/yearly)
- Next month total predicted expense calculated

**Scope:** This is a feature fix/enhancement following TDD methodology.
</problem_statement>

---

<recurring_transaction_types>
## Types of Recurring Transactions to Detect

### 1. Subscriptions (Auto-charged)
- **Streaming:** Netflix, Amazon Prime Video, Hotstar, Sony LIV, Zee5
- **Music:** Spotify, YouTube Music, Apple Music, Gaana, JioSaavn
- **Cloud/Storage:** Google One, iCloud, Dropbox
- **Apps:** YouTube Premium, Google Play Pass, Apple Arcade
- **News/Reading:** Kindle Unlimited, Audible, newspaper subscriptions
- **Software:** Adobe, Microsoft 365, Notion, productivity apps
- **Gaming:** PlayStation Plus, Xbox Game Pass

### 2. Utility Bills (May be autopay or manual)
- **Electricity:** TANGEDCO, BESCOM, MSEDCL, Tata Power, etc.
- **Gas:** Piped gas, cylinder booking
- **Water:** Municipal water bills
- **Internet/Broadband:** Airtel, Jio Fiber, ACT, BSNL
- **Mobile Postpaid:** Airtel, Jio, Vi, BSNL
- **DTH:** Tata Play, Airtel Digital TV, Dish TV

### 3. Financial Recurring
- **EMI Payments:** Loan EMIs, credit card EMIs
- **Insurance Premiums:** Health, life, vehicle insurance
- **SIP/Investments:** Mutual fund SIPs
- **Credit Card Bills:** Monthly bill payments

### 4. Living Expenses
- **Rent:** Monthly rent payments
- **Maintenance:** Society maintenance charges
- **Household Help:** Maid, cook, driver salary

### 5. Autopay Indicators in SMS
Look for keywords:
- "auto-debit", "autopay", "auto pay", "standing instruction"
- "SI", "mandate", "recurring payment"
- "subscription", "renewal", "renewed"
- "monthly charge", "annual charge"
</recurring_transaction_types>

---

<autonomous_operation_rules>
## Autonomous Operation Rules

### DO:
- ✅ Start with Phase 0 discovery - understand current recurring detection implementation
- ✅ Pull ALL historical SMS via ADB (go back as far as possible)
- ✅ Analyze transaction patterns over time (same merchant + similar amount + regular interval)
- ✅ Identify autopay keywords in SMS
- ✅ Write tests BEFORE implementing new detection logic
- ✅ Follow existing code patterns in the project
- ✅ Ensure recurring transactions appear in upcoming bills UI
- ✅ Calculate next occurrence date based on historical pattern
- ✅ Handle different frequencies (weekly, monthly, quarterly, yearly)

### DO NOT:
- ❌ Skip discovery phase
- ❌ Write implementation before tests
- ❌ Hardcode subscription lists - detect from actual SMS patterns
- ❌ Ignore edge cases (variable amounts, skipped months)
- ❌ Break existing SMS parsing functionality
- ❌ Assume all recurring transactions are monthly
</autonomous_operation_rules>

---

<decision_framework>
## Decision Framework

### Identifying Recurring Patterns:
A transaction is likely recurring if:
1. **Same merchant** (exact or fuzzy match) appears 2+ times
2. **Similar amount** (within 10% variance for utilities, exact for subscriptions)
3. **Regular interval** detected:
   - Weekly: ~7 days apart
   - Monthly: ~28-31 days apart
   - Quarterly: ~90 days apart
   - Yearly: ~365 days apart
4. **Autopay keywords** present in SMS

### Confidence Scoring:
- **High Confidence:** 3+ occurrences with consistent interval and amount
- **Medium Confidence:** 2 occurrences with matching interval
- **Low Confidence:** Single occurrence with subscription/autopay keywords

### Handling Variable Amounts:
- Utility bills may vary month to month
- Use average of last 3 occurrences for prediction
- Flag as "estimated" in UI

### When Pattern is Uncertain:
1. Check for autopay keywords
2. Check merchant against known subscription services
3. If still uncertain, flag for user confirmation
</decision_framework>

---

<state_tracking>
## State Tracking Files

### tests.json
```json
{
  "last_updated": "<timestamp>",
  "current_phase": "<phase>",
  "summary": {
    "total": 0,
    "passing": 0,
    "failing": 0
  },
  "recurring_detection_tests": [
    {"name": "<test_name>", "status": "passing|failing|not_started"}
  ],
  "discovered_recurring": {
    "subscriptions": [],
    "utilities": [],
    "financial": [],
    "other": []
  }
}
```

### progress.txt
```text
# Recurring Bills Detection - Progress

## Current Session
- Date: <date>
- Phase: <current phase>

## Discovered Recurring Transactions
### Subscriptions
- [merchant]: Rs.X every [interval], next due [date]

### Utility Bills
- [merchant]: ~Rs.X every month, next due [date]

### EMI/Financial
- [merchant]: Rs.X every month, next due [date]

## Test Status
- Total: X
- Passing: X
- Failing: X

## Current Task
<what you're working on>

## Next Steps
1. <next step>
```
</state_tracking>

---

# PHASE INSTRUCTIONS

<phase_0_discovery>
## PHASE 0: DISCOVERY & ANALYSIS

**Objective:** Understand current state and analyze historical SMS for recurring patterns.

### Step 0.1: Analyze Current Implementation
1. Find existing recurring detection code (if any)
2. Find where "upcoming bills" UI gets its data
3. Understand the data model for recurring transactions
4. Check if there's a RecurringTransaction entity/table
5. Identify gaps in current implementation

### Step 0.2: Pull ALL Historical SMS via ADB
1. Verify ADB connection
2. Pull ALL transaction SMS (not just recent)
3. Go back as far as SMS history allows
4. Goal: Capture multiple occurrences of recurring transactions

### Step 0.3: Identify Recurring Patterns from SMS
For each unique merchant/source, analyze:
1. **Frequency:** How often do transactions occur?
2. **Amount consistency:** Same amount or variable?
3. **Date pattern:** Similar day of month?
4. **Keywords:** Contains autopay/subscription indicators?

Create a list:
```
| Merchant | Occurrences | Amounts | Dates | Interval | Autopay? | Confidence |
|----------|-------------|---------|-------|----------|----------|------------|
| Netflix  | 5           | 649,649,649,649,649 | 5th each month | Monthly | Yes | High |
| TANGEDCO | 6           | 1200,1350,1180,1420,1290,1380 | ~15th | Monthly | No | High |
| ...      | ...         | ...     | ...   | ...      | ...      | ...        |
```

### Step 0.4: Categorize Discovered Recurring Transactions
Group into:
1. **Confirmed Recurring:** 3+ occurrences, clear pattern
2. **Likely Recurring:** 2 occurrences or autopay keywords
3. **Potential Recurring:** Single occurrence but subscription-like

### Step 0.5: Check Upcoming Bills UI
1. Navigate to upcoming bills section in app (or find the UI code)
2. Verify what data it currently shows
3. Identify why recurring transactions aren't appearing
4. Document the data flow: Detection → Storage → UI

### Step 0.6: Create Analysis Document
Create `RECURRING_ANALYSIS.md` with:
- Current implementation status
- All recurring transactions discovered from SMS
- Predicted bills for next month
- Gap analysis (what's missing in implementation)
- Recommended approach

**Deliverables:**
- [ ] Current recurring detection code analyzed
- [ ] Upcoming bills UI data flow understood
- [ ] ALL historical SMS pulled via ADB
- [ ] Recurring patterns identified and documented
- [ ] Next month predictions calculated
- [ ] `RECURRING_ANALYSIS.md` created
- [ ] `tests.json` initialized
- [ ] `progress.txt` started

**Proceed to Phase 1 after completing all deliverables.**
</phase_0_discovery>

---

<phase_1_planning>
## PHASE 1: PLANNING

**Objective:** Plan the implementation to make recurring bills appear in upcoming bills.

### Step 1.1: Define Data Model Requirements
Based on discovery, define what needs to be stored:
```
RecurringTransaction:
  - id
  - merchantName
  - merchantNameVariants (for fuzzy matching)
  - category (subscription/utility/financial/other)
  - amount (for fixed) or averageAmount (for variable)
  - isAmountFixed (boolean)
  - frequency (weekly/monthly/quarterly/yearly)
  - dayOfMonth / dayOfWeek (when it typically occurs)
  - isAutopay (boolean)
  - paymentMethod (UPI/card/etc)
  - lastOccurrence (date)
  - nextExpectedDate (calculated)
  - confidenceScore (high/medium/low)
  - isUserConfirmed (boolean)
  - transactionHistory (list of past occurrences)
```

### Step 1.2: Plan Detection Logic
1. **Pattern Detection Algorithm:**
   - Group transactions by merchant (fuzzy match)
   - Calculate interval between occurrences
   - Determine if interval is consistent
   - Calculate confidence score

2. **Autopay Detection:**
   - Scan SMS text for autopay keywords
   - Flag transactions accordingly

3. **Next Date Prediction:**
   - Based on historical pattern
   - Account for month length variations

### Step 1.3: Plan Test Cases
For each component:

**Recurring Detection Tests:**
- Detects monthly subscription (same amount, same day)
- Detects monthly utility (variable amount, similar day)
- Detects weekly recurring
- Detects quarterly recurring
- Detects yearly recurring (insurance, prime membership)
- Handles merchant name variations
- Calculates correct next occurrence date
- Identifies autopay from keywords
- Ignores one-time transactions
- Handles gaps in pattern (skipped month)

**Upcoming Bills Tests:**
- Returns all recurring transactions due in next 30 days
- Calculates total predicted expense
- Sorts by due date
- Shows correct amount (fixed or estimated)
- Flags autopay vs manual bills

### Step 1.4: Create Task Plan
Create `TASK_PLAN.md` with:
- Data model changes needed
- Detection algorithm approach
- Test cases to write
- UI integration points
- Execution order

**Deliverables:**
- [ ] Data model defined
- [ ] Detection algorithm planned
- [ ] Test cases listed
- [ ] UI integration points identified
- [ ] `TASK_PLAN.md` created
- [ ] `progress.txt` updated

**Proceed to Phase 2 after completing all deliverables.**
</phase_1_planning>

---

<phase_2_tests_red>
## PHASE 2: TESTS FIRST (RED)

**Objective:** Write failing tests that define expected behavior.

### Step 2.1: Write Recurring Detection Tests
Using discovered patterns from Phase 0 as test data:
- Test monthly subscription detection
- Test monthly utility detection (variable amounts)
- Test weekly recurring detection
- Test autopay keyword detection
- Test next occurrence date calculation
- Test confidence scoring
- Test merchant fuzzy matching

### Step 2.2: Write Upcoming Bills Tests
- Test retrieval of upcoming bills for next 30 days
- Test total prediction calculation
- Test sorting by due date
- Test filtering by category
- Test autopay flag display

### Step 2.3: Write Integration Tests
- Test full flow: SMS → Detection → Storage → UI retrieval
- Test that detected recurring shows in upcoming bills

### Step 2.4: Verify RED State
Run all tests - they must FAIL (no implementation yet)

**Deliverables:**
- [ ] Recurring detection tests written
- [ ] Upcoming bills tests written
- [ ] Integration tests written
- [ ] All tests confirmed FAILING
- [ ] `tests.json` updated
- [ ] `progress.txt` updated

**Proceed to Phase 3 after confirming RED state.**
</phase_2_tests_red>

---

<phase_3_implementation_green>
## PHASE 3: IMPLEMENTATION (GREEN)

**Objective:** Make all tests pass.

### Iteration Pattern
```
WHILE (any test failing):
    1. Pick ONE failing test
    2. Implement minimal code to pass it
    3. Run test
    4. IF passes: Commit, move to next
    5. IF fails: Debug, retry
    6. Run full suite to check regressions
```

### Implementation Order:
1. **Data Model:** Create/update RecurringTransaction entity
2. **Detection Logic:** Implement pattern detection algorithm
3. **Autopay Detection:** Implement keyword scanning
4. **Next Date Calculation:** Implement prediction logic
5. **Storage:** Save detected recurring transactions
6. **Retrieval:** Implement upcoming bills query
7. **UI Integration:** Ensure data reaches upcoming bills screen

### Key Implementation Points:
- Use existing transaction history to detect patterns
- Run detection on app startup or after new SMS received
- Store recurring patterns for quick retrieval
- Calculate next occurrence dynamically or on detection

**Deliverables:**
- [ ] Data model implemented
- [ ] Detection logic implemented
- [ ] Autopay detection implemented
- [ ] Next date prediction implemented
- [ ] Storage implemented
- [ ] Retrieval implemented
- [ ] UI integration complete
- [ ] All tests pass
- [ ] `tests.json` shows all GREEN
- [ ] `progress.txt` updated

**Proceed to Phase 4 when ALL tests pass.**
</phase_3_implementation_green>

---

<phase_4_refactor>
## PHASE 4: REFACTOR

**Objective:** Clean up code while keeping tests green.

### Focus Areas:
- Detection algorithm efficiency
- Code duplication removal
- Clear naming and documentation
- Edge case handling

**Deliverables:**
- [ ] Code cleaned up
- [ ] All tests still pass
- [ ] `progress.txt` updated
</phase_4_refactor>

---

<phase_5_verification>
## PHASE 5: VERIFICATION

**Objective:** Verify recurring bills appear correctly in the app.

### Step 5.1: Run Full Test Suite
All tests must pass.

### Step 5.2: Manual Verification
1. Launch the app
2. Navigate to upcoming bills section
3. Verify ALL discovered recurring transactions appear
4. Verify amounts are correct (or estimated for variable)
5. Verify dates are correct
6. Verify autopay flag is correct
7. Verify total next month prediction is calculated

### Step 5.3: Real Data Verification
Using the recurring transactions discovered in Phase 0:
- [ ] Netflix subscription appears
- [ ] Electricity bill appears (estimated amount)
- [ ] Broadband bill appears
- [ ] Mobile recharge appears (if recurring)
- [ ] EMI payments appear
- [ ] All other discovered recurring appear
- [ ] Next month total is calculated

### Step 5.4: Edge Case Verification
- Variable amount bills show as "estimated"
- Autopay transactions are flagged
- Past due items handled correctly
- New recurring detected after new SMS

**Verification Checklist:**
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Upcoming bills UI shows data
- [ ] All discovered recurring transactions visible
- [ ] Amounts correct or marked estimated
- [ ] Dates calculated correctly
- [ ] Autopay flags correct
- [ ] Next month total calculated
- [ ] No regressions in SMS parsing

**Deliverables:**
- [ ] Test results captured
- [ ] Manual verification complete
- [ ] Screenshots of working upcoming bills
- [ ] `progress.txt` updated
</phase_5_verification>

---

<phase_6_documentation>
## PHASE 6: DOCUMENTATION

**Objective:** Document the implementation and generate user report.

### Create/Update:

1. **CHANGELOG.md** - What was implemented

2. **RECURRING_TRANSACTIONS_REPORT.md** - User's recurring expenses:
```markdown
# Your Recurring Transactions

## Monthly Subscriptions
| Service | Amount | Day | Autopay |
|---------|--------|-----|---------|
| Netflix | ₹649 | 5th | Yes |
| Spotify | ₹119 | 12th | Yes |
| YouTube Premium | ₹129 | 18th | Yes |

## Monthly Bills
| Bill | Avg Amount | Typical Day | Autopay |
|------|------------|-------------|---------|
| Electricity | ~₹1,300 | 15th | No |
| Broadband | ₹999 | 1st | Yes |
| Mobile | ₹599 | 8th | Yes |

## EMI/Financial
| Description | Amount | Day |
|-------------|--------|-----|
| Home Loan EMI | ₹25,000 | 5th |
| Credit Card | Variable | 15th |

## Next Month Prediction
**Total Expected: ₹XX,XXX**

### Breakdown:
- Subscriptions: ₹X,XXX
- Utilities: ₹X,XXX (estimated)
- Financial: ₹XX,XXX
- Other: ₹X,XXX
```

3. **Technical Documentation** - How recurring detection works

**Deliverables:**
- [ ] CHANGELOG.md updated
- [ ] RECURRING_TRANSACTIONS_REPORT.md created
- [ ] Technical documentation added
- [ ] `progress.txt` marked COMPLETE
</phase_6_documentation>

---

<success_criteria>
## Success Criteria

The task is complete when:

- [ ] All recurring detection tests pass
- [ ] All upcoming bills tests pass
- [ ] Recurring transactions detected from historical SMS
- [ ] Subscriptions identified (Netflix, Spotify, etc.)
- [ ] Utility bills identified (electricity, broadband, etc.)
- [ ] EMI/financial recurring identified
- [ ] Autopay transactions flagged correctly
- [ ] Upcoming bills UI shows all recurring transactions
- [ ] Next occurrence dates calculated correctly
- [ ] Next month total expense predicted
- [ ] Variable amounts marked as estimated
- [ ] No regressions in existing functionality
- [ ] User report generated with all recurring expenses
</success_criteria>

---

## Start Here

**Begin with Phase 0: Discovery.**

1. Find existing recurring detection code (if any)
2. Find where upcoming bills UI gets its data
3. Pull ALL historical SMS via ADB
4. Analyze patterns to identify recurring transactions
5. Document what's missing in current implementation

Do not write implementation code until Phase 0 and Phase 1 are complete.