# Check SMS parsing logs
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

Write-Host "Checking app logs for SMS processing..." -ForegroundColor Cyan
Write-Host ""

& $adbPath logcat -d -s SmsReceiver:D SmsParser:D | Select-String "SMS received|Parsed transaction|Transaction saved" | Select-Object -Last 20

Write-Host ""
Write-Host "If you see 'Transaction saved' messages above, the SMS parsing is working!" -ForegroundColor Green
