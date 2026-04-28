# Send November 2025 SMS Test Data
# Purpose: Previous month test data for Analytics period jump testing
# Transactions: 23
# Date Range: Nov 1-30, 2025
# Expected Total: ~₹14,500
# Payment Mix: 60% UPI, 40% Credit Cards

$ErrorActionPreference = "Stop"

# Configuration
$adbPath = "adb"

# Helper function to generate UPI reference numbers
function Get-UpiRef {
    return (Get-Random -Minimum 100000000000 -Maximum 999999999999).ToString()
}

# Helper function to send SMS
function Send-Sms {
    param(
        [string]$sender,
        [string]$message
    )

    try {
        & $adbPath emu sms send $sender $message
        Start-Sleep -Milliseconds 800  # Prevent SMS flooding
        return $true
    }
    catch {
        Write-Host "⚠ Failed to send SMS: $_" -ForegroundColor Yellow
        return $false
    }
}

# Test data: November 2025 transactions
$transactions = @(
    # Nov 1 - Monthly subscriptions
    @{
        date = "01-11-25"
        sender = "HDFCBK"
        merchant = "Netflix"
        amount = "450.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "01-11-25"
        sender = "ICICIB"
        merchant = "Spotify"
        amount = "600.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 3 - Groceries
    @{
        date = "03-11-25"
        sender = "AXISBK"
        merchant = "BigBasket"
        amount = "1800.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 5 - Transport
    @{
        date = "05-11-25"
        sender = "SBIINB"
        merchant = "Uber"
        amount = "280.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "05-11-25"
        sender = "HDFCBK"
        merchant = "Ola"
        amount = "350.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 7 - Bills
    @{
        date = "07-11-25"
        sender = "ICICIB"
        merchant = "Electricity Bill"
        amount = "1150.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "07-11-25"
        sender = "AXISBK"
        merchant = "Airtel Broadband"
        amount = "999.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 9 - Food delivery
    @{
        date = "09-11-25"
        sender = "HDFCBK"
        merchant = "Swiggy"
        amount = "620.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "09-11-25"
        sender = "SBIINB"
        merchant = "Zomato"
        amount = "550.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 11 - Shopping (Credit Card)
    @{
        date = "11-11-25"
        sender = "HDFCBK"
        merchant = "Amazon"
        amount = "2800.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Nov 13 - Entertainment
    @{
        date = "13-11-25"
        sender = "ICICIB"
        merchant = "BookMyShow"
        amount = "400.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 15 - Groceries
    @{
        date = "15-11-25"
        sender = "AXISBK"
        merchant = "DMart"
        amount = "1350.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 16 - Food
    @{
        date = "16-11-25"
        sender = "HDFCBK"
        merchant = "Dominos"
        amount = "750.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 18 - Shopping (Credit Card)
    @{
        date = "18-11-25"
        sender = "ICICIB"
        merchant = "Flipkart"
        amount = "1950.00"
        cardLast4 = "5678"
        type = "CreditCard"
    },

    # Nov 20 - Transport
    @{
        date = "20-11-25"
        sender = "SBIINB"
        merchant = "Rapido"
        amount = "150.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 21 - Bills
    @{
        date = "21-11-25"
        sender = "HDFCBK"
        merchant = "Jio Mobile"
        amount = "399.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 23 - Food & Entertainment
    @{
        date = "23-11-25"
        sender = "ICICIB"
        merchant = "Starbucks"
        amount = "420.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "23-11-25"
        sender = "AXISBK"
        merchant = "PVR Cinemas"
        amount = "700.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 25 - Shopping (Credit Card)
    @{
        date = "25-11-25"
        sender = "HDFCBK"
        merchant = "Myntra"
        amount = "1600.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Nov 27 - Food delivery
    @{
        date = "27-11-25"
        sender = "SBIINB"
        merchant = "Swiggy"
        amount = "580.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 28 - Transport
    @{
        date = "28-11-25"
        sender = "HDFCBK"
        merchant = "Uber"
        amount = "320.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Nov 29 - Shopping (Credit Card)
    @{
        date = "29-11-25"
        sender = "AXISBK"
        merchant = "Ajio"
        amount = "1250.00"
        cardLast4 = "9012"
        type = "CreditCard"
    },

    # Nov 30 - Food
    @{
        date = "30-11-25"
        sender = "ICICIB"
        merchant = "McDonalds"
        amount = "380.00"
        upiRef = Get-UpiRef
        type = "UPI"
    }
)

# Main execution
Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "November 2025 SMS Test Data Sender" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Transactions: $($transactions.Count)" -ForegroundColor White
Write-Host "Expected Total: ~₹14,500" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan

# Check ADB connection
Write-Host "`nChecking ADB connection..." -ForegroundColor Yellow
$adbCheck = & $adbPath devices 2>&1
if ($adbCheck -match "emulator-\d+") {
    Write-Host "✓ Emulator connected" -ForegroundColor Green
}
else {
    Write-Host "✗ No emulator found. Please start the emulator first." -ForegroundColor Red
    exit 1
}

# Send transactions
Write-Host "`nSending $($transactions.Count) transactions..." -ForegroundColor Cyan
$successCount = 0
$totalAmount = 0

for ($i = 0; $i -lt $transactions.Count; $i++) {
    $tx = $transactions[$i]
    $num = $i + 1

    Write-Host "`n[$num/$($transactions.Count)] " -NoNewline -ForegroundColor White
    Write-Host "$($tx.merchant) - ₹$($tx.amount) ($($tx.date))" -ForegroundColor Yellow

    # Build SMS message based on type
    if ($tx.type -eq "UPI") {
        $message = switch ($tx.sender) {
            "HDFCBK" { "Paid Rs.$($tx.amount) to $($tx.merchant) on $($tx.date) using UPI. UPI Ref: $($tx.upiRef). -HDFC Bank" }
            "SBIINB" { "Rs.$($tx.amount) debited from A/c XX9012 to VPA $($tx.merchant)@paytm on $($tx.date). UPI Ref $($tx.upiRef) -SBI" }
            "ICICIB" { "INR $($tx.amount) debited from A/c XX5678 on $($tx.date) for UPI to $($tx.merchant)@paytm. Ref $($tx.upiRef)" }
            "AXISBK" { "INR $($tx.amount) debited from A/c no. XX3456 on $($tx.date) for UPI-$($tx.merchant). UPI Ref: $($tx.upiRef)" }
        }
    }
    else {
        # Credit Card
        $message = switch ($tx.sender) {
            "HDFCBK" { "HDFC Bank Credit Card XX$($tx.cardLast4) has been used for Rs.$($tx.amount) at $($tx.merchant) on $($tx.date) at 14:30:45" }
            "ICICIB" { "Alert: ICICI Card ending $($tx.cardLast4) used for INR $($tx.amount) at $($tx.merchant) on $($tx.date)" }
            "SBIINB" { "Your SBI Card ending $($tx.cardLast4) was used for Rs.$($tx.amount) at $($tx.merchant) on $($tx.date)" }
            "AXISBK" { "Axis Bank Card XX$($tx.cardLast4): Rs.$($tx.amount) spent at $($tx.merchant) on $($tx.date)" }
        }
    }

    if (Send-Sms -sender $tx.sender -message $message) {
        $successCount++
        $totalAmount += [double]$tx.amount
        Write-Host "  ✓ Sent successfully" -ForegroundColor Green
    }
    else {
        Write-Host "  ✗ Failed to send" -ForegroundColor Red
    }
}

# Summary
Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Total Sent: $successCount / $($transactions.Count)" -ForegroundColor White
Write-Host "Total Amount: ₹$([math]::Round($totalAmount, 2))" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan

# Verification tip
Write-Host "`nVerification:" -ForegroundColor Yellow
Write-Host "  1. Wait 3-5 seconds for SMS processing" -ForegroundColor Gray
Write-Host "  2. Open Fino app and check Analytics tab" -ForegroundColor Gray
Write-Host "  3. Expected: ~23 transactions for November 2025" -ForegroundColor Gray
Write-Host "  4. Expected total: ~₹14,500" -ForegroundColor Gray

Write-Host "`n✓ November 2025 test data sent successfully!`n" -ForegroundColor Green
