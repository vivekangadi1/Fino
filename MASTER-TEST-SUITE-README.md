# Master SMS Test Suite

**Version:** 1.0.0
**Created:** 2025-12-20
**Script:** `master-sms-test-suite.ps1`

## Overview

The Master SMS Test Suite is a comprehensive PowerShell orchestration script designed to simplify testing of the Fino app's SMS parsing functionality. It provides a menu-driven interface for sending realistic transaction data to the Android emulator with detailed progress tracking and reporting.

## Quick Start

```powershell
.\master-sms-test-suite.ps1
```

## Prerequisites

1. Android emulator running
2. ADB (Android Debug Bridge) accessible in PATH or at default location
3. PowerShell 5.1 or higher
4. Fino app installed on emulator

## Features

### Interactive Menu System
- Clear, numbered menu options
- Easy navigation between scenarios
- Visual feedback with color-coded output
- Graceful error handling

### Test Scenarios

#### 1. Minimal Test Set
**Best for:** Quick comprehensive testing
**Data:** 4 key periods (Dec 2025, Nov 2025, Sep 2025, Dec 2024)
**Volume:** ~100 transactions, Rs.62,500
**Use case:** Verify all period navigation, YoY comparison, and trending

#### 2. All 2024 Data
**Best for:** Historical baseline
**Data:** December 2024 only
**Volume:** 27 transactions, Rs.16,200
**Use case:** YoY comparison testing, historical data analysis

#### 3. All 2025 Data
**Best for:** Current year trends
**Data:** Sep 2025, Nov 2025, Dec 2025
**Volume:** 73 transactions, Rs.46,300
**Use case:** Current year spending patterns, monthly comparisons

#### 4. Everything (Chronological)
**Best for:** Complete database population
**Data:** All available periods in chronological order
**Volume:** ~100 transactions, Rs.62,500
**Use case:** Full system testing, realistic data flow

#### 5. Specific Month
**Best for:** Targeted testing
**Data:** Choose one period
**Volume:** 22-28 transactions per month
**Use case:** Debugging specific period issues, focused testing

### Progress Tracking

- Real-time transaction counter (e.g., [15/28])
- Percentage-based progress bars
- Individual transaction status (success/failure)
- Batch summaries after each period

### Comprehensive Reporting

After execution, you receive a detailed summary:

#### Overall Statistics
- Total transactions sent vs attempted
- Total monetary amount
- Success rate percentage

#### Period Breakdown
- Transactions per period
- Amount per period
- Success rate per period

#### Payment Method Analysis
- UPI payments grouped by bank
- Credit card payments by card number
- Transaction count and percentage per method
- Amount and percentage breakdown

#### Category Distribution
- Automatic categorization of merchants
- Top spending categories
- Transaction count per category
- Percentage of total spending

### Smart Features

1. **Transaction Parsing**
   - Automatically extracts transaction data from source scripts
   - Generates unique UPI reference numbers
   - Builds bank-specific SMS formats

2. **Error Handling**
   - Validates ADB connection before starting
   - Checks for missing test data files
   - Continues on individual SMS failures
   - Provides clear error messages

3. **SMS Throttling**
   - 800ms delay between messages
   - Prevents emulator SMS flooding
   - Ensures reliable delivery

4. **Batch Processing**
   - Progress updates every 10 transactions
   - 3-second wait after each batch
   - Allows app time to process SMS

## Test Data Coverage

### Banks Supported
- HDFC Bank (UPI + Credit Card)
- ICICI Bank (UPI + Credit Card)
- SBI Bank (UPI + Credit Card)
- Axis Bank (UPI + Credit Card)

### Transaction Types
- UPI payments (60% of volume)
- Credit card transactions (40% of volume)

