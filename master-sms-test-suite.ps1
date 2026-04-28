#Requires -Version 5.1
<#
.SYNOPSIS
    Master SMS Test Suite Orchestrator for Fino App
.DESCRIPTION
    Menu-driven interface for sending SMS test data to Android emulator.
    Supports multiple test scenarios with progress tracking and comprehensive reporting.
.NOTES
    Author: Claude Code
    Version: 1.0.0
    Date: 2025-12-20
#>

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$ProgressPreference = "Continue"

#region Configuration
$Script:Config = @{
    AdbPath = "adb"
    SmsDelay = 800  # milliseconds between SMS
    BatchSize = 10  # Progress update frequency
    Encoding = [System.Text.Encoding]::UTF8
}

$Script:TestDataScripts = @{
    "Dec2025" = @{
        Path = "send-december-2025-sms.ps1"
        Transactions = 28
        ExpectedTotal = 18000
        Period = "December 2025"
        SortOrder = 1
    }
    "Nov2025" = @{
        Path = "send-november-2025-sms.ps1"
        Transactions = 23
        ExpectedTotal = 14500
        Period = "November 2025"
        SortOrder = 2
    }
    "Sep2025" = @{
        Path = "send-september-2025-sms.ps1"
        Transactions = 22
        ExpectedTotal = 13800
        Period = "September 2025"
        SortOrder = 3
    }
    "Dec2024" = @{
        Path = "send-december-2024-sms.ps1"
        Transactions = 27
        ExpectedTotal = 16200
        Period = "December 2024"
        SortOrder = 4
    }
}
#endregion

#region Helper Functions

function Write-ColorLine {
    param(
        [string]$Message,
        [ConsoleColor]$Color = "White",
        [switch]$NoNewline
    )

    $params = @{
        Object = $Message
        ForegroundColor = $Color
    }
    if ($NoNewline) { $params.NoNewline = $true }
    Write-Host @params
}

function Write-Header {
    param([string]$Title)

    $width = 70
    $padding = [math]::Max(0, ($width - $Title.Length) / 2)
    $paddingStr = "=" * [math]::Floor($padding)

    Write-Host ""
    Write-ColorLine ("=" * $width) -Color Cyan
    Write-ColorLine ("{0}{1}{0}" -f $paddingStr, $Title) -Color Cyan
    Write-ColorLine ("=" * $width) -Color Cyan
    Write-Host ""
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-ColorLine ("-" * 70) -Color DarkCyan
    Write-ColorLine $Title -Color Yellow
    Write-ColorLine ("-" * 70) -Color DarkCyan
}

function Test-AdbConnection {
    try {
        $devices = & $Script:Config.AdbPath devices 2>&1
        if ($devices -match "emulator-\d+") {
            Write-ColorLine "✓ Emulator connected" -Color Green
            return $true
        }
        else {
            Write-ColorLine "✗ No emulator detected" -Color Red
            Write-ColorLine "  Please start the Android emulator first." -Color Yellow
            return $false
        }
    }
    catch {
        Write-ColorLine "✗ ADB not found or not accessible" -Color Red
        Write-ColorLine "  Error: $_" -Color DarkRed
        return $false
    }
}

function Get-UpiRef {
    return (Get-Random -Minimum 100000000000 -Maximum 999999999999).ToString()
}

function Send-Sms {
    param(
        [string]$Sender,
        [string]$Message,
        [switch]$Silent
    )

    try {
        $output = & $Script:Config.AdbPath emu sms send $Sender $Message 2>&1
        Start-Sleep -Milliseconds $Script:Config.SmsDelay
        return $true
    }
    catch {
        if (-not $Silent) {
            Write-ColorLine "  ⚠ Failed: $_" -Color Yellow
        }
        return $false
    }
}

