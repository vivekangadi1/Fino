# SMS Testing Guide for Fino App

## Method 1: Using Master SMS Test Suite (Recommended)

### Quick Start
The easiest way to send comprehensive test data is using the master orchestration script:

```powershell
.\master-sms-test-suite.ps1
```

This launches an interactive menu with the following options:

### Menu Options

**[1] Send Minimal Test Set**
- 4 key periods: Dec 2025, Nov 2025, Sep 2025, Dec 2024
- Total: ~100 transactions, Rs.62,500
- Best for: Quick comprehensive testing across all periods

**[2] Send All 2024 Data**
- Historical data: Dec 2024 only
- Total: ~27 transactions, Rs.16,200
- Best for: Year-over-year comparison testing

**[3] Send All 2025 Data**
- Current year: Dec 2025, Nov 2025, Sep 2025
- Total: ~73 transactions, Rs.46,300
- Best for: Current year trend analysis

**[4] Send Everything (Chronological)**
- All available test data in date order
- Total: ~100 transactions, Rs.62,500
- Best for: Complete database population

**[5] Send Specific Month**
- Choose individual month to send
- Best for: Targeted period testing

### Features

- Progress indicators for each batch
- Real-time SMS sending with verification
- Comprehensive summary report showing:
  - Total transactions sent
  - Total amount by period
  - Payment method breakdown (UPI vs Credit Cards)
  - Category distribution
  - Success rate statistics
- Automatic error handling
- Chronological ordering for realistic data flow

### Example Output

```
====================================
Comprehensive Test Summary
====================================

Overall Statistics
==================================
  Total Transactions Sent: 100 / 100
  Total Amount: Rs.62,500.00
  Success Rate: 100.0%

Period Breakdown
==================================
  December 2024
    Transactions: 27/27
    Amount: Rs.16,200.00
  September 2025
    Transactions: 22/22
    Amount: Rs.13,800.00
  November 2025
    Transactions: 23/23
    Amount: Rs.14,500.00
  December 2025
    Transactions: 28/28
    Amount: Rs.18,000.00

Payment Method Breakdown
==================================
  UPI Payments:
    HDFCBK UPI: Rs.18,450.00 (35 txns, 29.5%)
    ICICIB UPI: Rs.12,300.00 (28 txns, 19.7%)
    SBIINB UPI: Rs.10,150.00 (24 txns, 16.2%)
    AXISBK UPI: Rs.9,100.00 (21 txns, 14.6%)

  Credit Card Payments:
    HDFCBK CC *1234: Rs.8,200.00 (12 txns, 13.1%)
    ICICIB CC *5678: Rs.4,300.00 (8 txns, 6.9%)

Category Distribution
==================================
  Food Delivery: Rs.15,800.00 (28 txns, 25.3%)
  Shopping: Rs.12,500.00 (18 txns, 20.0%)
  Groceries: Rs.10,200.00 (15 txns, 16.3%)
  Bills: Rs.8,900.00 (14 txns, 14.2%)
  Transport: Rs.7,600.00 (16 txns, 12.2%)
  Entertainment: Rs.5,500.00 (9 txns, 8.8%)
```

## Method 2: Using Android Studio Emulator SMS Injection

### Step 1: Open Extended Controls
1. Run your emulator
2. Click the **"..."** (More) button on the emulator toolbar
3. Select **"Phone"** from the left menu

### Step 2: Send Test SMS

#### UPI Transaction SMS Examples

**HDFC UPI Transaction:**
```
Sender: HDFCBK
Message: Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank
```

**ICICI UPI Transaction:**
```
Sender: ICICIB
Message: Rs.300.00 debited from A/c **5678 for UPI-P2M txn to Zomato on 20-Dec-24. Ref No: 234567890123. -ICICI Bank
```

**SBI UPI Transaction:**
```
Sender: SBIINB
Message: Rs.200.00 debited from A/c **9012 on 20-Dec-24 for VPA txn to Uber. Ref: 345678901234. -SBI
```

**Axis UPI Transaction:**
```
Sender: AXISBK
Message: INR 150.00 debited from A/c no. **3456 for UPI-Ola on 20-Dec-24. UPI Ref No: 456789012345.
```

#### Credit Card Transaction SMS Examples

