# SMS Testing Guide for Fino App

## Method 1: Using Android Studio Emulator SMS Injection

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

## Quick Test Script

### PowerShell Script to Send Multiple SMS
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

Run: `.\test-sms.ps1`

---

**Happy Testing!** 🎉