function Build-SmsMessage {
    param(
        [hashtable]$Transaction
    )

    if ($Transaction.type -eq "UPI") {
        $message = switch ($Transaction.sender) {
            "HDFCBK" { "Paid Rs.$($Transaction.amount) to $($Transaction.merchant) on $($Transaction.date) using UPI. UPI Ref: $($Transaction.upiRef). -HDFC Bank" }
            "SBIINB" { "Rs.$($Transaction.amount) debited from A/c XX9012 to VPA $($Transaction.merchant)@paytm on $($Transaction.date). UPI Ref $($Transaction.upiRef) -SBI" }
            "ICICIB" { "INR $($Transaction.amount) debited from A/c XX5678 on $($Transaction.date) for UPI to $($Transaction.merchant)@paytm. Ref $($Transaction.upiRef)" }
            "AXISBK" { "INR $($Transaction.amount) debited from A/c no. XX3456 on $($Transaction.date) for UPI-$($Transaction.merchant). UPI Ref: $($Transaction.upiRef)" }
        }
    }
    else {
        # Credit Card
        $message = switch ($Transaction.sender) {
            "HDFCBK" { "HDFC Bank Credit Card XX$($Transaction.cardLast4) has been used for Rs.$($Transaction.amount) at $($Transaction.merchant) on $($Transaction.date) at 14:30:45" }
            "ICICIB" { "Alert: ICICI Card ending $($Transaction.cardLast4) used for INR $($Transaction.amount) at $($Transaction.merchant) on $($Transaction.date)" }
            "SBIINB" { "Your SBI Card ending $($Transaction.cardLast4) was used for Rs.$($Transaction.amount) at $($Transaction.merchant) on $($Transaction.date)" }
            "AXISBK" { "Axis Bank Card XX$($Transaction.cardLast4): Rs.$($Transaction.amount) spent at $($Transaction.merchant) on $($Transaction.date)" }
        }
    }

    return $message
}

function Send-TransactionBatch {
    param(
        [array]$Transactions,
        [string]$BatchName,
        [ref]$Stats
    )

    $batchTotal = 0
    $batchSuccess = 0
    $batchPaymentMethods = @{}
    $batchCategories = @{}

    Write-Section "Sending $BatchName"
    Write-ColorLine "Total transactions: $($Transactions.Count)" -Color White
    Write-Host ""

    for ($i = 0; $i -lt $Transactions.Count; $i++) {
        $tx = $Transactions[$i]
        $num = $i + 1
        $percent = [math]::Round(($num / $Transactions.Count) * 100)

        # Show progress
        if ($num % $Script:Config.BatchSize -eq 0 -or $num -eq $Transactions.Count) {
            Write-Progress -Activity "Sending $BatchName" `
                          -Status "$num of $($Transactions.Count) transactions" `
                          -PercentComplete $percent
        }

        Write-ColorLine "[$num/$($Transactions.Count)] " -Color White -NoNewline
        Write-ColorLine "$($tx.merchant) - ₹$($tx.amount) " -Color Yellow -NoNewline
        Write-ColorLine "($($tx.date))" -Color DarkGray

        # Build and send SMS
        $message = Build-SmsMessage -Transaction $tx

        if (Send-Sms -Sender $tx.sender -Message $message -Silent) {
            $batchSuccess++
            $amount = [double]$tx.amount
            $batchTotal += $amount

            # Track payment method
            $paymentMethod = if ($tx.type -eq "UPI") {
                "$($tx.sender) UPI"
            } else {
                "$($tx.sender) CC *$($tx.cardLast4)"
            }

            if (-not $batchPaymentMethods.ContainsKey($paymentMethod)) {
                $batchPaymentMethods[$paymentMethod] = @{
                    Count = 0
                    Total = 0
                    Type = $tx.type
                }
            }
            $batchPaymentMethods[$paymentMethod].Count++
            $batchPaymentMethods[$paymentMethod].Total += $amount

            # Track category (simple categorization)
            $category = Get-TransactionCategory -Merchant $tx.merchant
            if (-not $batchCategories.ContainsKey($category)) {
                $batchCategories[$category] = @{
                    Count = 0
                    Total = 0
                }
            }
            $batchCategories[$category].Count++
            $batchCategories[$category].Total += $amount

            Write-ColorLine "  ✓ Sent" -Color Green
        }
        else {
            Write-ColorLine "  ✗ Failed" -Color Red
        }
    }

    Write-Progress -Activity "Sending $BatchName" -Completed

    # Update overall stats
    $Stats.Value.TotalSent += $batchSuccess
    $Stats.Value.TotalTransactions += $Transactions.Count
    $Stats.Value.TotalAmount += $batchTotal

    # Merge payment methods
    foreach ($pm in $batchPaymentMethods.Keys) {
        if (-not $Stats.Value.PaymentMethods.ContainsKey($pm)) {
            $Stats.Value.PaymentMethods[$pm] = @{
                Count = 0
                Total = 0
                Type = $batchPaymentMethods[$pm].Type
            }
        }
        $Stats.Value.PaymentMethods[$pm].Count += $batchPaymentMethods[$pm].Count
        $Stats.Value.PaymentMethods[$pm].Total += $batchPaymentMethods[$pm].Total
    }

    # Merge categories
    foreach ($cat in $batchCategories.Keys) {
        if (-not $Stats.Value.Categories.ContainsKey($cat)) {
            $Stats.Value.Categories[$cat] = @{
                Count = 0
                Total = 0
            }
        }
        $Stats.Value.Categories[$cat].Count += $batchCategories[$cat].Count
        $Stats.Value.Categories[$cat].Total += $batchCategories[$cat].Total
    }

    # Add period stat
    $Stats.Value.Periods[$BatchName] = @{
        Sent = $batchSuccess
        Total = $Transactions.Count
        Amount = $batchTotal
    }

    # Show batch summary
    Write-Host ""
    Write-ColorLine "Batch Complete: $batchSuccess/$($Transactions.Count) sent, ₹$([math]::Round($batchTotal, 2))" -Color Green

    # Wait for processing
    Write-ColorLine "Waiting 3 seconds for SMS processing..." -Color DarkGray
    Start-Sleep -Seconds 3
}

