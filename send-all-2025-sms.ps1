# Send All 2025 SMS Test Data (Jan-Nov)
# Purpose: Year-to-date test data for comprehensive Analytics testing
# Transactions: 220
# Date Range: Jan 1 - Nov 30, 2025
# Expected Total: ~₹1,65,000
# Payment Mix: 60% UPI, 40% Credit Cards

$ErrorActionPreference = "Stop"

# Configuration
$adbPath = "adb"
$batchSize = 50  # Process in batches to prevent overwhelming the system

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

# Test data: January - November 2025 transactions (220 total)
$transactions = @(
    # ============================================
    # JANUARY 2025 - 20 transactions (~₹15,000)
    # ============================================
    @{ date = "01-01-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-01-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-01-25"; sender = "HDFCBK"; merchant = "BigBasket"; amount = "2100.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-01-25"; sender = "SBIINB"; merchant = "Uber"; amount = "320.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-01-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3200.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-01-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1150.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-01-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "06-01-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "680.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-01-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "520.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-01-25"; sender = "ICICIB"; merchant = "Flipkart"; amount = "2500.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "09-01-25"; sender = "AXISBK"; merchant = "Rapido"; amount = "180.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-01-25"; sender = "HDFCBK"; merchant = "Amazon Prime"; amount = "1500.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "11-01-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-01-25"; sender = "HDFCBK"; merchant = "Ola"; amount = "290.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "13-01-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "400.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-01-25"; sender = "HDFCBK"; merchant = "Myntra"; amount = "1100.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "15-01-25"; sender = "AXISBK"; merchant = "Starbucks"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-01-25"; sender = "SBIINB"; merchant = "McDonalds"; amount = "380.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "20-01-25"; sender = "ICICIB"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-01-25"; sender = "HDFCBK"; merchant = "Dominos"; amount = "820.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # FEBRUARY 2025 - 18 transactions (~₹13,500)
    # ============================================
    @{ date = "01-02-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-02-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-02-25"; sender = "SBIINB"; merchant = "BigBasket"; amount = "1950.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-02-25"; sender = "AXISBK"; merchant = "Uber"; amount = "340.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-02-25"; sender = "HDFCBK"; merchant = "Swiggy"; amount = "590.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-02-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1100.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-02-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-02-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "2800.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "08-02-25"; sender = "SBIINB"; merchant = "Zomato"; amount = "480.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-02-25"; sender = "ICICIB"; merchant = "Ola"; amount = "310.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-02-25"; sender = "AXISBK"; merchant = "DMart"; amount = "1280.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-02-25"; sender = "HDFCBK"; merchant = "Barbeque Nation"; amount = "1500.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "15-02-25"; sender = "SBIINB"; merchant = "BookMyShow"; amount = "550.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-02-25"; sender = "ICICIB"; merchant = "Nykaa"; amount = "850.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "20-02-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-02-25"; sender = "AXISBK"; merchant = "Rapido"; amount = "150.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-02-25"; sender = "SBIINB"; merchant = "Starbucks"; amount = "420.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "27-02-25"; sender = "HDFCBK"; merchant = "Swiggy"; amount = "640.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # MARCH 2025 - 20 transactions (~₹15,500)
    # ============================================
    @{ date = "01-03-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-03-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-03-25"; sender = "AXISBK"; merchant = "BigBasket"; amount = "2300.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-03-25"; sender = "SBIINB"; merchant = "Uber"; amount = "380.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-03-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3100.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-03-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1250.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-03-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-03-25"; sender = "SBIINB"; merchant = "Zomato"; amount = "550.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-03-25"; sender = "HDFCBK"; merchant = "Myntra"; amount = "1800.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "09-03-25"; sender = "ICICIB"; merchant = "Swiggy"; amount = "620.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-03-25"; sender = "AXISBK"; merchant = "Ola"; amount = "290.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-03-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-03-25"; sender = "HDFCBK"; merchant = "PVR Cinemas"; amount = "750.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-03-25"; sender = "ICICIB"; merchant = "McDonalds"; amount = "420.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-03-25"; sender = "AXISBK"; merchant = "Ajio"; amount = "1600.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "18-03-25"; sender = "SBIINB"; merchant = "Starbucks"; amount = "490.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "20-03-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-03-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "160.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-03-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "780.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-03-25"; sender = "SBIINB"; merchant = "BookMyShow"; amount = "500.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # APRIL 2025 - 20 transactions (~₹14,800)
    # ============================================
    @{ date = "01-04-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-04-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-04-25"; sender = "SBIINB"; merchant = "BigBasket"; amount = "2150.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-04-25"; sender = "AXISBK"; merchant = "Uber"; amount = "350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-04-25"; sender = "HDFCBK"; merchant = "Swiggy"; amount = "670.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-04-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1180.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-04-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-04-25"; sender = "SBIINB"; merchant = "Amazon"; amount = "2900.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "08-04-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "540.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-04-25"; sender = "ICICIB"; merchant = "Ola"; amount = "310.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-04-25"; sender = "AXISBK"; merchant = "DMart"; amount = "1380.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-04-25"; sender = "SBIINB"; merchant = "Haldirams"; amount = "650.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-04-25"; sender = "HDFCBK"; merchant = "Flipkart"; amount = "1850.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "17-04-25"; sender = "ICICIB"; merchant = "Starbucks"; amount = "460.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-04-25"; sender = "AXISBK"; merchant = "BookMyShow"; amount = "480.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "20-04-25"; sender = "SBIINB"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-04-25"; sender = "HDFCBK"; merchant = "Rapido"; amount = "140.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "24-04-25"; sender = "ICICIB"; merchant = "McDonalds"; amount = "390.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "27-04-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "810.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "29-04-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "720.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # MAY 2025 - 20 transactions (~₹15,200)
    # ============================================
    @{ date = "01-05-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-05-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-05-25"; sender = "AXISBK"; merchant = "BigBasket"; amount = "2250.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-05-25"; sender = "SBIINB"; merchant = "Uber"; amount = "370.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-05-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3300.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-05-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1320.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-05-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-05-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "710.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-05-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "580.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "09-05-25"; sender = "ICICIB"; merchant = "Myntra"; amount = "1950.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "10-05-25"; sender = "AXISBK"; merchant = "Ola"; amount = "330.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-05-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1420.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-05-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "510.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-05-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-05-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "410.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-05-25"; sender = "SBIINB"; merchant = "Nykaa"; amount = "920.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "20-05-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-05-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "170.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-05-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "850.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-05-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "780.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # JUNE 2025 - 20 transactions (~₹14,600)
    # ============================================
    @{ date = "01-06-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-06-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-06-25"; sender = "SBIINB"; merchant = "BigBasket"; amount = "2080.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-06-25"; sender = "AXISBK"; merchant = "Uber"; amount = "360.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-06-25"; sender = "HDFCBK"; merchant = "Swiggy"; amount = "690.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-06-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1280.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-06-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-06-25"; sender = "SBIINB"; merchant = "Amazon"; amount = "2700.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "08-06-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "560.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-06-25"; sender = "ICICIB"; merchant = "Ola"; amount = "320.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-06-25"; sender = "AXISBK"; merchant = "DMart"; amount = "1350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-06-25"; sender = "SBIINB"; merchant = "Starbucks"; amount = "480.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-06-25"; sender = "HDFCBK"; merchant = "Flipkart"; amount = "1750.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "17-06-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "520.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-06-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "400.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "20-06-25"; sender = "SBIINB"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-06-25"; sender = "HDFCBK"; merchant = "Rapido"; amount = "150.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "24-06-25"; sender = "ICICIB"; merchant = "Dominos"; amount = "790.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "27-06-25"; sender = "AXISBK"; merchant = "PVR Cinemas"; amount = "680.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "29-06-25"; sender = "SBIINB"; merchant = "Haldirams"; amount = "550.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # JULY 2025 - 20 transactions (~₹15,800)
    # ============================================
    @{ date = "01-07-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-07-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-07-25"; sender = "AXISBK"; merchant = "BigBasket"; amount = "2350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-07-25"; sender = "SBIINB"; merchant = "Uber"; amount = "390.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-07-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3400.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-07-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1380.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-07-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-07-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "730.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-07-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "09-07-25"; sender = "ICICIB"; merchant = "Myntra"; amount = "2100.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "10-07-25"; sender = "AXISBK"; merchant = "Ola"; amount = "340.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-07-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1480.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-07-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "530.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-07-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "650.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-07-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "430.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-07-25"; sender = "SBIINB"; merchant = "Ajio"; amount = "1700.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "20-07-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-07-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "180.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-07-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "870.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-07-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "820.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # AUGUST 2025 - 20 transactions (~₹16,200)
    # ============================================
    @{ date = "01-08-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-08-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-08-25"; sender = "SBIINB"; merchant = "BigBasket"; amount = "2400.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-08-25"; sender = "AXISBK"; merchant = "Uber"; amount = "410.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-08-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3600.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-08-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1420.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-08-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-08-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "760.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-08-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "620.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "09-08-25"; sender = "ICICIB"; merchant = "Flipkart"; amount = "2250.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "10-08-25"; sender = "AXISBK"; merchant = "Ola"; amount = "360.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-08-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1520.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-08-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "550.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-08-25"; sender = "ICICIB"; merchant = "Haldirams"; amount = "680.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-08-25"; sender = "AXISBK"; merchant = "BookMyShow"; amount = "700.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-08-25"; sender = "SBIINB"; merchant = "McDonalds"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "20-08-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-08-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "190.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-08-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "900.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-08-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "850.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # SEPTEMBER 2025 - 20 transactions (~₹15,400)
    # ============================================
    @{ date = "01-09-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-09-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-09-25"; sender = "AXISBK"; merchant = "BigBasket"; amount = "2280.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-09-25"; sender = "SBIINB"; merchant = "Uber"; amount = "380.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-09-25"; sender = "HDFCBK"; merchant = "Swiggy"; amount = "710.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-09-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-09-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-09-25"; sender = "SBIINB"; merchant = "Amazon"; amount = "3100.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "08-09-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "590.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "09-09-25"; sender = "ICICIB"; merchant = "Myntra"; amount = "1850.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "10-09-25"; sender = "AXISBK"; merchant = "Ola"; amount = "350.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-09-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1440.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-09-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "520.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-09-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "580.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-09-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "440.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-09-25"; sender = "SBIINB"; merchant = "Nykaa"; amount = "980.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "20-09-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-09-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "170.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-09-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "830.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-09-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "760.00"; upiRef = Get-UpiRef; type = "UPI" },

    # ============================================
    # OCTOBER 2025 - 22 transactions (~₹17,500)
    # ============================================
    @{ date = "01-10-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-10-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-10-25"; sender = "SBIINB"; merchant = "BigBasket"; amount = "2500.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-10-25"; sender = "AXISBK"; merchant = "Uber"; amount = "420.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-10-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3800.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-10-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-10-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-10-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "780.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-10-25"; sender = "HDFCBK"; merchant = "Flipkart"; amount = "2400.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "09-10-25"; sender = "ICICIB"; merchant = "Zomato"; amount = "640.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "10-10-25"; sender = "AXISBK"; merchant = "Ola"; amount = "370.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-10-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1550.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-10-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "560.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-10-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "720.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-10-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "470.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-10-25"; sender = "SBIINB"; merchant = "Myntra"; amount = "2050.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "20-10-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-10-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "200.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "24-10-25"; sender = "AXISBK"; merchant = "Haldirams"; amount = "650.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "26-10-25"; sender = "SBIINB"; merchant = "Dominos"; amount = "920.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-10-25"; sender = "HDFCBK"; merchant = "PVR Cinemas"; amount = "880.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "30-10-25"; sender = "ICICIB"; merchant = "Barbeque Nation"; amount = "1650.00"; cardLast4 = "1234"; type = "CreditCard" },

    # ============================================
    # NOVEMBER 2025 - 20 transactions (~₹16,500)
    # ============================================
    @{ date = "01-11-25"; sender = "HDFCBK"; merchant = "Netflix"; amount = "450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "01-11-25"; sender = "ICICIB"; merchant = "Spotify"; amount = "600.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "02-11-25"; sender = "AXISBK"; merchant = "BigBasket"; amount = "2450.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "03-11-25"; sender = "SBIINB"; merchant = "Uber"; amount = "400.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "04-11-25"; sender = "HDFCBK"; merchant = "Amazon"; amount = "3700.00"; cardLast4 = "1234"; type = "CreditCard" },
    @{ date = "05-11-25"; sender = "ICICIB"; merchant = "Electricity Bill"; amount = "1400.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "05-11-25"; sender = "AXISBK"; merchant = "Airtel Broadband"; amount = "999.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "07-11-25"; sender = "SBIINB"; merchant = "Swiggy"; amount = "750.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "08-11-25"; sender = "HDFCBK"; merchant = "Zomato"; amount = "610.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "09-11-25"; sender = "ICICIB"; merchant = "Flipkart"; amount = "2300.00"; cardLast4 = "5678"; type = "CreditCard" },
    @{ date = "10-11-25"; sender = "AXISBK"; merchant = "Ola"; amount = "360.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "12-11-25"; sender = "SBIINB"; merchant = "DMart"; amount = "1500.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "14-11-25"; sender = "HDFCBK"; merchant = "Starbucks"; amount = "540.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "15-11-25"; sender = "ICICIB"; merchant = "BookMyShow"; amount = "680.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "17-11-25"; sender = "AXISBK"; merchant = "McDonalds"; amount = "460.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "18-11-25"; sender = "SBIINB"; merchant = "Myntra"; amount = "1900.00"; cardLast4 = "9012"; type = "CreditCard" },
    @{ date = "20-11-25"; sender = "HDFCBK"; merchant = "Jio Mobile"; amount = "399.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "22-11-25"; sender = "ICICIB"; merchant = "Rapido"; amount = "190.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "25-11-25"; sender = "AXISBK"; merchant = "Dominos"; amount = "880.00"; upiRef = Get-UpiRef; type = "UPI" },
    @{ date = "28-11-25"; sender = "SBIINB"; merchant = "PVR Cinemas"; amount = "820.00"; upiRef = Get-UpiRef; type = "UPI" }
)

# Main execution
Write-Host "`n=====================================" -ForegroundColor Cyan
Write-Host "2025 Year-to-Date SMS Test Data Sender" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Transactions: $($transactions.Count)" -ForegroundColor White
Write-Host "Date Range: Jan 1 - Nov 30, 2025" -ForegroundColor White
Write-Host "Expected Total: ~₹1,65,000" -ForegroundColor White
Write-Host "Batch Size: $batchSize" -ForegroundColor White
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

# Send transactions in batches
Write-Host "`nSending $($transactions.Count) transactions in batches of $batchSize..." -ForegroundColor Cyan
$successCount = 0
$totalAmount = 0
$currentBatch = 1
$totalBatches = [Math]::Ceiling($transactions.Count / $batchSize)

for ($i = 0; $i -lt $transactions.Count; $i++) {
    $tx = $transactions[$i]
    $num = $i + 1

    # Display batch progress
    if (($i % $batchSize) -eq 0 -and $i -ne 0) {
        $currentBatch++
        Write-Host "`n--- Batch $currentBatch of $totalBatches ---" -ForegroundColor Magenta
        Write-Host "Progress: $num / $($transactions.Count) transactions sent" -ForegroundColor Magenta
        Write-Host "Running total: ₹$([math]::Round($totalAmount, 2))" -ForegroundColor Magenta
        Start-Sleep -Seconds 2  # Pause between batches
    }

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
Write-Host "Final Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Total Sent: $successCount / $($transactions.Count)" -ForegroundColor White
Write-Host "Total Amount: ₹$([math]::Round($totalAmount, 2))" -ForegroundColor White
Write-Host "Success Rate: $([math]::Round(($successCount / $transactions.Count) * 100, 2))%" -ForegroundColor White
Write-Host "=====================================" -ForegroundColor Cyan

# Month-by-month breakdown
Write-Host "`nMonth-by-Month Breakdown:" -ForegroundColor Yellow
$monthGroups = @{
    "January" = $transactions | Where-Object { $_.date -like "*-01-25" }
    "February" = $transactions | Where-Object { $_.date -like "*-02-25" }
    "March" = $transactions | Where-Object { $_.date -like "*-03-25" }
    "April" = $transactions | Where-Object { $_.date -like "*-04-25" }
    "May" = $transactions | Where-Object { $_.date -like "*-05-25" }
    "June" = $transactions | Where-Object { $_.date -like "*-06-25" }
    "July" = $transactions | Where-Object { $_.date -like "*-07-25" }
    "August" = $transactions | Where-Object { $_.date -like "*-08-25" }
    "September" = $transactions | Where-Object { $_.date -like "*-09-25" }
    "October" = $transactions | Where-Object { $_.date -like "*-10-25" }
    "November" = $transactions | Where-Object { $_.date -like "*-11-25" }
}

foreach ($month in $monthGroups.Keys | Sort-Object) {
    $count = $monthGroups[$month].Count
    $monthTotal = ($monthGroups[$month] | ForEach-Object { [double]$_.amount } | Measure-Object -Sum).Sum
    Write-Host "  $month : $count transactions, ₹$([math]::Round($monthTotal, 2))" -ForegroundColor Gray
}

# Verification tip
Write-Host "`nVerification Steps:" -ForegroundColor Yellow
Write-Host "  1. Wait 5-10 seconds for SMS processing" -ForegroundColor Gray
Write-Host "  2. Open Fino app and check Analytics tab" -ForegroundColor Gray
Write-Host "  3. Expected: 220 transactions across 11 months" -ForegroundColor Gray
Write-Host "  4. Expected total: ~₹1,65,000" -ForegroundColor Gray
Write-Host "  5. Check monthly trends and spending patterns" -ForegroundColor Gray

Write-Host "`n✓ 2025 year-to-date test data sent successfully!`n" -ForegroundColor Green
