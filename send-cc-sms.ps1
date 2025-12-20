# Send correctly formatted Credit Card SMS
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "Sending Credit Card SMS..." -ForegroundColor Cyan
Write-Host ""

# HDFC Credit Card - "has been used"
Write-Host "[1/3] HDFC Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "HDFC Bank Credit Card XX1234 has been used for Rs.1000.00 at AMAZON on 20-12-24 at 14:30:45"
Start-Sleep -Seconds 2

# ICICI Credit Card - "Card ending"
Write-Host "[2/3] ICICI Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "Alert: ICICI Card ending 5678 used for INR 500.00 at Flipkart on 20-Dec-24"
Start-Sleep -Seconds 2

# SBI Credit Card - "Card ending was used"
Write-Host "[3/3] SBI Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "SBICARD" "Your SBI Card ending 9012 was used for Rs.300 at Swiggy on 20/12/2024"
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "All Credit Card SMS sent!" -ForegroundColor Green
Write-Host ""
Write-Host "Checking logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

& $adbPath logcat -d -s SmsReceiver:D | Select-String "SMS received|Parsed transaction|Transaction saved" | Select-Object -Last 15