function Get-TransactionCategory {
    param([string]$Merchant)

    $categoryMap = @{
        "Netflix|Spotify|Amazon Prime|Steam|BookMyShow|PVR|Coursera" = "Entertainment"
        "Swiggy|Zomato|Dominos|McDonalds|Starbucks" = "Food Delivery"
        "BigBasket|DMart" = "Groceries"
        "Uber|Ola|Rapido" = "Transport"
        "Amazon|Flipkart|Myntra|Nykaa|Ajio" = "Shopping"
        "Electricity|Airtel|Jio" = "Bills"
        "Tuition|Crossword|Sapna|Staples" = "Education"
    }

    foreach ($pattern in $categoryMap.Keys) {
        if ($Merchant -match $pattern) {
            return $categoryMap[$pattern]
        }
    }

    return "Other"
}

function Load-TransactionsFromScript {
    param([string]$ScriptPath)

    if (-not (Test-Path $ScriptPath)) {
        Write-ColorLine "  ✗ Script not found: $ScriptPath" -Color Red
        return @()
    }

    # Execute script in isolated scope to get transactions
    try {
        $scriptContent = Get-Content $ScriptPath -Raw

        # Extract transactions array using regex
        if ($scriptContent -match '\$transactions\s*=\s*@\(([\s\S]*?)\)[\s]*\n') {
            $transactionsCode = '$transactions = @(' + $matches[1] + ')'

            # Execute in new scope
            $sb = [ScriptBlock]::Create($transactionsCode + "`n; `$transactions")
            $transactions = & $sb

            # Add UPI refs where needed
            foreach ($tx in $transactions) {
                if ($tx.type -eq "UPI" -and -not $tx.upiRef) {
                    $tx.upiRef = Get-UpiRef
                }
            }

            return $transactions
        }
        else {
            Write-ColorLine "  ⚠ Could not parse transactions from script" -Color Yellow
            return @()
        }
    }
    catch {
        Write-ColorLine "  ✗ Error loading transactions: $_" -Color Red
        return @()
    }
}

