# Build and Install Fino App
# This script sets JAVA_HOME correctly and builds the app

Write-Host "Setting JAVA_HOME to local JDK 17..." -ForegroundColor Cyan
$env:JAVA_HOME = "C:\projects\Fino\jdk-17.0.13+11"

Write-Host "JAVA_HOME set to: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

Write-Host "Building debug APK..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug --warning-mode all

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Build successful! APK location:" -ForegroundColor Green
    Write-Host "app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Yellow
    Write-Host ""

    Write-Host "Checking for connected devices..." -ForegroundColor Cyan
    $adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

    if (Test-Path $adbPath) {
        & $adbPath devices
        Write-Host ""
        Write-Host "Installing on connected device/emulator..." -ForegroundColor Cyan
        .\gradlew.bat installDebug

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "App installed successfully!" -ForegroundColor Green
            Write-Host "You can now launch Fino on your device/emulator" -ForegroundColor Yellow
        } else {
            Write-Host "Installation failed. Make sure a device/emulator is connected." -ForegroundColor Red
        }
    } else {
        Write-Host "ADB not found. Skipping installation." -ForegroundColor Yellow
        Write-Host "Please install manually or connect a device and run: gradlew.bat installDebug" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "Build failed! Please check the errors above." -ForegroundColor Red
}
