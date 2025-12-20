# Check for SMS parsing errors
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "Checking for parsing errors..." -ForegroundColor Yellow
Write-Host ""

Write-Host "=== All SmsReceiver logs ===" -ForegroundColor Cyan
& $adbPath logcat -d -s SmsReceiver:D | Select-Object -Last 30

Write-Host ""
Write-Host "=== Any parsing errors ===" -ForegroundColor Cyan
& $adbPath logcat -d | Select-String "Could not parse|parsing|SmsParser" | Select-Object -Last 10
