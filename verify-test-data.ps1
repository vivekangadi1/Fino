# Verify that test SMS were parsed and saved correctly

$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Verifying Test Data" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Checking all parsed transactions..." -ForegroundColor Yellow
Write-Host ""

# Get all parsing logs
Write-Host "=== All Parsed Transactions ===" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-String "Parsed transaction" | ForEach-Object {
    if ($_ -match "(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}).*Parsed transaction: ([0-9.]+) from (.+)") {
        $timestamp = $matches[1]
        $amount = $matches[2]
        $merchant = $matches[3]
        Write-Host "  Rs.$amount from $merchant" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "=== Transaction Save Confirmations ===" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-String "Transaction saved with ID" | ForEach-Object {
    if ($_ -match "Transaction saved with ID: (\d+)") {
        $id = $matches[1]
        Write-Host "  Transaction saved with ID: $id" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "=== Counting December 2024 Transactions ===" -ForegroundColor Cyan
$dec2024Count = (& $adbPath logcat -d -s SmsReceiver:D | Select-String "20-12-24" | Measure-Object).Count
Write-Host "  Found $dec2024Count SMS for December 2024" -ForegroundColor White

Write-Host ""
Write-Host "=== Counting December 2025 Transactions ===" -ForegroundColor Cyan
$dec2025Count = (& $adbPath logcat -d -s SmsReceiver:D | Select-String "20-12-25" | Measure-Object).Count
Write-Host "  Found $dec2025Count SMS for December 2025" -ForegroundColor White

Write-Host ""
Write-Host "=== Any Parsing Errors ===" -ForegroundColor Cyan
$errors = & $adbPath logcat -d -s SmsReceiver:D | Select-String "Could not parse"
if ($errors) {
    Write-Host "  Errors found:" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
} else {
    Write-Host "  No parsing errors found!" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected: 14 transactions total (7 from Dec 2024 + 7 from Dec 2025)" -ForegroundColor Yellow
Write-Host "December 2024: $dec2024Count SMS received" -ForegroundColor White
Write-Host "December 2025: $dec2025Count SMS received" -ForegroundColor White
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Open Fino app on emulator" -ForegroundColor White
Write-Host "  2. Go to Analytics screen" -ForegroundColor White
Write-Host "  3. Current view should show December 2025 data (Rs.6650)" -ForegroundColor White
Write-Host "  4. Click < button 12 times to navigate to December 2024" -ForegroundColor White
Write-Host "  5. Should see December 2024 data (Rs.2950)" -ForegroundColor White
Write-Host ""