function Show-ComprehensiveReport {
    param([hashtable]$Stats)

    Write-Header "Comprehensive Test Summary"

    # Overall Stats
    Write-ColorLine "Overall Statistics" -Color Cyan
    Write-ColorLine ("=" * 70) -Color DarkCyan
    Write-ColorLine "  Total Transactions Sent: " -Color White -NoNewline
    Write-ColorLine "$($Stats.TotalSent) / $($Stats.TotalTransactions)" -Color Green
    Write-ColorLine "  Total Amount: " -Color White -NoNewline
    Write-ColorLine "₹$([math]::Round($Stats.TotalAmount, 2))" -Color Green
    Write-ColorLine "  Success Rate: " -Color White -NoNewline
    $successRate = if ($Stats.TotalTransactions -gt 0) {
        ($Stats.TotalSent / $Stats.TotalTransactions) * 100
    } else { 0 }
    Write-ColorLine "$([math]::Round($successRate, 1))%" -Color Green

    # Period Breakdown
    if ($Stats.Periods.Count -gt 0) {
        Write-Host ""
        Write-ColorLine "Period Breakdown" -Color Cyan
        Write-ColorLine ("=" * 70) -Color DarkCyan

        foreach ($period in ($Stats.Periods.Keys | Sort-Object)) {
            $p = $Stats.Periods[$period]
            Write-ColorLine "  $period" -Color Yellow
            Write-ColorLine "    Transactions: " -Color White -NoNewline
            Write-ColorLine "$($p.Sent)/$($p.Total)" -Color Gray
            Write-ColorLine "    Amount: " -Color White -NoNewline
            Write-ColorLine "₹$([math]::Round($p.Amount, 2))" -Color Gray
        }
    }

    # Payment Method Breakdown
    if ($Stats.PaymentMethods.Count -gt 0) {
        Write-Host ""
        Write-ColorLine "Payment Method Breakdown" -Color Cyan
        Write-ColorLine ("=" * 70) -Color DarkCyan

        # Group by type
        $upiMethods = $Stats.PaymentMethods.GetEnumerator() | Where-Object { $_.Value.Type -eq "UPI" }
        $ccMethods = $Stats.PaymentMethods.GetEnumerator() | Where-Object { $_.Value.Type -eq "CreditCard" }

        if ($upiMethods) {
            Write-ColorLine "  UPI Payments:" -Color Yellow
            foreach ($pm in ($upiMethods | Sort-Object { $_.Value.Total } -Descending)) {
                $percent = ($pm.Value.Total / $Stats.TotalAmount) * 100
                $txnCount = $pm.Value.Count
                $roundedTotal = [math]::Round($pm.Value.Total, 2)
                $roundedPercent = [math]::Round($percent, 1)
                Write-ColorLine "    $($pm.Key): " -Color White -NoNewline
                Write-ColorLine "₹$roundedTotal " -Color Gray -NoNewline
                Write-ColorLine "($txnCount txns, $roundedPercent%)" -Color DarkGray
            }
        }

        if ($ccMethods) {
            Write-Host ""
            Write-ColorLine "  Credit Card Payments:" -Color Magenta
            foreach ($pm in ($ccMethods | Sort-Object { $_.Value.Total } -Descending)) {
                $percent = ($pm.Value.Total / $Stats.TotalAmount) * 100
                $txnCount = $pm.Value.Count
                $roundedTotal = [math]::Round($pm.Value.Total, 2)
                $roundedPercent = [math]::Round($percent, 1)
                Write-ColorLine "    $($pm.Key): " -Color White -NoNewline
                Write-ColorLine "₹$roundedTotal " -Color Gray -NoNewline
                Write-ColorLine "($txnCount txns, $roundedPercent%)" -Color DarkGray
            }
        }
    }

    # Category Distribution
    if ($Stats.Categories.Count -gt 0) {
        Write-Host ""
        Write-ColorLine "Category Distribution" -Color Cyan
        Write-ColorLine ("=" * 70) -Color DarkCyan

        foreach ($cat in ($Stats.Categories.GetEnumerator() | Sort-Object { $_.Value.Total } -Descending)) {
            $percent = ($cat.Value.Total / $Stats.TotalAmount) * 100
            $txnCount = $cat.Value.Count
            $roundedTotal = [math]::Round($cat.Value.Total, 2)
            $roundedPercent = [math]::Round($percent, 1)
            Write-ColorLine "  $($cat.Key): " -Color Yellow -NoNewline
            Write-ColorLine "₹$roundedTotal " -Color White -NoNewline
            Write-ColorLine "($txnCount txns, $roundedPercent%)" -Color DarkGray
        }
    }

    # Verification Instructions
    Write-Host ""
    Write-ColorLine "Verification Steps" -Color Cyan
    Write-ColorLine ("=" * 70) -Color DarkCyan
    Write-ColorLine "  1. Wait 5-10 seconds for final SMS processing" -Color Gray
    Write-ColorLine "  2. Open Fino app on the emulator" -Color Gray
    Write-ColorLine "  3. Grant SMS permissions if prompted" -Color Gray
    Write-ColorLine "  4. Navigate to Home screen to see transactions" -Color Gray
    Write-ColorLine "  5. Check Analytics screen for period breakdowns" -Color Gray
    Write-ColorLine "  6. Verify Payment Methods section" -Color Gray
    Write-Host ""

    Write-ColorLine ("=" * 70) -Color Green
    Write-ColorLine "Test suite execution completed successfully!" -Color Green
    Write-ColorLine ("=" * 70) -Color Green
    Write-Host ""
}