### Merchant Categories
- Food Delivery (Swiggy, Zomato, Dominos, McDonald's, Starbucks)
- Shopping (Amazon, Flipkart, Myntra, Nykaa, Ajio)
- Groceries (BigBasket, DMart)
- Transport (Uber, Ola, Rapido)
- Bills (Electricity, Airtel, Jio)
- Entertainment (Netflix, Spotify, BookMyShow, PVR, Steam)
- Education (Tuition fees, Coursera, bookstores)

### Date Ranges
- **December 2025:** Dec 1-20, 2025 (current month, 28 txns)
- **November 2025:** Nov 1-30, 2025 (previous month, 23 txns)
- **September 2025:** Sep 1-30, 2025 (back-to-school, 22 txns)
- **December 2024:** Dec 1-31, 2024 (YoY baseline, 27 txns)

### Amount Distribution
- Small transactions: Rs.150 - Rs.600 (transport, food)
- Medium transactions: Rs.600 - Rs.2,000 (groceries, bills)
- Large transactions: Rs.2,000+ (shopping, education)

## Usage Examples

### Example 1: Full System Test
```powershell
# Launch script
.\master-sms-test-suite.ps1

# Select option 4 (Send Everything)
# Wait 2-3 minutes for completion
# Review comprehensive report
# Verify in Fino app
```

### Example 2: Quick Period Navigation Test
```powershell
# Launch script
.\master-sms-test-suite.ps1

# Select option 1 (Minimal Test Set)
# Wait 1-2 minutes
# Open Fino app
# Navigate through different periods in Analytics
# Verify data appears correctly for each period
```

### Example 3: Specific Month Testing
```powershell
# Launch script
.\master-sms-test-suite.ps1

# Select option 5 (Specific Month)
# Choose desired month
# Wait for completion
# Test specific features for that period
```

## Output Example

```
======================================================================
                    Fino SMS Test Suite v1.0.0
======================================================================

----------------------------------------------------------------------
Checking Prerequisites
----------------------------------------------------------------------
✓ Emulator connected

Verifying test data scripts...
  ✓ send-december-2025-sms.ps1
  ✓ send-november-2025-sms.ps1
  ✓ send-september-2025-sms.ps1
  ✓ send-december-2024-sms.ps1

----------------------------------------------------------------------
Sending December 2024
----------------------------------------------------------------------
Total transactions: 27

[1/27] Netflix - ₹405.00 (01-12-24)
  ✓ Sent
[2/27] Spotify - ₹540.00 (01-12-24)
  ✓ Sent
...
[27/27] Jio Mobile - ₹359.00 (19-12-24)
  ✓ Sent

Batch Complete: 27/27 sent, ₹16,200.00
Waiting 3 seconds for SMS processing...

======================================================================
                  Comprehensive Test Summary
======================================================================

Overall Statistics
==================================================================
  Total Transactions Sent: 100 / 100
  Total Amount: ₹62,500.00
  Success Rate: 100.0%

Period Breakdown
==================================================================
  December 2024
    Transactions: 27/27
    Amount: ₹16,200.00
  September 2025
    Transactions: 22/22
    Amount: ₹13,800.00
  November 2025
    Transactions: 23/23
    Amount: ₹14,500.00
  December 2025
    Transactions: 28/28
    Amount: ₹18,000.00

Payment Method Breakdown
==================================================================
  UPI Payments:
    HDFCBK UPI: ₹18,450.00 (35 txns, 29.5%)
    ICICIB UPI: ₹12,300.00 (28 txns, 19.7%)
    SBIINB UPI: ₹10,150.00 (24 txns, 16.2%)
    AXISBK UPI: ₹9,100.00 (21 txns, 14.6%)

  Credit Card Payments:
    HDFCBK CC *1234: ₹8,200.00 (12 txns, 13.1%)
    ICICIB CC *5678: ₹4,300.00 (8 txns, 6.9%)
    AXISBK CC *9012: ₹4,300.00 (8 txns, 6.9%)

Category Distribution
==================================================================
  Food Delivery: ₹15,800.00 (28 txns, 25.3%)
  Shopping: ₹12,500.00 (18 txns, 20.0%)
  Groceries: ₹10,200.00 (15 txns, 16.3%)
  Bills: ₹8,900.00 (14 txns, 14.2%)
  Transport: ₹7,600.00 (16 txns, 12.2%)
  Entertainment: ₹5,500.00 (9 txns, 8.8%)

Verification Steps
==================================================================
  1. Wait 5-10 seconds for final SMS processing
  2. Open Fino app on the emulator
  3. Grant SMS permissions if prompted
  4. Navigate to Home screen to see transactions
  5. Check Analytics screen for period breakdowns
  6. Verify Payment Methods section

======================================================================
Test suite execution completed successfully!
======================================================================
```

## Troubleshooting

### Script Fails to Start

**Problem:** "ADB not found or not accessible"
**Solution:**
- Ensure Android SDK Platform Tools are installed
- Add ADB to system PATH
- Or update `$Script:Config.AdbPath` in the script

### No Emulator Detected

**Problem:** "No emulator detected"
**Solution:**
- Start Android emulator before running script
- Verify with `adb devices` command
- Ensure emulator fully boots before running

### Missing Test Data Scripts

**Problem:** "Script not found: send-xxx.ps1"
**Solution:**
- Ensure you're running from project root directory
- Verify test data scripts exist in same folder
- Script will skip missing periods and continue

### SMS Not Appearing in App

**Problem:** SMS sent successfully but not in app
**Solution:**
- Grant SMS permissions in app
- Check app is running on emulator
- View logcat: `adb logcat -s SmsReceiver:D`
- Verify SMS parsing patterns match in app code

### Low Success Rate

**Problem:** Many SMS failures
**Solution:**
- Reduce SMS delay (increase `$Script:Config.SmsDelay`)
- Check emulator performance/memory
- Restart emulator and try again
- Run smaller batches (option 5)

## Technical Details

### Architecture

```
master-sms-test-suite.ps1
├── Configuration
│   ├── ADB path
│   ├── SMS delay timing
│   └── Batch size for progress
├── Helper Functions
│   ├── Write-ColorLine (colored output)
│   ├── Test-AdbConnection (emulator check)
│   ├── Get-UpiRef (generate references)
│   ├── Send-Sms (ADB SMS sender)
│   ├── Build-SmsMessage (format bank SMS)
│   ├── Get-TransactionCategory (categorize)
│   └── Load-TransactionsFromScript (parse)
├── Orchestration Functions
│   ├── Send-TransactionBatch (batch processor)
│   ├── Show-ComprehensiveReport (reporting)
│   └── Show-Menu (UI)
└── Scenario Functions
    ├── Start-MinimalTestSet
    ├── Start-All2024Data
    ├── Start-All2025Data
    ├── Start-Everything
    └── Start-SpecificMonth
```

### PowerShell Best Practices

- `[CmdletBinding()]` for advanced function features
- `$ErrorActionPreference = "Stop"` for fail-fast behavior
- Try-catch blocks for error handling
- Parameter validation where applicable
- Consistent naming conventions
- Clear documentation and comments
- Progress indicators for long operations
- Color-coded output for readability

### Data Flow

1. User selects scenario from menu
2. Script validates prerequisites (ADB, emulator)
3. Loads transaction data from source scripts
4. Processes transactions in chronological order
5. Builds bank-specific SMS messages
6. Sends via ADB with throttling
7. Tracks statistics (amounts, methods, categories)
8. Displays comprehensive summary report
9. Provides verification instructions

## Extending the Script

### Adding New Test Data Periods

1. Create new PowerShell script (e.g., `send-january-2025-sms.ps1`)
2. Follow existing script format with `$transactions` array
3. Add entry to `$Script:TestDataScripts` hashtable:

```powershell
"Jan2025" = @{
    Path = "send-january-2025-sms.ps1"
    Transactions = 25
    ExpectedTotal = 15000
    Period = "January 2025"
    SortOrder = 5
}
```

### Customizing Categories

Edit `Get-TransactionCategory` function:

```powershell
$categoryMap = @{
    "Netflix|Spotify|Prime" = "Entertainment"
    "Swiggy|Zomato" = "Food Delivery"
    "BigBasket|DMart" = "Groceries"
    # Add your patterns here
    "NewMerchant1|NewMerchant2" = "New Category"
}
```

### Adjusting Delays

Modify configuration at script start:

```powershell
$Script:Config = @{
    AdbPath = "adb"
    SmsDelay = 1000  # Increase for slower devices
    BatchSize = 5    # Decrease for more frequent updates
}
```

## Related Files

- `send-december-2025-sms.ps1` - December 2025 test data
- `send-november-2025-sms.ps1` - November 2025 test data
- `send-september-2025-sms.ps1` - September 2025 test data
- `send-december-2024-sms.ps1` - December 2024 test data
- `SMS_TESTING_GUIDE.md` - Comprehensive SMS testing guide
- `verify-test-data.ps1` - Verification utilities

## Support

For issues or questions:
1. Check `SMS_TESTING_GUIDE.md` for troubleshooting
2. Review logcat output: `adb logcat -s SmsReceiver:D SmsParser:D`
3. Verify emulator and ADB setup
4. Check test data script formats match expected structure

## Version History

### v1.0.0 (2025-12-20)
- Initial release
- Interactive menu system
- 5 test scenarios
- Comprehensive reporting
- Progress tracking
- Error handling
- Support for 4 test periods (100 transactions)

---

**Author:** Claude Code
**License:** MIT
**Project:** Fino - Personal Finance Android App
