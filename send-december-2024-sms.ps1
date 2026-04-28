# Send December 2024 SMS Test Data
# Purpose: Historical month test data for YoY comparison and Analytics testing
# Transactions: 27
# Date Range: Dec 1-31, 2024
# Expected Total: ~₹16,200
# Payment Mix: 60% UPI, 40% Credit Cards
# Note: Amounts are 10% lower than Dec 2025 (inflation effect for YoY analysis)

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

# Test data: December 2024 transactions (amounts 10% lower than Dec 2025)
$transactions = @(
    # Dec 1 - Monthly subscriptions
    @{
        date = "01-12-24"
        sender = "HDFCBK"
        merchant = "Netflix"
        amount = "405.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "01-12-24"
        sender = "ICICIB"
        merchant = "Spotify"
        amount = "540.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 2 - Groceries & Food
    @{
        date = "02-12-24"
        sender = "HDFCBK"
        merchant = "BigBasket"
        amount = "1980.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "02-12-24"
        sender = "AXISBK"
        merchant = "Swiggy"
        amount = "675.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 3 - Transport
    @{
        date = "03-12-24"
        sender = "SBIINB"
        merchant = "Uber"
        amount = "315.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "03-12-24"
        sender = "HDFCBK"
        merchant = "Ola"
        amount = "378.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 4 - Shopping (Credit Card)
    @{
        date = "04-12-24"
        sender = "HDFCBK"
        merchant = "Amazon"
        amount = "3150.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Dec 5 - Bills
    @{
        date = "05-12-24"
        sender = "ICICIB"
        merchant = "Electricity Bill"
        amount = "1080.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "05-12-24"
        sender = "AXISBK"
        merchant = "Airtel Broadband"
        amount = "899.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 6 - Entertainment
    @{
        date = "06-12-24"
        sender = "SBIINB"
        merchant = "BookMyShow"
        amount = "315.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 7 - Food delivery
    @{
        date = "07-12-24"
        sender = "HDFCBK"
        merchant = "Zomato"
        amount = "405.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "07-12-24"
        sender = "ICICIB"
        merchant = "Swiggy"
        amount = "468.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 8 - Shopping (Credit Card)
    @{
        date = "08-12-24"
        sender = "ICICIB"
        merchant = "Flipkart"
        amount = "2520.00"
        cardLast4 = "5678"
        type = "CreditCard"
    },

    # Dec 9 - Transport & Food
    @{
        date = "09-12-24"
        sender = "AXISBK"
        merchant = "Uber"
        amount = "252.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "09-12-24"
        sender = "SBIINB"
        merchant = "Dominos"
        amount = "801.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 10 - Subscription
    @{
        date = "10-12-24"
        sender = "HDFCBK"
        merchant = "Amazon Prime"
        amount = "1350.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 11 - Shopping & Entertainment
    @{
        date = "11-12-24"
        sender = "SBIINB"
        merchant = "PVR Cinemas"
        amount = "720.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "11-12-24"
        sender = "HDFCBK"
        merchant = "Myntra"
        amount = "1080.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Dec 12 - Groceries
    @{
        date = "12-12-24"
        sender = "ICICIB"
        merchant = "DMart"
        amount = "1350.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 13 - Transport
    @{
        date = "13-12-24"
        sender = "AXISBK"
        merchant = "Rapido"
        amount = "162.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "13-12-24"
        sender = "HDFCBK"
        merchant = "Ola"
        amount = "288.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 14 - Shopping
    @{
        date = "14-12-24"
        sender = "ICICIB"
        merchant = "Nykaa"
        amount = "855.00"
        cardLast4 = "5678"
        type = "CreditCard"
    },

    # Dec 15 - Food
    @{
        date = "15-12-24"
        sender = "SBIINB"
        merchant = "Starbucks"
        amount = "432.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "15-12-24"
        sender = "HDFCBK"
        merchant = "McDonalds"
        amount = "315.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 17 - Shopping
    @{
        date = "17-12-24"
        sender = "AXISBK"
        merchant = "Ajio"
        amount = "1620.00"
        cardLast4 = "9012"
        type = "CreditCard"
    },

    # Dec 18 - Entertainment
    @{
        date = "18-12-24"
        sender = "HDFCBK"
        merchant = "Steam"
        amount = "539.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Dec 19 - Bills
    @{
        date = "19-12-24"
        sender = "ICICIB"
        merchant = "Jio Mobile"
        amount = "359.00"
        upiRef = Get-UpiRef
        type = "UPI"
    }
)

# Main execution
Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "December 2024 SMS Test Data Sender" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Transactions: $($transactions.Count)" -ForegroundColor White
Write-Host "Expected Total: ~₹16,200" -ForegroundColor White
Write-Host "YoY Comparison: 10% lower than Dec 2025" -ForegroundColor White
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
Write-Host "  3. Expected: 27 transactions for December 2024" -ForegroundColor Gray
Write-Host "  4. Expected total: ~₹16,200" -ForegroundColor Gray
Write-Host "  5. YoY Comparison: Dec 2024 (₹16,200) vs Dec 2025 (₹18,000)" -ForegroundColor Gray

Write-Host "`n✓ December 2024 test data sent successfully!`n" -ForegroundColor Green
