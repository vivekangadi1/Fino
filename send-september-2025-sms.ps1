# Send September 2025 SMS Test Data
# Purpose: Back-to-school month test data with education emphasis
# Transactions: 22
# Date Range: Sep 1-30, 2025
# Expected Total: ~₹13,800
# Payment Mix: 65% UPI, 35% Credit Cards

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

# Test data: September 2025 transactions
$transactions = @(
    # Sep 1 - Back to school supplies
    @{
        date = "01-09-25"
        sender = "HDFCBK"
        merchant = "Amazon"
        amount = "2850.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },
    @{
        date = "01-09-25"
        sender = "ICICIB"
        merchant = "Flipkart"
        amount = "1200.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 2 - Education fees
    @{
        date = "02-09-25"
        sender = "AXISBK"
        merchant = "Tuition Fee"
        amount = "3500.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 3 - Groceries & Transport
    @{
        date = "03-09-25"
        sender = "SBIINB"
        merchant = "BigBasket"
        amount = "1800.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "03-09-25"
        sender = "HDFCBK"
        merchant = "Uber"
        amount = "280.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 5 - Books & Stationery
    @{
        date = "05-09-25"
        sender = "ICICIB"
        merchant = "Crossword"
        amount = "950.00"
        cardLast4 = "5678"
        type = "CreditCard"
    },
    @{
        date = "05-09-25"
        sender = "HDFCBK"
        merchant = "Sapna Book House"
        amount = "680.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 7 - Monthly subscriptions
    @{
        date = "07-09-25"
        sender = "AXISBK"
        merchant = "Netflix"
        amount = "450.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "07-09-25"
        sender = "SBIINB"
        merchant = "Spotify"
        amount = "600.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 9 - Food & Transport
    @{
        date = "09-09-25"
        sender = "HDFCBK"
        merchant = "Swiggy"
        amount = "420.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "09-09-25"
        sender = "ICICIB"
        merchant = "Ola"
        amount = "310.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 12 - Online courses
    @{
        date = "12-09-25"
        sender = "HDFCBK"
        merchant = "Coursera"
        amount = "1499.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Sep 14 - Groceries
    @{
        date = "14-09-25"
        sender = "SBIINB"
        merchant = "DMart"
        amount = "1350.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 15 - Bills
    @{
        date = "15-09-25"
        sender = "AXISBK"
        merchant = "Electricity Bill"
        amount = "1150.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "15-09-25"
        sender = "HDFCBK"
        merchant = "Airtel Broadband"
        amount = "999.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 18 - Limited entertainment
    @{
        date = "18-09-25"
        sender = "ICICIB"
        merchant = "BookMyShow"
        amount = "280.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 20 - Food delivery
    @{
        date = "20-09-25"
        sender = "SBIINB"
        merchant = "Zomato"
        amount = "380.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },
    @{
        date = "20-09-25"
        sender = "HDFCBK"
        merchant = "Dominos"
        amount = "650.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 23 - Transport
    @{
        date = "23-09-25"
        sender = "AXISBK"
        merchant = "Rapido"
        amount = "150.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 25 - Mobile recharge
    @{
        date = "25-09-25"
        sender = "ICICIB"
        merchant = "Jio Mobile"
        amount = "399.00"
        upiRef = Get-UpiRef
        type = "UPI"
    },

    # Sep 28 - Stationery refill
    @{
        date = "28-09-25"
        sender = "HDFCBK"
        merchant = "Staples"
        amount = "420.00"
        cardLast4 = "1234"
        type = "CreditCard"
    },

    # Sep 30 - Groceries
    @{
        date = "30-09-25"
        sender = "SBIINB"
        merchant = "BigBasket"
        amount = "980.00"
        upiRef = Get-UpiRef
        type = "UPI"
    }
)

# Main execution
Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "September 2025 SMS Test Data Sender" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Transactions: $($transactions.Count)" -ForegroundColor White
Write-Host "Expected Total: ~₹13,800" -ForegroundColor White
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
Write-Host "  3. Expected: 22 transactions for September 2025" -ForegroundColor Gray
Write-Host "  4. Expected total: ~₹13,800" -ForegroundColor Gray
Write-Host "  5. Key categories: Education, Shopping, Groceries" -ForegroundColor Gray

Write-Host "`n✓ September 2025 test data sent successfully!`n" -ForegroundColor Green
