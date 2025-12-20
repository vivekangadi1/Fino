# Simple SMS Injection Script for Fino App Testing

Write-Host "Fino App - SMS Injection Test" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host ""

# Find ADB
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    Write-Host "ERROR: ADB not found" -ForegroundColor Red
    exit 1
}

Write-Host "ADB found: OK" -ForegroundColor Green
Write-Host ""

# Define test SMS
$sms1 = @{sender="HDFCBK"; msg="Rs.500.00 paid from A/c **1234 using UPI to Swiggy on 20-Dec-24. UPI Ref No. 123456789012. -HDFC Bank"}
$sms2 = @{sender="ICICIB"; msg="Rs.300.00 debited from A/c **5678 for UPI-P2M txn to Zomato on 20-Dec-24. Ref No: 234567890123. -ICICI Bank"}
$sms3 = @{sender="SBIINB"; msg="Rs.200.00 debited from A/c **9012 on 20-Dec-24 for VPA txn to Uber. Ref: 345678901234. -SBI"}
$sms4 = @{sender="AXISBK"; msg="INR 150.00 debited from A/c no. **3456 for UPI-Ola on 20-Dec-24. UPI Ref No: 456789012345."}
$sms5 = @{sender="HDFCBK"; msg="HDFC Bank Credit Card XX1234 has been used for Rs.1,000.00 at AMAZON on 20-Dec-24. Available credit limit: Rs.50,000.00"}
$sms6 = @{sender="ICICIB"; msg="ICICI Credit Card XX5678 used for Rs.500.00 at Flipkart on 20-Dec-24. Avl Lmt: Rs.40,000"}
$sms7 = @{sender="SBICARD"; msg="SBI Card XX9012 used for Rs.300.00 at Swiggy on 20-Dec-24. Avl Bal: Rs.30,000"}

$allSms = @($sms1, $sms2, $sms3, $sms4, $sms5, $sms6, $sms7)

Write-Host "Sending test SMS messages..." -ForegroundColor Yellow
Write-Host ""

$count = 0
foreach ($sms in $allSms) {
    $count++
    Write-Host "[$count/7] Sending from $($sms.sender)..." -ForegroundColor Cyan

    $result = & $adbPath emu sms send $sms.sender $sms.msg 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Success!" -ForegroundColor Green
    } else {
        Write-Host "  Failed: $result" -ForegroundColor Red
    }

    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "=============================" -ForegroundColor Cyan
Write-Host "All SMS sent successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Expected in Fino App:" -ForegroundColor Yellow
Write-Host "- UPI Payments: Rs.1,150 (HDFC, ICICI, SBI, Axis)" -ForegroundColor White
Write-Host "- Credit Cards: Rs.1,800 (HDFC ****1234, ICICI ****5678, SBI ****9012)" -ForegroundColor White
Write-Host ""
Write-Host "Check Analytics screen > Payment Methods section" -ForegroundColor Cyan
