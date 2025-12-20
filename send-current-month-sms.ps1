# SMS for current month (December 2025) to test current period
# Use different amounts than historical data to distinguish

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "Sending SMS for CURRENT MONTH (December 2025)..." -ForegroundColor Cyan
Write-Host ""

# HDFC UPI - Current month
Write-Host "[1/7] HDFC UPI (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "Paid Rs.750.00 to Starbucks on 20-12-25 using UPI. UPI Ref: 123456789999. -HDFC Bank"
Start-Sleep -Seconds 2

# SBI UPI - Current month
Write-Host "[2/7] SBI UPI (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBIINB" "Rs.450 debited from A/c XX9012 to VPA netflix@paytm on 20-12-25. UPI Ref 345678909999 -SBI"
Start-Sleep -Seconds 2

# ICICI UPI - Current month
Write-Host "[3/7] ICICI UPI (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "INR 600.00 debited from A/c XX5678 on 20-12-25 for UPI to spotify@paytm. Ref 234567899999"
Start-Sleep -Seconds 2

# Axis UPI - Current month
Write-Host "[4/7] Axis UPI (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "AXISBK" "INR 350.00 debited from A/c no. XX3456 on 20-Dec-25 for UPI-BookMyShow. UPI Ref: 456789099999"
Start-Sleep -Seconds 2

# HDFC Credit Card - Current month (must say "has been used")
Write-Host "[5/7] HDFC Credit Card (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "HDFC Bank Credit Card XX1234 has been used for Rs.2500.00 at BigBazaar on 20-12-25 at 14:30:45"
Start-Sleep -Seconds 2

# ICICI Credit Card - Current month (must say "ending" and "INR")
Write-Host "[6/7] ICICI Credit Card (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "Alert: ICICI Card ending 5678 used for INR 1200.00 at MakeMyTrip on 20-Dec-25"
Start-Sleep -Seconds 2

# SBI Credit Card - Current month (must say "ending" and "was used")
Write-Host "[7/7] SBI Credit Card (current month)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBICARD" "Your SBI Card ending 9012 was used for Rs.800 at PVRCinemas on 20/12/2025"
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "All CURRENT MONTH SMS sent!" -ForegroundColor Green
Write-Host "Total: 7 transactions for December 2025" -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected totals:" -ForegroundColor Yellow
Write-Host "  UPI: Rs.2150 (HDFC 750 + SBI 450 + ICICI 600 + Axis 350)" -ForegroundColor White
Write-Host "  Credit Cards: Rs.4500 (HDFC 2500 + ICICI 1200 + SBI 800)" -ForegroundColor White
Write-Host "  Total Spent: Rs.6650" -ForegroundColor White
Write-Host ""
Write-Host "Wait 5 seconds then check logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "=== Checking logs ===" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-String "SMS received|Parsed transaction|Transaction saved" | Select-Object -Last 25
