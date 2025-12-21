# Analytics Period Jump Testing Guide

This guide provides comprehensive instructions for testing the Analytics screen period jump functionality and Compare Months feature using SMS test scripts.

## Table of Contents

1. [Overview](#overview)
2. [Bug Fixes Implemented](#bug-fixes-implemented)
3. [Pre-Test Setup](#pre-test-setup)
4. [Available Test Scripts](#available-test-scripts)
5. [Test Execution Procedures](#test-execution-procedures)
6. [Manual Testing Checklist](#manual-testing-checklist)
7. [Expected Results](#expected-results)
8. [Troubleshooting](#troubleshooting)

---

## Overview

The Analytics screen provides several period navigation features that allow users to jump between different time periods:

- **Last Month** - View previous month's data
- **3 Months Ago** - View data from 3 months back
- **Last Year** - View same month from previous year
- **Same Month Last Year** - Another way to view year-over-year comparison
- **Compare Months** - Side-by-side comparison of two periods

This guide helps verify that all these features work correctly after the bug fixes.

---

## Bug Fixes Implemented

### Bug #1: Period Jump Buttons Race Condition

**Issue**: When clicking period jump buttons (Last Month, 3 Months Ago, etc.), the period label would change but the displayed transaction data remained the same.

**Root Cause**: Race condition in `AnalyticsViewModel.updateSelectedDate()` - navigation buttons were updated synchronously before `loadData()` completed asynchronously.

**Fix**: Removed premature `updateNavigationButtons()` call from `updateSelectedDate()` and moved it to execute AFTER data loads in the `loadData()` collect block.

**Location**: `app/src/main/java/com/fino/app/presentation/viewmodel/AnalyticsViewModel.kt`
- Lines 476-485: Removed early call
- Lines 182-198: Added call after data processing

### Bug #2: Compare Months Crash

**Issue**: Clicking the "Compare Months" button would crash the app.

**Root Cause**: Repository injection issue in `ComparisonViewModel` init block - immediate call to `loadComparisonData()` without error handling.

**Fix**: Wrapped init block in `viewModelScope.launch` with try-catch for graceful error handling.

**Location**: `app/src/main/java/com/fino/app/presentation/viewmodel/ComparisonViewModel.kt`
- Lines 43-56: Added null-safety and error handling

---

## Pre-Test Setup

### 1. Check ADB Connection

Ensure Android emulator is running and ADB is accessible:

```powershell
adb devices
```

Expected output:
```
List of devices attached
emulator-5554   device
```

### 2. Clear Existing Data (Optional)

For a clean test environment, clear app data:

```powershell
adb shell pm clear com.fino.app
```

**Note**: This will delete all existing transactions, categories, and settings.

### 3. Launch App and Grant Permissions

1. Start the Fino app on the emulator
2. Grant SMS permissions when prompted
3. Complete initial setup if needed
4. Note the current date (should be December 2025 for current tests)

### 4. Verify SMS Receiver is Active

Check logcat to ensure SMS receiver is working:

```powershell
adb logcat -s "SmsReceiver:*" -c  # Clear logs
adb logcat -s "SmsReceiver:*"     # Monitor SMS processing
```

Keep this terminal open to watch SMS processing in real-time.

---

## Available Test Scripts

### Individual Month Scripts

| Script | Period | Transactions | Total Amount | Purpose |
|--------|--------|--------------|--------------|---------|
| `send-december-2025-sms.ps1` | Dec 1-20, 2025 | 28 | ~₹18,000 | Current month |
| `send-november-2025-sms.ps1` | Nov 1-30, 2025 | 23 | ~₹14,500 | Last month |
| `send-september-2025-sms.ps1` | Sep 1-30, 2025 | 22 | ~₹13,800 | 3 months ago |
| `send-december-2024-sms.ps1` | Dec 1-31, 2024 | 27 | ~₹16,200 | Same month last year |

### Annual Scripts

| Script | Period | Transactions | Total Amount | Purpose |
|--------|--------|--------------|--------------|---------|
| `send-all-2024-sms.ps1` | Jan-Dec 2024 | 240 | ~₹1,80,000 | Full year baseline |
| `send-all-2025-sms.ps1` | Jan-Nov 2025 | 220 | ~₹1,65,000 | Current year YTD |

### Master Orchestration Script

**`master-sms-test-suite.ps1`** - Interactive menu with options:
- **[1]** Send minimal test set (4 key periods)
- **[2]** Send all 2024 data
- **[3]** Send all 2025 data
- **[4]** Send everything (chronological order)
- **[5]** Send specific month only
- **[0]** Exit

---

## Test Execution Procedures

### Quick Test (Minimal Dataset)

Best for rapid verification of period jump functionality.

```powershell
cd C:\projects\Fino
.\master-sms-test-suite.ps1
# Select option: [1] Send minimal test set
```

**What it does**:
- Sends 100 transactions across 4 key periods
- Takes ~2 minutes to complete
- Validates all period jump buttons

**After completion**:
- Wait 3-5 seconds for SMS processing
- Open Fino app → Analytics tab
- Follow the Manual Testing Checklist below

### Full Test (Complete Dataset)

For comprehensive testing including trend analysis and YoY comparisons.

```powershell
cd C:\projects\Fino
.\master-sms-test-suite.ps1
# Select option: [4] Send everything
```

**What it does**:
- Sends 560 transactions across 24 months (2024-2025)
- Takes ~8 minutes to complete
- Validates all features including trends, comparisons, budgets

### Individual Month Test

To test a specific period or re-send failed transactions.

```powershell
cd C:\projects\Fino
.\send-december-2025-sms.ps1
```

Replace with any specific month script as needed.

---

## Manual Testing Checklist

After sending the minimal test set, perform these manual verification steps:

### ✅ Step 1: Verify Current Month Data

Navigate to **Analytics Tab**

- [ ] Current period shows "December 2025"
- [ ] Transaction list displays ~28 transactions
- [ ] Total spent shows ~₹18,000
- [ ] Category breakdown displays correctly
- [ ] Payment method breakdown shows mix of UPI/Credit Card
- [ ] Charts render without errors

### ✅ Step 2: Test "Last Month" Button

Click the **"Last Month"** period jump button

- [ ] Period label changes to "November 2025"
- [ ] Transaction list updates to show ~23 transactions
- [ ] Total spent changes to ~₹14,500
- [ ] Category breakdown recalculates
- [ ] Charts refresh with November data
- [ ] Navigation arrows update state

### ✅ Step 3: Test "3 Months Ago" Button

Click the **"3 Months Ago"** period jump button

- [ ] Period label changes to "September 2025"
- [ ] Transaction list updates to show ~22 transactions
- [ ] Total spent changes to ~₹13,800
- [ ] Category breakdown recalculates
- [ ] Charts refresh with September data

### ✅ Step 4: Test "Last Year" Button

Click the **"Last Year"** period jump button

- [ ] Period label changes to "December 2024"
- [ ] Transaction list updates to show ~27 transactions
- [ ] Total spent changes to ~₹16,200
- [ ] Category breakdown recalculates
- [ ] Year-over-year indicator appears (if implemented)

### ✅ Step 5: Test "Same Month Last Year" Button

Click the **"Same Month Last Year"** period jump button

- [ ] Period label changes to "December 2024"
- [ ] Data matches "Last Year" button results
- [ ] Transaction count ~27
- [ ] Total spent ~₹16,200

### ✅ Step 6: Test Period Label Click (Return to Current)

Click on the **Period Label** at the top

- [ ] Period label resets to "December 2025"
- [ ] Transaction list returns to current month data
- [ ] Total spent returns to ~₹18,000
- [ ] All data matches initial current month state

### ✅ Step 7: Test Navigation Arrows

Test **Left Arrow** (Previous Period):
- [ ] Clicking left arrow navigates to November 2025
- [ ] Data updates correctly
- [ ] Can navigate backward through months

Test **Right Arrow** (Next Period):
- [ ] Right arrow is disabled when at current month
- [ ] Right arrow becomes enabled when viewing past months
- [ ] Clicking right arrow moves forward one month
- [ ] Cannot navigate beyond current month

### ✅ Step 8: Test "Compare Months" Feature

Click the **"Compare Months"** button

- [ ] **NO CRASH** - App navigates to Comparison screen
- [ ] Comparison screen loads successfully
- [ ] Shows two period selectors (Current Period / Compare With)
- [ ] Can navigate both periods independently
- [ ] Left/Right arrows work for both selectors
- [ ] Future months are properly disabled

**On Comparison Screen**:
- [ ] Side-by-side period comparison displays
- [ ] Total spending shows for both periods
- [ ] Percentage change displays correctly
- [ ] Category-wise breakdown visible
- [ ] Top categories comparison shows
- [ ] Can navigate back to Analytics tab

### ✅ Step 9: Test Period Picker Dialog (if using heatmap)

If the app uses the Period Picker Dialog with spending heatmap:

Click period label to open dialog:
- [ ] Dialog opens without crash
- [ ] Year selector works (left/right chevrons)
- [ ] Spending heatmap displays months
- [ ] Months with data show color intensity
- [ ] Future months are grayed out and disabled
- [ ] Clicking a month updates selection
- [ ] Cancel button dismisses dialog
- [ ] Select button applies choice and updates main screen

---

## Expected Results

### Transaction Counts by Period

| Period | Label | Expected Transactions | Expected Total |
|--------|-------|-----------------------|----------------|
| Current | December 2025 | ~28 | ~₹18,000 |
| Last Month | November 2025 | ~23 | ~₹14,500 |
| 3 Months Ago | September 2025 | ~22 | ~₹13,800 |
| Last Year | December 2024 | ~27 | ~₹16,200 |

### Year-over-Year Comparison (Dec 2024 vs Dec 2025)

- **December 2024**: ₹16,200 (27 transactions)
- **December 2025**: ₹18,000 (28 transactions)
- **Change**: +₹1,800 (+11.1%)
- **Interpretation**: Reflects ~10% inflation year-over-year

### Payment Method Distribution

Expected mix across all periods:
- **UPI**: ~60% of transactions
- **Credit Card**: ~40% of transactions

### Category Distribution

Top categories across test data:
1. Shopping (20-25%)
2. Food & Dining (20-25%)
3. Bills & Utilities (15-20%)
4. Transport (10-15%)
5. Entertainment (10-15%)
6. Subscriptions (5-10%)

---

## Troubleshooting

### Issue: Period changes but shows "No transactions"

**Possible Causes**:
- SMS messages weren't parsed correctly
- Date format mismatch between SMS and parser
- Transactions filtered out by period logic

**Diagnosis**:
```powershell
adb logcat -s "SmsReceiver:*" -s "SmsParser:*" | Select-String "error|failed"
```

**Solutions**:
1. Check logcat for parsing errors
2. Verify date format matches parser expectations (DD-MM-YY)
3. Re-run specific month script
4. Check if transactions are categorized as DEBIT (only DEBIT shows in Analytics)

### Issue: Compare Months still crashes

**Possible Causes**:
- Repository injection failed in Hilt
- Flow not properly initialized
- Null pointer in data processing

**Diagnosis**:
```powershell
adb logcat -s "ComparisonViewModel:*" -s "AndroidRuntime:*"
```

**Solutions**:
1. Verify RepositoryModule.kt has correct @Provides methods
2. Check that @HiltViewModel annotation is present
3. Rebuild app: `.\gradlew.bat clean assembleDebug`
4. Reinstall app: `.\gradlew.bat installDebug`

### Issue: Amounts don't match expected totals

**Possible Causes**:
- Some transactions failed to save
- Duplicate UPI references rejected
- SMS parsing failed for certain formats

**Diagnosis**:
```powershell
adb logcat -s "SmsReceiver:*" | Select-String "saved|duplicate|failed"
```

**Solutions**:
1. Count actual transactions in logcat vs expected
2. Re-run specific month script (UPI refs are randomized)
3. Check for duplicate detection messages
4. Verify all bank formats are supported in SmsParser

### Issue: SMS flooding / Messages rejected

**Symptoms**:
- Script runs but transactions don't appear
- Emulator becomes unresponsive
- High message count causes issues

**Solutions**:
1. Scripts include 800ms delay between messages - do not reduce
2. If flooding occurs, restart emulator
3. Clear app data: `adb shell pm clear com.fino.app`
4. Re-run scripts in smaller batches

### Issue: Navigation buttons don't update

**Possible Causes**:
- Fix not properly applied
- State not recomposing
- Flow not emitting updates

**Diagnosis**:
- Check that fix is in AnalyticsViewModel.kt lines 197-198
- Verify updateNavigationButtons() is called AFTER data loads
- Check UI is observing uiState.collectAsState()

**Solutions**:
1. Verify code changes were saved
2. Rebuild app completely
3. Check StateFlow is properly exposed to UI

### Issue: Test scripts fail with "adb not found"

**Solutions**:
```powershell
# Add Android SDK platform-tools to PATH, or use full path:
$adbPath = "C:\Users\YourUser\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```

Edit script and replace `$adbPath = "adb"` with full path.

---

## Advanced Testing

### Automated Verification Script

Create `verify-analytics.ps1` for automated checks:

```powershell
# Check transaction counts via logcat
$savedCount = (adb logcat -d -s "SmsReceiver:*" | Select-String "saved").Count

Write-Host "Transactions saved: $savedCount"
Write-Host "Expected: 100 (minimal set) or 560 (full set)"

if ($savedCount -ge 90) {
    Write-Host "✓ Test PASSED" -ForegroundColor Green
} else {
    Write-Host "✗ Test FAILED - Too few transactions" -ForegroundColor Red
}
```

### Performance Testing

Monitor app performance during period switches:

```powershell
# Monitor UI thread
adb shell "dumpsys gfxinfo com.fino.app reset"

# Perform period jumps in app

adb shell "dumpsys gfxinfo com.fino.app"
# Check for frame drops, jank
```

### Memory Leak Detection

Check for memory issues with large datasets:

```powershell
# Before test
adb shell "dumpsys meminfo com.fino.app"

# Send all 2024 + 2025 data (560 transactions)

# After test
adb shell "dumpsys meminfo com.fino.app"
# Compare heap usage
```

---

## Test Data Characteristics

### December 2025 (Current Month)
- **28 transactions** from Dec 1-20
- Mix of subscriptions, shopping, food, transport
- Holiday shopping emphasis
- Total: ~₹18,000

### November 2025 (Last Month)
- **23 transactions** from Nov 1-30
- Regular monthly expenses
- Lower than December (no holiday boost)
- Total: ~₹14,500

### September 2025 (3 Months Ago)
- **22 transactions** from Sep 1-30
- Back-to-school emphasis
- Education category transactions
- Lower entertainment
- Total: ~₹13,800

### December 2024 (Last Year)
- **27 transactions** from Dec 1-31
- Similar pattern to Dec 2025
- 10% lower amounts (inflation comparison)
- Total: ~₹16,200

---

## Success Criteria Summary

All tests are considered successful when:

✅ **Bug Fixes**:
- [ ] Period jump buttons update displayed data correctly
- [ ] Compare Months opens without crashing
- [ ] Navigation buttons reflect actual data state
- [ ] No race conditions or UI inconsistencies

✅ **Functionality**:
- [ ] All 5 period jump buttons work correctly
- [ ] Manual navigation (arrows) works
- [ ] Period label click returns to current
- [ ] Compare Months shows side-by-side data
- [ ] All expected transactions appear

✅ **Data Accuracy**:
- [ ] Transaction counts match expected values
- [ ] Total amounts are within 5% of expected
- [ ] Category breakdowns make sense
- [ ] Payment methods distribute correctly

✅ **User Experience**:
- [ ] No crashes or freezes
- [ ] Smooth transitions between periods
- [ ] Loading states display correctly
- [ ] Error messages are helpful (if any)

---

## Reporting Issues

If you find bugs during testing, report with:

1. **Steps to reproduce**
2. **Expected behavior**
3. **Actual behavior**
4. **Logcat output** (use `adb logcat -d > issue-log.txt`)
5. **Test data used** (which script)
6. **Device/Emulator info**

---

## Appendix: SMS Message Formats

### UPI Transaction Formats

**HDFC Bank**:
```
Paid Rs.450.00 to Netflix on 01-12-25 using UPI. UPI Ref: 123456789012. -HDFC Bank
```

**SBI**:
```
Rs.450.00 debited from A/c XX9012 to VPA Netflix@paytm on 01-12-25. UPI Ref 123456789012 -SBI
```

**ICICI Bank**:
```
INR 450.00 debited from A/c XX5678 on 01-12-25 for UPI to Netflix@paytm. Ref 123456789012
```

**Axis Bank**:
```
INR 450.00 debited from A/c no. XX3456 on 01-12-25 for UPI-Netflix. UPI Ref: 123456789012
```

### Credit Card Transaction Formats

**HDFC Bank**:
```
HDFC Bank Credit Card XX1234 has been used for Rs.3500.00 at Amazon on 04-12-25 at 14:30:45
```

**ICICI Bank**:
```
Alert: ICICI Card ending 5678 used for INR 3500.00 at Amazon on 04-12-25
```

**SBI**:
```
Your SBI Card ending 1234 was used for Rs.3500.00 at Amazon on 04-12-25
```

**Axis Bank**:
```
Axis Bank Card XX9012: Rs.3500.00 spent at Amazon on 04-12-25
```

---

## Quick Reference Commands

```powershell
# Check emulator connection
adb devices

# Clear app data
adb shell pm clear com.fino.app

# Monitor SMS processing
adb logcat -s "SmsReceiver:*"

# Check for errors
adb logcat -s "AndroidRuntime:*" "SmsParser:*"

# Count saved transactions
(adb logcat -d -s "SmsReceiver:*" | Select-String "saved").Count

# Rebuild and install
.\gradlew.bat clean assembleDebug installDebug
```

---

**Last Updated**: December 20, 2025
**Version**: 1.0
**Related**: See plan file at `C:\Users\Vivek Angadi\.claude\plans\glittery-foraging-petal.md`