function Show-Menu {
    Write-Header "Fino SMS Test Suite - Main Menu"

    Write-ColorLine "Available Test Scenarios:" -Color Cyan
    Write-Host ""
    Write-ColorLine "  [1] Send Minimal Test Set" -Color Yellow
    Write-ColorLine "      4 key periods: Dec 2025, Nov 2025, Sep 2025, Dec 2024" -Color Gray
    Write-ColorLine "      Total: ~100 transactions, ₹62,500" -Color Gray
    Write-Host ""
    Write-ColorLine "  [2] Send All 2024 Data" -Color Yellow
    Write-ColorLine "      Historical data: Dec 2024" -Color Gray
    Write-ColorLine "      Total: ~27 transactions, ₹16,200" -Color Gray
    Write-Host ""
    Write-ColorLine "  [3] Send All 2025 Data" -Color Yellow
    Write-ColorLine "      Current year: Dec 2025, Nov 2025, Sep 2025" -Color Gray
    Write-ColorLine "      Total: ~73 transactions, ₹46,300" -Color Gray
    Write-Host ""
    Write-ColorLine "  [4] Send Everything (Chronological)" -Color Yellow
    Write-ColorLine "      All available test data in date order" -Color Gray
    Write-ColorLine "      Total: ~100 transactions, ₹62,500" -Color Gray
    Write-Host ""
    Write-ColorLine "  [5] Send Specific Month" -Color Yellow
    Write-ColorLine "      Choose individual month to send" -Color Gray
    Write-Host ""
    Write-ColorLine "  [0] Exit" -Color Red
    Write-Host ""
    Write-ColorLine ("=" * 70) -Color Cyan
    Write-Host ""
}

function Get-MonthChoice {
    Write-Header "Select Month to Send"

    $months = $Script:TestDataScripts.GetEnumerator() | Sort-Object { $_.Value.SortOrder }

    $i = 1
    foreach ($month in $months) {
        Write-ColorLine "  [$i] $($month.Value.Period)" -Color Yellow
        Write-ColorLine "      $($month.Value.Transactions) transactions, ₹$($month.Value.ExpectedTotal)" -Color Gray
        $i++
    }
    Write-Host ""
    Write-ColorLine "  [0] Back to Main Menu" -Color Red
    Write-Host ""

    do {
        $choice = Read-Host "Enter choice (0-$($months.Count))"
        $choiceNum = 0
        $valid = [int]::TryParse($choice, [ref]$choiceNum) -and $choiceNum -ge 0 -and $choiceNum -le $months.Count

        if (-not $valid) {
            Write-ColorLine "Invalid choice. Please enter 0-$($months.Count)" -Color Red
        }
    } while (-not $valid)

    if ($choiceNum -eq 0) {
        return $null
    }

    return ($months | Select-Object -Index ($choiceNum - 1)).Key
}