**HDFC Credit Card:**
```
Sender: HDFCBK
Message: HDFC Bank Credit Card XX1234 has been used for Rs.1,000.00 at AMAZON on 20-Dec-24. Available credit limit: Rs.50,000.00
```

**ICICI Credit Card:**
```
Sender: ICICIB
Message: ICICI Credit Card XX5678 used for Rs.500.00 at Flipkart on 20-Dec-24. Avl Lmt: Rs.40,000
```

**SBI Credit Card:**
```
Sender: SBICARD
Message: SBI Card XX9012 used for Rs.300.00 at Swiggy on 20-Dec-24. Avl Bal: Rs.30,000
```

### Step 3: Verify in App
1. Open Fino app
2. Go to **Home Screen** - transaction should appear
3. Go to **Analytics Screen** - check Payment Methods section
4. You should see:
   - UPI Payments section with bank-wise breakdown
   - Credit Card Payments section with card-wise breakdown

## Method 2: Using ADB Command Line

### Send SMS via ADB
```bash
# Format: adb emu sms send <sender> <message>

# HDFC UPI
adb emu sms send HDFCBK "Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank"

# ICICI Credit Card
adb emu sms send ICICIB "ICICI Credit Card XX5678 used for Rs.500.00 at Flipkart on 20-Dec-24. Avl Lmt: Rs.40,000"

# SBI UPI
adb emu sms send SBIINB "Rs.200.00 debited from A/c **9012 on 20-Dec-24 for VPA txn to Uber. Ref: 345678901234. -SBI"
```

### Check Logcat for Parsing
```bash
# Filter for Fino app logs
adb logcat | grep -i "SmsReceiver\|SmsParser"

# Look for:
# - "SMS received from: HDFCBK"
# - "Parsed transaction: 500.0 from Swiggy"
# - "Transaction saved with ID: 1"
```

## Method 3: Using Telnet to Emulator

### Step 1: Connect to Emulator
```bash
# Find emulator port (usually 5554)
adb devices

# Connect via telnet
telnet localhost 5554
```

### Step 2: Authenticate
```bash
# Get auth token from (Windows)
type %USERPROFILE%\.emulator_console_auth_token

# Or (Linux/Mac)
cat ~/.emulator_console_auth_token

# Authenticate
auth <token>
```

### Step 3: Send SMS
```bash
sms send HDFCBK "Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank"
```

## Expected Results

### After Sending HDFC UPI SMS:
- **Home Screen**: New transaction appears
  - Merchant: Swiggy
  - Amount: ₹500
  - Source: SMS
  - Bank: HDFC (stored in DB)
  - Payment Method: UPI (stored in DB)

- **Analytics Screen → Payment Methods**:
  - UPI Payments section
    - HDFC UPI: ₹500 (100%) • 1 transaction

### After Sending Multiple SMS:
```
UPI Payments (₹1,150)
├── HDFC UPI - ₹500 (43%) • 1 transaction
├── ICICI UPI - ₹300 (26%) • 1 transaction
├── SBI UPI - ₹200 (17%) • 1 transaction
└── Axis UPI - ₹150 (13%) • 1 transaction

Credit Card Payments (₹1,800)
├── HDFC ****1234 - ₹1,000 (56%) • 1 transaction
├── ICICI ****5678 - ₹500 (28%) • 1 transaction
└── SBI ****9012 - ₹300 (17%) • 1 transaction
```

## Debugging Tips

### 1. Check SMS Permissions
```kotlin
// App needs READ_SMS permission
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
```

### 2. Check BroadcastReceiver Registration
Verify in AndroidManifest.xml:
```xml
<receiver android:name=".service.sms.SmsReceiver">
    <intent-filter>
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
</receiver>
```

### 3. Check Logs
```bash
# Watch for transaction processing
adb logcat -s SmsReceiver:D SmsParser:D

# Expected output:
# SmsReceiver: SMS received from: HDFCBK
# SmsReceiver: Parsed transaction: 500.0 from Swiggy
# SmsReceiver: Transaction saved with ID: 1
```

### 4. Check Database
```bash
# Connect to device
adb shell

# Open database (adjust path if needed)
run-as com.fino.app
sqlite3 databases/fino_database

# Query transactions
SELECT id, amount, merchantName, bankName, paymentMethod, cardLastFour FROM transactions;

# Should see:
# 1|500.0|Swiggy|HDFC|UPI|null
```

