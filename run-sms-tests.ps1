$env:JAVA_HOME = Join-Path $PSScriptRoot 'jdk-17.0.13+11'

Write-Host "Running SmsParser tests..." -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat testDebugUnitTest --tests "com.fino.app.service.parser.SmsParserTest"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Tests passed!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Tests failed!" -ForegroundColor Red
}
