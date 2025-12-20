# Run AnalyticsViewModel tests
$env:JAVA_HOME = 'C:\projects\Fino\jdk-17.0.13+11'

Write-Host "Running AnalyticsViewModel tests..." -ForegroundColor Cyan
Write-Host ""

.\gradlew.bat testDebugUnitTest --tests "com.fino.app.presentation.viewmodel.AnalyticsViewModelTest"

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Tests passed!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Tests failed!" -ForegroundColor Red
}