function Start-MinimalTestSet {
    Write-Header "Minimal Test Set (4 Key Periods)"

    $stats = @{
        TotalSent = 0
        TotalTransactions = 0
        TotalAmount = 0
        PaymentMethods = @{}
        Categories = @{}
        Periods = @{}
    }

    # Send in chronological order: Dec 2024 -> Sep 2025 -> Nov 2025 -> Dec 2025
    $order = @("Dec2024", "Sep2025", "Nov2025", "Dec2025")

    foreach ($key in $order) {
        $script = $Script:TestDataScripts[$key]
        $transactions = Load-TransactionsFromScript -ScriptPath $script.Path

        if ($transactions.Count -gt 0) {
            Send-TransactionBatch -Transactions $transactions `
                                 -BatchName $script.Period `
                                 -Stats ([ref]$stats)
        }
        else {
            Write-ColorLine "⚠ Skipping $($script.Period) - no transactions loaded" -Color Yellow
        }
    }

    Show-ComprehensiveReport -Stats $stats
}

function Start-All2024Data {
    Write-Header "All 2024 Data"

    $stats = @{
        TotalSent = 0
        TotalTransactions = 0
        TotalAmount = 0
        PaymentMethods = @{}
        Categories = @{}
        Periods = @{}
    }

    $script = $Script:TestDataScripts["Dec2024"]
    $transactions = Load-TransactionsFromScript -ScriptPath $script.Path

    if ($transactions.Count -gt 0) {
        Send-TransactionBatch -Transactions $transactions `
                             -BatchName $script.Period `
                             -Stats ([ref]$stats)
    }

    Show-ComprehensiveReport -Stats $stats
}

function Start-All2025Data {
    Write-Header "All 2025 Data"

    $stats = @{
        TotalSent = 0
        TotalTransactions = 0
        TotalAmount = 0
        PaymentMethods = @{}
        Categories = @{}
        Periods = @{}
    }

    # Send in chronological order: Sep -> Nov -> Dec
    $order = @("Sep2025", "Nov2025", "Dec2025")

    foreach ($key in $order) {
        $script = $Script:TestDataScripts[$key]
        $transactions = Load-TransactionsFromScript -ScriptPath $script.Path

        if ($transactions.Count -gt 0) {
            Send-TransactionBatch -Transactions $transactions `
                                 -BatchName $script.Period `
                                 -Stats ([ref]$stats)
        }
        else {
            Write-ColorLine "⚠ Skipping $($script.Period) - no transactions loaded" -Color Yellow
        }
    }

    Show-ComprehensiveReport -Stats $stats
}

function Start-Everything {
    Write-Header "Everything (Chronological Order)"

    Write-ColorLine "This will send all available test data in chronological order." -Color Yellow
    Write-ColorLine "Estimated time: 2-3 minutes" -Color Gray
    Write-Host ""

    $stats = @{
        TotalSent = 0
        TotalTransactions = 0
        TotalAmount = 0
        PaymentMethods = @{}
        Categories = @{}
        Periods = @{}
    }

    # Chronological order: Dec 2024 -> Sep 2025 -> Nov 2025 -> Dec 2025
    $order = @("Dec2024", "Sep2025", "Nov2025", "Dec2025")

    foreach ($key in $order) {
        $script = $Script:TestDataScripts[$key]
        $transactions = Load-TransactionsFromScript -ScriptPath $script.Path

        if ($transactions.Count -gt 0) {
            Send-TransactionBatch -Transactions $transactions `
                                 -BatchName $script.Period `
                                 -Stats ([ref]$stats)
        }
        else {
            Write-ColorLine "⚠ Skipping $($script.Period) - no transactions loaded" -Color Yellow
        }
    }

    Show-ComprehensiveReport -Stats $stats
}