## Test Scenarios

### Scenario 1: UPI Payment Method Tracking
1. Send HDFC UPI SMS
2. Send ICICI UPI SMS
3. Open Analytics → Should see 2 UPI entries grouped by bank

### Scenario 2: Credit Card Tracking with Multiple Cards
1. Send HDFC card ****1234 SMS
2. Send HDFC card ****5678 SMS (different card, same bank)
3. Open Analytics → Should see 2 separate entries

### Scenario 3: Mixed Payment Methods
1. Send UPI SMS
2. Send Credit Card SMS
3. Add manual transaction (no payment method)
4. Open Analytics → Should see UPI section, Credit Card section, and Unknown section

### Scenario 4: Period Filtering
1. Send multiple SMS
2. Change period selector (Week/Month/Year)
3. Verify payment method breakdown updates correctly

## Common Issues

### Issue: SMS Not Received
**Solution**:
- Check emulator is running
- Verify sender name matches BANK_SENDERS list in SmsReceiver.kt
- Check logcat for "Ignoring non-bank SMS"

### Issue: Transaction Not Parsed
**Solution**:
- Verify SMS format matches regex patterns in parsers
- Check logcat for "Could not parse transaction from SMS"
- Ensure SMS contains amount, merchant, and bank identifier

### Issue: Payment Method Not Showing
**Solution**:
- Verify database version is 6 (check logcat for migration)
- Check transaction has bankName and paymentMethod fields populated
- Ensure you're on the Analytics screen with correct period selected

### Issue: App Crashes on Database Migration
**Solution**:
- Uninstall app completely
- Reinstall (triggers fresh database creation)
- Or: adb shell pm clear com.fino.app

## Quick Test Scripts

### Master Test Suite (Recommended)
For comprehensive testing with all features:
```powershell
.\master-sms-test-suite.ps1
```
Features:
- Interactive menu
- Multiple test scenarios
- Progress tracking
- Comprehensive reporting
- Error handling

### Individual Period Scripts
For testing specific months:
```powershell
# December 2025 (current month, 28 transactions)
.\send-december-2025-sms.ps1

# November 2025 (previous month, 23 transactions)
.\send-november-2025-sms.ps1

# September 2025 (back-to-school, 22 transactions)
.\send-september-2025-sms.ps1

# December 2024 (YoY comparison, 27 transactions)
.\send-december-2024-sms.ps1
```

### Simple Manual Test
For quick manual testing:
```powershell
# test-sms.ps1
$sms = @(
    @{sender="HDFCBK"; msg="Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank"},
    @{sender="ICICIB"; msg="Rs.300.00 debited from A/c **5678 for UPI-P2M txn to Zomato on 20-Dec-24. Ref No: 234567890123. -ICICI Bank"},
    @{sender="HDFCBK"; msg="HDFC Bank Credit Card XX1234 has been used for Rs.1,000.00 at AMAZON on 20-Dec-24."},
    @{sender="ICICIB"; msg="ICICI Credit Card XX5678 used for Rs.500.00 at Flipkart on 20-Dec-24."}
)

foreach ($s in $sms) {
    adb emu sms send $s.sender $s.msg
    Write-Host "Sent SMS from $($s.sender)"
    Start-Sleep -Seconds 2
}
```

## Available Test Data Files

The project includes the following test data scripts:

| Script | Period | Transactions | Amount | Description |
|--------|--------|-------------|--------|-------------|
| `send-december-2025-sms.ps1` | Dec 1-20, 2025 | 28 | Rs.18,000 | Current month, mixed spending |
| `send-november-2025-sms.ps1` | Nov 1-30, 2025 | 23 | Rs.14,500 | Previous month, moderate spending |
| `send-september-2025-sms.ps1` | Sep 1-30, 2025 | 22 | Rs.13,800 | Back-to-school emphasis |
| `send-december-2024-sms.ps1` | Dec 1-31, 2024 | 27 | Rs.16,200 | YoY comparison (10% lower) |

All scripts follow the same format and can be run independently or via the master suite.

---

**Happy Testing!** 🎉
