# SMS Injection Test Script for Fino App
# This script sends test SMS messages to the emulator to test payment method analytics

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "     Fino App - SMS Injection Test for Payment Analytics" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# Find ADB
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: ADB not found at: $adbPath" -ForegroundColor Red
    Write-Host "Please install Android SDK Platform Tools" -ForegroundColor Red
    exit 1
}

Write-Host "Using ADB: $adbPath" -ForegroundColor Green
Write-Host ""

# Check for connected devices
Write-Host "Checking for connected devices..." -ForegroundColor Cyan
& $adbPath devices
Write-Host ""

$devices = & $adbPath devices | Select-String "emulator" | Measure-Object
if ($devices.Count -eq 0) {
    Write-Host "WARNING: No emulator found. Please start Android Studio emulator first." -ForegroundColor Yellow
    Write-Host "Press any key to continue anyway..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "                    Sending Test SMS Messages" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# Define test SMS messages
$testMessages = @(
    @{
        name = "HDFC UPI Payment"
        sender = "HDFCBK"
        message = "Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank"
        expected = "HDFC UPI, ₹500"
    },
    @{
        name = "ICICI UPI Payment"
        sender = "ICICIB"
        message = "Rs.300.00 debited from A/c **5678 for UPI-P2M txn to Zomato on 20-Dec-24. Ref No: 234567890123. -ICICI Bank"
        expected = "ICICI UPI, ₹300"
    },
    @{
        name = "SBI UPI Payment"
        sender = "SBIINB"
        message = "Rs.200.00 debited from A/c **9012 on 20-Dec-24 for VPA txn to Uber. Ref: 345678901234. -SBI"
        expected = "SBI UPI, ₹200"
    },
    @{
        name = "Axis UPI Payment"
        sender = "AXISBK"
        message = "INR 150.00 debited from A/c no. **3456 for UPI-Ola on 20-Dec-24. UPI Ref No: 456789012345."
        expected = "Axis UPI, ₹150"
    },
    @{
        name = "HDFC Credit Card"
        sender = "HDFCBK"
        message = "HDFC Bank Credit Card XX1234 has been used for Rs.1,000.00 at AMAZON on 20-Dec-24. Available credit limit: Rs.50,000.00"
        expected = "HDFC ****1234, ₹1,000"
    },
    @{
        name = "ICICI Credit Card"
        sender = "ICICIB"
        message = "ICICI Credit Card XX5678 used for Rs.500.00 at Flipkart on 20-Dec-24. Avl Lmt: Rs.40,000"
        expected = "ICICI ****5678, ₹500"
    },
    @{
        name = "SBI Credit Card"
        sender = "SBICARD"
        message = "SBI Card XX9012 used for Rs.300.00 at Swiggy on 20-Dec-24. Avl Bal: Rs.30,000"
        expected = "SBI ****9012, ₹300"
    }
)

$successCount = 0
$totalCount = $testMessages.Count

foreach ($sms in $testMessages) {
    Write-Host "Sending: $($sms.name)..." -ForegroundColor Yellow
    Write-Host "  From: $($sms.sender)" -ForegroundColor Gray
    Write-Host "  Expected: $($sms.expected)" -ForegroundColor Gray

    # Send SMS via ADB
    $result = & $adbPath emu sms send $sms.sender $sms.message 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Sent successfully!" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "  ✗ Failed to send" -ForegroundColor Red
        Write-Host "  Error: $result" -ForegroundColor Red
    }

    Write-Host ""
    Start-Sleep -Seconds 2  # Wait 2 seconds between messages
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "                         Test Summary" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Total SMS Sent: $successCount / $totalCount" -ForegroundColor $(if ($successCount -eq $totalCount) { "Green" } else { "Yellow" })
Write-Host ""

Write-Host "Expected Results in Fino App:" -ForegroundColor Cyan
Write-Host ""
Write-Host "📱 Home Screen:" -ForegroundColor Yellow
Write-Host "   - 7 new transactions should appear" -ForegroundColor White
Write-Host ""
Write-Host "📊 Analytics Screen → Payment Methods:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   UPI Payments (₹1,150)" -ForegroundColor Cyan
Write-Host "   ├── HDFC UPI - ₹500 (43%)" -ForegroundColor White
Write-Host "   ├── ICICI UPI - ₹300 (26%)" -ForegroundColor White
Write-Host "   ├── SBI UPI - ₹200 (17%)" -ForegroundColor White
Write-Host "   └── Axis UPI - ₹150 (13%)" -ForegroundColor White
Write-Host ""
Write-Host "   Credit Card Payments (₹1,800)" -ForegroundColor Magenta
Write-Host "   ├── HDFC ****1234 - ₹1,000 (56%)" -ForegroundColor White
Write-Host "   ├── ICICI ****5678 - ₹500 (28%)" -ForegroundColor White
Write-Host "   └── SBI ****9012 - ₹300 (17%)" -ForegroundColor White
Write-Host ""

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Open Fino app on your emulator" -ForegroundColor White
Write-Host "2. Grant SMS permissions if prompted" -ForegroundColor White
Write-Host "3. Navigate to Analytics screen" -ForegroundColor White
Write-Host "4. Check the 'Payment Methods' section" -ForegroundColor White
Write-Host "5. Verify the breakdown matches the expected results above" -ForegroundColor White
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "To view SMS parsing logs, run:" -ForegroundColor Yellow
Write-Host "  adb logcat -s SmsReceiver:D SmsParser:D" -ForegroundColor Gray
Write-Host ""