function Start-SpecificMonth {
    $monthKey = Get-MonthChoice

    if ($null -eq $monthKey) {
        return  # Back to main menu
    }

    $script = $Script:TestDataScripts[$monthKey]

    Write-Header "Sending $($script.Period)"

    $stats = @{
        TotalSent = 0
        TotalTransactions = 0
        TotalAmount = 0
        PaymentMethods = @{}
        Categories = @{}
        Periods = @{}
    }

    $transactions = Load-TransactionsFromScript -ScriptPath $script.Path

    if ($transactions.Count -gt 0) {
        Send-TransactionBatch -Transactions $transactions `
                             -BatchName $script.Period `
                             -Stats ([ref]$stats)

        Show-ComprehensiveReport -Stats $stats
    }
    else {
        Write-ColorLine "✗ Failed to load transactions from $($script.Path)" -Color Red
    }
}

#endregion

#region Main Script

function Main {
    Clear-Host

    Write-Header "Fino SMS Test Suite v1.0.0"

    # Check ADB connection
    Write-Section "Checking Prerequisites"

    if (-not (Test-AdbConnection)) {
        Write-Host ""
        Write-ColorLine "Cannot proceed without emulator connection." -Color Red
        Write-ColorLine "Press any key to exit..." -Color Yellow
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        exit 1
    }

    # Verify test data scripts exist
    Write-Host ""
    Write-ColorLine "Verifying test data scripts..." -Color Yellow
    $missingScripts = @()

    foreach ($key in $Script:TestDataScripts.Keys) {
        $scriptPath = $Script:TestDataScripts[$key].Path
        if (Test-Path $scriptPath) {
            Write-ColorLine "  ✓ $scriptPath" -Color Green
        }
        else {
            Write-ColorLine "  ✗ $scriptPath (NOT FOUND)" -Color Red
            $missingScripts += $scriptPath
        }
    }

    if ($missingScripts.Count -gt 0) {
        Write-Host ""
        Write-ColorLine "⚠ Warning: Some test data scripts are missing." -Color Yellow
        Write-ColorLine "  Missing scripts will be skipped." -Color Gray
        Write-Host ""
        Write-ColorLine "Press any key to continue..." -Color Yellow
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }

    # Main menu loop
    do {
        Clear-Host
        Show-Menu

        $choice = Read-Host "Enter your choice"

        switch ($choice) {
            "1" {
                Start-MinimalTestSet
                Write-Host ""
                Write-ColorLine "Press any key to return to menu..." -Color Yellow
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
            "2" {
                Start-All2024Data
                Write-Host ""
                Write-ColorLine "Press any key to return to menu..." -Color Yellow
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
            "3" {
                Start-All2025Data
                Write-Host ""
                Write-ColorLine "Press any key to return to menu..." -Color Yellow
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
            "4" {
                Start-Everything
                Write-Host ""
                Write-ColorLine "Press any key to return to menu..." -Color Yellow
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
            "5" {
                Start-SpecificMonth
                Write-Host ""
                Write-ColorLine "Press any key to return to menu..." -Color Yellow
                $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
            }
            "0" {
                Clear-Host
                Write-Header "Goodbye!"
                Write-ColorLine "Thank you for using Fino SMS Test Suite." -Color Cyan
                Write-Host ""
                return
            }
            default {
                Write-ColorLine "Invalid choice. Please try again." -Color Red
                Start-Sleep -Seconds 2
            }
        }
    } while ($true)
}

# Script entry point
try {
    Main
}
catch {
    Write-Host ""
    Write-ColorLine "Fatal Error: $_" -Color Red
    Write-ColorLine "Stack Trace:" -Color DarkRed
    Write-ColorLine $_.ScriptStackTrace -Color DarkRed
    Write-Host ""
    exit 1
}

#endregion
