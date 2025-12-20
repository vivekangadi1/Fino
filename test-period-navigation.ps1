# Comprehensive test for period navigation feature
# Sends SMS for both December 2024 (historical) and December 2025 (current)

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Period Navigation Comprehensive Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will send:" -ForegroundColor Yellow
Write-Host "  - 7 transactions for December 2024 (historical)" -ForegroundColor White
Write-Host "  - 7 transactions for December 2025 (current month)" -ForegroundColor White
Write-Host ""
Write-Host "Press Enter to continue..." -ForegroundColor Yellow
Read-Host

# ========================================
# PART 1: December 2024 (Historical Data)
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "PART 1: December 2024 (Historical)" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "[1/7] HDFC UPI (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "Paid Rs.500.00 to Swiggy on 20-12-24 using UPI. UPI Ref: 123456789012. -HDFC Bank"
Start-Sleep -Seconds 2

Write-Host "[2/7] SBI UPI (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBIINB" "Rs.200 debited from A/c XX9012 to VPA uber@paytm on 20-12-24. UPI Ref 345678901234 -SBI"
Start-Sleep -Seconds 2

Write-Host "[3/7] ICICI UPI (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "INR 300.00 debited from A/c XX5678 on 20-12-24 for UPI to zomato@paytm. Ref 234567890123"
Start-Sleep -Seconds 2

Write-Host "[4/7] Axis UPI (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "AXISBK" "INR 150.00 debited from A/c no. XX3456 on 20-Dec-24 for UPI-Ola. UPI Ref: 456789012345"
Start-Sleep -Seconds 2

Write-Host "[5/7] HDFC Credit Card (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "HDFC Bank Credit Card XX1234 has been used for Rs.1000.00 at AMAZON on 20-12-24 at 14:30:45"
Start-Sleep -Seconds 2

Write-Host "[6/7] ICICI Credit Card (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "Alert: ICICI Card ending 5678 used for INR 500.00 at Flipkart on 20-Dec-24"
Start-Sleep -Seconds 2

Write-Host "[7/7] SBI Credit Card (Dec 2024)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBICARD" "Your SBI Card ending 9012 was used for Rs.300 at Swiggy on 20/12/2024"
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "December 2024 data sent!" -ForegroundColor Green
Write-Host "  UPI: Rs.1150 (HDFC 500 + SBI 200 + ICICI 300 + Axis 150)" -ForegroundColor White
Write-Host "  Credit Cards: Rs.1800 (HDFC 1000 + ICICI 500 + SBI 300)" -ForegroundColor White
Write-Host "  Total: Rs.2950" -ForegroundColor White
Write-Host ""
Write-Host "Waiting 5 seconds before sending current month data..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# ========================================
# PART 2: December 2025 (Current Month)
# ========================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "PART 2: December 2025 (Current Month)" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

Write-Host "[1/7] HDFC UPI (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "Paid Rs.750.00 to Starbucks on 20-12-25 using UPI. UPI Ref: 123456789999. -HDFC Bank"
Start-Sleep -Seconds 2

Write-Host "[2/7] SBI UPI (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBIINB" "Rs.450 debited from A/c XX9012 to VPA netflix@paytm on 20-12-25. UPI Ref 345678909999 -SBI"
Start-Sleep -Seconds 2

Write-Host "[3/7] ICICI UPI (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "INR 600.00 debited from A/c XX5678 on 20-12-25 for UPI to spotify@paytm. Ref 234567899999"
Start-Sleep -Seconds 2

Write-Host "[4/7] Axis UPI (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "AXISBK" "INR 350.00 debited from A/c no. XX3456 on 20-Dec-25 for UPI-BookMyShow. UPI Ref: 456789099999"
Start-Sleep -Seconds 2

Write-Host "[5/7] HDFC Credit Card (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "HDFC Bank Credit Card XX1234 has been used for Rs.2500.00 at BigBazaar on 20-12-25 at 14:30:45"
Start-Sleep -Seconds 2

Write-Host "[6/7] ICICI Credit Card (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "Alert: ICICI Card ending 5678 used for INR 1200.00 at MakeMyTrip on 20-Dec-25"
Start-Sleep -Seconds 2

Write-Host "[7/7] SBI Credit Card (Dec 2025)..." -ForegroundColor Yellow
& $adbPath emu sms send "SBICARD" "Your SBI Card ending 9012 was used for Rs.800 at PVRCinemas on 20/12/2025"
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "December 2025 data sent!" -ForegroundColor Green
Write-Host "  UPI: Rs.2150 (HDFC 750 + SBI 450 + ICICI 600 + Axis 350)" -ForegroundColor White
Write-Host "  Credit Cards: Rs.4500 (HDFC 2500 + ICICI 1200 + SBI 800)" -ForegroundColor White
Write-Host "  Total: Rs.6650" -ForegroundColor White
Write-Host ""

# ========================================
# VERIFICATION
# ========================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Data Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "December 2024 (Historical):" -ForegroundColor Yellow
Write-Host "  Total Spent: Rs.2950" -ForegroundColor White
Write-Host "  Transactions: 7 (4 UPI + 3 Credit Card)" -ForegroundColor White
Write-Host ""
Write-Host "December 2025 (Current):" -ForegroundColor Yellow
Write-Host "  Total Spent: Rs.6650" -ForegroundColor White
Write-Host "  Transactions: 7 (4 UPI + 3 Credit Card)" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "How to Test in App" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Open Fino app → Navigate to Analytics" -ForegroundColor White
Write-Host ""
Write-Host "2. Current Period (December 2025):" -ForegroundColor Yellow
Write-Host "   - Should show 'December 2025'" -ForegroundColor White
Write-Host "   - Total Spent: Rs.6650" -ForegroundColor White
Write-Host "   - 7 transactions" -ForegroundColor White
Write-Host "   - Payment methods visible" -ForegroundColor White
Write-Host "   - > button should be DISABLED (can't go to future)" -ForegroundColor White
Write-Host ""
Write-Host "3. Navigate to Historical (December 2024):" -ForegroundColor Yellow
Write-Host "   - Click < button 12 times to go back 12 months" -ForegroundColor White
Write-Host "   - Should show 'December 2024'" -ForegroundColor White
Write-Host "   - Total Spent: Rs.2950" -ForegroundColor White
Write-Host "   - 7 transactions" -ForegroundColor White
Write-Host "   - Different merchants (Swiggy, Uber, Amazon, etc.)" -ForegroundColor White
Write-Host "   - > button should be ENABLED" -ForegroundColor White
Write-Host ""
Write-Host "4. Test Navigation:" -ForegroundColor Yellow
Write-Host "   - Click > to navigate forward month by month" -ForegroundColor White
Write-Host "   - Click period label to jump to current month" -ForegroundColor White
Write-Host "   - Switch between Week/Month/Year tabs" -ForegroundColor White
Write-Host ""
Write-Host "Wait 5 seconds then check logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Recent SMS Parsing Logs" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-String "SMS received|Parsed transaction|Transaction saved" | Select-Object -Last 30

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "Test Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
