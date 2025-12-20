# SMS that match the existing parser patterns

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "Sending SMS that match parser patterns..." -ForegroundColor Cyan
Write-Host ""

# HDFC UPI - matches pattern: "Paid Rs.XXX to MERCHANT on DD-MM-YY using UPI"
Write-Host "[1/7] HDFC UPI..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "Paid Rs.500.00 to Swiggy on 20-12-24 using UPI. UPI Ref: 123456789012. -HDFC Bank"
Start-Sleep -Seconds 2

# SBI UPI - matches pattern: "Rs.XXX debited from A/c XX1234 to VPA merchant@upi"
Write-Host "[2/7] SBI UPI..." -ForegroundColor Yellow
& $adbPath emu sms send "SBIINB" "Rs.200 debited from A/c XX9012 to VPA uber@paytm on 20-12-24. UPI Ref 345678901234 -SBI"
Start-Sleep -Seconds 2

# ICICI UPI - matches pattern: "INR XXX debited from A/c XX1234 on DD-MM-YY for UPI to merchant@ybl"
Write-Host "[3/7] ICICI UPI..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "INR 300.00 debited from A/c XX5678 on 20-12-24 for UPI to zomato@paytm. Ref 234567890123"
Start-Sleep -Seconds 2

# Axis UPI - matches pattern: "INR XXX debited from A/c no. XX4321 on DD-Mon-YY for UPI-MERCHANT"
Write-Host "[4/7] Axis UPI..." -ForegroundColor Yellow
& $adbPath emu sms send "AXISBK" "INR 150.00 debited from A/c no. XX3456 on 20-Dec-24 for UPI-Ola. UPI Ref: 456789012345"
Start-Sleep -Seconds 2

# HDFC Credit Card - matches pattern: "HDFC Bank Credit Card XX1234 has been used for Rs.XXX at MERCHANT"
Write-Host "[5/7] HDFC Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "HDFCBK" "HDFC Bank Credit Card XX1234 has been used for Rs.1000.00 at AMAZON on 20-12-24 at 14:30:45"
Start-Sleep -Seconds 2

# ICICI Credit Card - matches pattern: "ICICI Card ending 5678 used for INR XXX at MERCHANT"
Write-Host "[6/7] ICICI Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "ICICIB" "Alert: ICICI Card ending 5678 used for INR 500.00 at Flipkart on 20-Dec-24"
Start-Sleep -Seconds 2

# SBI Credit Card - matches pattern: "SBI Card ending 9012 was used for Rs.XXX at MERCHANT"
Write-Host "[7/7] SBI Credit Card..." -ForegroundColor Yellow
& $adbPath emu sms send "SBICARD" "Your SBI Card ending 9012 was used for Rs.300 at Swiggy on 20/12/2024"
Start-Sleep -Seconds 2

Write-Host ""
Write-Host "All SMS sent!" -ForegroundColor Green
Write-Host "These should parse correctly now." -ForegroundColor Cyan
Write-Host ""
Write-Host "Wait 5 seconds then check logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "=== Checking logs ===" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-String "SMS received|Parsed transaction|Transaction saved" | Select-Object -Last 25
