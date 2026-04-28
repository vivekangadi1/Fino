# Send Full Year 2024 SMS Test Data
# Purpose: Complete year historical data for analytics testing
# Transactions: 240
# Date Range: Jan 1 - Dec 31, 2024
# Expected Annual Total: ~₹1,80,000
# Payment Mix: 60% UPI, 40% Credit Cards
# Seasonal Distribution:
#   Jan-Feb: 33 tx (15-18/month, post-holiday recovery)
#   Mar-Apr: 38 tx (18-20/month, medium spending)
#   May-Aug: 85 tx (20-22/month, medium-high summer)
#   Sep-Oct: 47 tx (22-25/month, high festive season)
#   Nov-Dec: 53 tx (25-28/month, highest holiday shopping)

$ErrorActionPreference = "Stop"

# Configuration
$adbPath = "adb"
$batchSize = 20  # Send in batches to avoid flooding
$batchDelay = 5  # Seconds between batches

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

# Test data: Full Year 2024 transactions
$transactions = @(
    # ==================== JANUARY 2024 (15 transactions, ₹12,500) ====================
    # Post-holiday recovery, lower spending

    @{date="01-01-24"; sender="HDFCBK"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-01-24"; sender="ICICIB"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-01-24"; sender="AXISBK"; merchant="BigBasket"; amount="1800.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-01-24"; sender="SBIINB"; merchant="Electricity Bill"; amount="1100.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-01-24"; sender="HDFCBK"; merchant="Swiggy"; amount="550.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-01-24"; sender="ICICIB"; merchant="Uber"; amount="280.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-01-24"; sender="HDFCBK"; merchant="Amazon"; amount="2200.00"; cardLast4="1234"; type="CreditCard"},
    @{date="13-01-24"; sender="AXISBK"; merchant="DMart"; amount="1300.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-01-24"; sender="SBIINB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-01-24"; sender="ICICIB"; merchant="Zomato"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-01-24"; sender="HDFCBK"; merchant="BookMyShow"; amount="300.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="22-01-24"; sender="AXISBK"; merchant="Ola"; amount="350.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-01-24"; sender="SBIINB"; merchant="Dominos"; amount="680.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-01-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-01-24"; sender="HDFCBK"; merchant="Flipkart"; amount="1072.00"; cardLast4="1234"; type="CreditCard"},

    # ==================== FEBRUARY 2024 (18 transactions, ₹14,800) ====================
    # Still recovering, gradual increase

    @{date="01-02-24"; sender="ICICIB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-02-24"; sender="AXISBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-02-24"; sender="SBIINB"; merchant="More Supermarket"; amount="1500.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-02-24"; sender="HDFCBK"; merchant="Swiggy"; amount="620.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-02-24"; sender="ICICIB"; merchant="Uber"; amount="320.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-02-24"; sender="AXISBK"; merchant="Amazon"; amount="1800.00"; cardLast4="9012"; type="CreditCard"},
    @{date="11-02-24"; sender="SBIINB"; merchant="BigBasket"; amount="1900.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="13-02-24"; sender="HDFCBK"; merchant="Electricity Bill"; amount="1150.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="14-02-24"; sender="ICICIB"; merchant="Ferns N Petals"; amount="800.00"; cardLast4="5678"; type="CreditCard"},
    @{date="15-02-24"; sender="AXISBK"; merchant="Zomato"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-02-24"; sender="SBIINB"; merchant="PVR Cinemas"; amount="700.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-02-24"; sender="HDFCBK"; merchant="Myntra"; amount="1500.00"; cardLast4="1234"; type="CreditCard"},
    @{date="21-02-24"; sender="ICICIB"; merchant="Ola"; amount="280.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="23-02-24"; sender="AXISBK"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-02-24"; sender="SBIINB"; merchant="McDonald's"; amount="380.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-02-24"; sender="HDFCBK"; merchant="DMart"; amount="1400.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-02-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-02-24"; sender="AXISBK"; merchant="Swiggy"; amount="502.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== MARCH 2024 (19 transactions, ₹16,200) ====================
    # Spring, moderate spending

    @{date="01-03-24"; sender="SBIINB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-03-24"; sender="HDFCBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-03-24"; sender="ICICIB"; merchant="BigBasket"; amount="2100.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-03-24"; sender="AXISBK"; merchant="Electricity Bill"; amount="1250.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-03-24"; sender="SBIINB"; merchant="Swiggy"; amount="680.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-03-24"; sender="HDFCBK"; merchant="Uber"; amount="390.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-03-24"; sender="ICICIB"; merchant="Amazon"; amount="2500.00"; cardLast4="5678"; type="CreditCard"},
    @{date="13-03-24"; sender="AXISBK"; merchant="Reliance Fresh"; amount="1600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-03-24"; sender="SBIINB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-03-24"; sender="HDFCBK"; merchant="Zomato"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-03-24"; sender="ICICIB"; merchant="BookMyShow"; amount="350.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-03-24"; sender="AXISBK"; merchant="Myntra"; amount="1350.00"; cardLast4="9012"; type="CreditCard"},
    @{date="23-03-24"; sender="SBIINB"; merchant="Ola"; amount="310.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-03-24"; sender="HDFCBK"; merchant="Starbucks"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-03-24"; sender="ICICIB"; merchant="DMart"; amount="1450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-03-24"; sender="AXISBK"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-03-24"; sender="SBIINB"; merchant="Dominos"; amount="750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-03-24"; sender="HDFCBK"; merchant="Rapido"; amount="72.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-03-24"; sender="ICICIB"; merchant="Swiggy"; amount="610.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== APRIL 2024 (19 transactions, ₹15,800) ====================
    # Spring continues

    @{date="01-04-24"; sender="AXISBK"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-04-24"; sender="SBIINB"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-04-24"; sender="HDFCBK"; merchant="BigBasket"; amount="1950.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-04-24"; sender="ICICIB"; merchant="Electricity Bill"; amount="1200.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-04-24"; sender="AXISBK"; merchant="Swiggy"; amount="580.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-04-24"; sender="SBIINB"; merchant="Uber"; amount="340.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-04-24"; sender="HDFCBK"; merchant="Amazon"; amount="1900.00"; cardLast4="1234"; type="CreditCard"},
    @{date="13-04-24"; sender="ICICIB"; merchant="More Supermarket"; amount="1700.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-04-24"; sender="AXISBK"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-04-24"; sender="SBIINB"; merchant="Zomato"; amount="490.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-04-24"; sender="HDFCBK"; merchant="BookMyShow"; amount="400.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-04-24"; sender="ICICIB"; merchant="Flipkart"; amount="1650.00"; cardLast4="5678"; type="CreditCard"},
    @{date="23-04-24"; sender="AXISBK"; merchant="Ola"; amount="280.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-04-24"; sender="SBIINB"; merchant="KFC"; amount="550.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-04-24"; sender="HDFCBK"; merchant="DMart"; amount="1500.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-04-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-04-24"; sender="AXISBK"; merchant="Subway"; amount="380.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-04-24"; sender="SBIINB"; merchant="Rapido"; amount="82.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-04-24"; sender="HDFCBK"; merchant="Swiggy"; amount="550.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== MAY 2024 (21 transactions, ₹17,500) ====================
    # Summer begins, increased spending

    @{date="01-05-24"; sender="ICICIB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-05-24"; sender="AXISBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-05-24"; sender="SBIINB"; merchant="BigBasket"; amount="2200.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-05-24"; sender="HDFCBK"; merchant="Electricity Bill"; amount="1350.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-05-24"; sender="ICICIB"; merchant="Swiggy"; amount="650.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-05-24"; sender="AXISBK"; merchant="Uber"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-05-24"; sender="SBIINB"; merchant="Amazon"; amount="2800.00"; cardLast4="7890"; type="CreditCard"},
    @{date="13-05-24"; sender="HDFCBK"; merchant="Reliance Fresh"; amount="1850.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-05-24"; sender="ICICIB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-05-24"; sender="AXISBK"; merchant="Zomato"; amount="580.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-05-24"; sender="SBIINB"; merchant="PVR Cinemas"; amount="750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-05-24"; sender="HDFCBK"; merchant="Myntra"; amount="1600.00"; cardLast4="1234"; type="CreditCard"},
    @{date="23-05-24"; sender="ICICIB"; merchant="Ola"; amount="360.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-05-24"; sender="AXISBK"; merchant="McDonald's"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-05-24"; sender="SBIINB"; merchant="DMart"; amount="1600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-05-24"; sender="HDFCBK"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-05-24"; sender="ICICIB"; merchant="Dominos"; amount="820.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-05-24"; sender="AXISBK"; merchant="Rapido"; amount="92.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-05-24"; sender="SBIINB"; merchant="Swiggy"; amount="610.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-05-24"; sender="HDFCBK"; merchant="BookMyShow"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-05-24"; sender="ICICIB"; merchant="Starbucks"; amount="500.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== JUNE 2024 (21 transactions, ₹16,900) ====================
    # Summer peak

    @{date="01-06-24"; sender="AXISBK"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-06-24"; sender="SBIINB"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-06-24"; sender="HDFCBK"; merchant="BigBasket"; amount="2100.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-06-24"; sender="ICICIB"; merchant="Electricity Bill"; amount="1400.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-06-24"; sender="AXISBK"; merchant="Swiggy"; amount="690.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-06-24"; sender="SBIINB"; merchant="Uber"; amount="380.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-06-24"; sender="HDFCBK"; merchant="Amazon"; amount="2400.00"; cardLast4="1234"; type="CreditCard"},
    @{date="13-06-24"; sender="ICICIB"; merchant="More Supermarket"; amount="1750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-06-24"; sender="AXISBK"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-06-24"; sender="SBIINB"; merchant="Zomato"; amount="540.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-06-24"; sender="HDFCBK"; merchant="BookMyShow"; amount="380.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-06-24"; sender="ICICIB"; merchant="Flipkart"; amount="1800.00"; cardLast4="5678"; type="CreditCard"},
    @{date="23-06-24"; sender="AXISBK"; merchant="Ola"; amount="320.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-06-24"; sender="SBIINB"; merchant="Pizza Hut"; amount="680.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-06-24"; sender="HDFCBK"; merchant="DMart"; amount="1550.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-06-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-06-24"; sender="AXISBK"; merchant="Burger King"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-06-24"; sender="SBIINB"; merchant="Rapido"; amount="102.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-06-24"; sender="HDFCBK"; merchant="Swiggy"; amount="580.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-06-24"; sender="ICICIB"; merchant="Cafe Coffee Day"; amount="360.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-06-24"; sender="AXISBK"; merchant="Amazon Prime"; amount="1500.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== JULY 2024 (22 transactions, ₹18,200) ====================
    # Summer high spending

    @{date="01-07-24"; sender="SBIINB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-07-24"; sender="HDFCBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-07-24"; sender="ICICIB"; merchant="BigBasket"; amount="2300.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-07-24"; sender="AXISBK"; merchant="Electricity Bill"; amount="1450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-07-24"; sender="SBIINB"; merchant="Swiggy"; amount="720.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-07-24"; sender="HDFCBK"; merchant="Uber"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-07-24"; sender="ICICIB"; merchant="Amazon"; amount="3200.00"; cardLast4="5678"; type="CreditCard"},
    @{date="13-07-24"; sender="AXISBK"; merchant="Reliance Fresh"; amount="1900.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-07-24"; sender="SBIINB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-07-24"; sender="HDFCBK"; merchant="Zomato"; amount="620.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-07-24"; sender="ICICIB"; merchant="PVR Cinemas"; amount="850.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-07-24"; sender="AXISBK"; merchant="Myntra"; amount="1750.00"; cardLast4="9012"; type="CreditCard"},
    @{date="23-07-24"; sender="SBIINB"; merchant="Ola"; amount="390.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-07-24"; sender="HDFCBK"; merchant="Starbucks"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-07-24"; sender="ICICIB"; merchant="DMart"; amount="1650.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-07-24"; sender="AXISBK"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-07-24"; sender="SBIINB"; merchant="Dominos"; amount="880.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-07-24"; sender="HDFCBK"; merchant="Rapido"; amount="112.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-07-24"; sender="ICICIB"; merchant="Swiggy"; amount="640.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-07-24"; sender="AXISBK"; merchant="BookMyShow"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-07-24"; sender="SBIINB"; merchant="McDonald's"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-07-24"; sender="HDFCBK"; merchant="Steam"; amount="650.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== AUGUST 2024 (21 transactions, ₹17,600) ====================
    # Monsoon, continued high spending

    @{date="01-08-24"; sender="ICICIB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-08-24"; sender="AXISBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-08-24"; sender="SBIINB"; merchant="BigBasket"; amount="2150.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-08-24"; sender="HDFCBK"; merchant="Electricity Bill"; amount="1500.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-08-24"; sender="ICICIB"; merchant="Swiggy"; amount="680.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-08-24"; sender="AXISBK"; merchant="Uber"; amount="410.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-08-24"; sender="SBIINB"; merchant="Amazon"; amount="2600.00"; cardLast4="7890"; type="CreditCard"},
    @{date="13-08-24"; sender="HDFCBK"; merchant="More Supermarket"; amount="1800.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-08-24"; sender="ICICIB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-08-24"; sender="AXISBK"; merchant="Zomato"; amount="590.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-08-24"; sender="SBIINB"; merchant="BookMyShow"; amount="400.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-08-24"; sender="HDFCBK"; merchant="Flipkart"; amount="2100.00"; cardLast4="1234"; type="CreditCard"},
    @{date="23-08-24"; sender="ICICIB"; merchant="Ola"; amount="350.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-08-24"; sender="AXISBK"; merchant="KFC"; amount="620.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-08-24"; sender="SBIINB"; merchant="DMart"; amount="1700.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-08-24"; sender="HDFCBK"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-08-24"; sender="ICICIB"; merchant="Subway"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-08-24"; sender="AXISBK"; merchant="Rapido"; amount="122.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-08-24"; sender="SBIINB"; merchant="Swiggy"; amount="660.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-08-24"; sender="HDFCBK"; merchant="Cafe Coffee Day"; amount="380.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-08-24"; sender="ICICIB"; merchant="Starbucks"; amount="570.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== SEPTEMBER 2024 (23 transactions, ₹19,500) ====================
    # Festive season begins, high spending

    @{date="01-09-24"; sender="AXISBK"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-09-24"; sender="SBIINB"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-09-24"; sender="HDFCBK"; merchant="BigBasket"; amount="2400.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-09-24"; sender="ICICIB"; merchant="Electricity Bill"; amount="1550.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-09-24"; sender="AXISBK"; merchant="Swiggy"; amount="750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-09-24"; sender="SBIINB"; merchant="Uber"; amount="480.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-09-24"; sender="HDFCBK"; merchant="Amazon"; amount="3500.00"; cardLast4="1234"; type="CreditCard"},
    @{date="13-09-24"; sender="ICICIB"; merchant="Reliance Fresh"; amount="2000.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-09-24"; sender="AXISBK"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-09-24"; sender="SBIINB"; merchant="Zomato"; amount="680.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-09-24"; sender="HDFCBK"; merchant="PVR Cinemas"; amount="900.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-09-24"; sender="ICICIB"; merchant="Myntra"; amount="2200.00"; cardLast4="5678"; type="CreditCard"},
    @{date="23-09-24"; sender="AXISBK"; merchant="Ola"; amount="420.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-09-24"; sender="SBIINB"; merchant="McDonald's"; amount="480.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-09-24"; sender="HDFCBK"; merchant="DMart"; amount="1800.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-09-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-09-24"; sender="AXISBK"; merchant="Dominos"; amount="920.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-09-24"; sender="SBIINB"; merchant="Rapido"; amount="132.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-09-24"; sender="HDFCBK"; merchant="Swiggy"; amount="720.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-09-24"; sender="ICICIB"; merchant="BookMyShow"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-09-24"; sender="AXISBK"; merchant="Starbucks"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-09-24"; sender="SBIINB"; merchant="Ajio"; amount="1850.00"; cardLast4="7890"; type="CreditCard"},
    @{date="30-09-24"; sender="HDFCBK"; merchant="Nykaa"; amount="500.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== OCTOBER 2024 (24 transactions, ₹21,000) ====================
    # Festive peak (Diwali season)

    @{date="01-10-24"; sender="ICICIB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-10-24"; sender="AXISBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-10-24"; sender="SBIINB"; merchant="BigBasket"; amount="2600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-10-24"; sender="HDFCBK"; merchant="Electricity Bill"; amount="1600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-10-24"; sender="ICICIB"; merchant="Swiggy"; amount="820.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-10-24"; sender="AXISBK"; merchant="Uber"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-10-24"; sender="SBIINB"; merchant="Amazon"; amount="4200.00"; cardLast4="7890"; type="CreditCard"},
    @{date="13-10-24"; sender="HDFCBK"; merchant="More Supermarket"; amount="2100.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-10-24"; sender="ICICIB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-10-24"; sender="AXISBK"; merchant="Zomato"; amount="720.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-10-24"; sender="SBIINB"; merchant="BookMyShow"; amount="500.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-10-24"; sender="HDFCBK"; merchant="Flipkart"; amount="2800.00"; cardLast4="1234"; type="CreditCard"},
    @{date="23-10-24"; sender="ICICIB"; merchant="Myntra"; amount="1900.00"; cardLast4="5678"; type="CreditCard"},
    @{date="25-10-24"; sender="AXISBK"; merchant="Ola"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-10-24"; sender="SBIINB"; merchant="Pizza Hut"; amount="780.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-10-24"; sender="HDFCBK"; merchant="DMart"; amount="1900.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-10-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-10-24"; sender="AXISBK"; merchant="Dominos"; amount="950.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-10-24"; sender="SBIINB"; merchant="Rapido"; amount="142.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-10-24"; sender="HDFCBK"; merchant="Swiggy"; amount="780.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-10-24"; sender="ICICIB"; merchant="Starbucks"; amount="590.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-10-24"; sender="AXISBK"; merchant="Haldirams"; amount="620.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-10-24"; sender="SBIINB"; merchant="Ajio"; amount="1600.00"; cardLast4="7890"; type="CreditCard"},
    @{date="31-10-24"; sender="HDFCBK"; merchant="Tanishq"; amount="500.00"; upiRef=Get-UpiRef; type="UPI"},

    # ==================== NOVEMBER 2024 (25 transactions, ₹22,800) ====================
    # Peak shopping season

    @{date="01-11-24"; sender="ICICIB"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-11-24"; sender="AXISBK"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-11-24"; sender="SBIINB"; merchant="BigBasket"; amount="2700.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-11-24"; sender="HDFCBK"; merchant="Electricity Bill"; amount="1650.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="07-11-24"; sender="ICICIB"; merchant="Swiggy"; amount="880.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="09-11-24"; sender="AXISBK"; merchant="Uber"; amount="560.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="11-11-24"; sender="SBIINB"; merchant="Amazon"; amount="4800.00"; cardLast4="7890"; type="CreditCard"},
    @{date="13-11-24"; sender="HDFCBK"; merchant="Reliance Fresh"; amount="2200.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="15-11-24"; sender="ICICIB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-11-24"; sender="AXISBK"; merchant="Zomato"; amount="760.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="19-11-24"; sender="SBIINB"; merchant="PVR Cinemas"; amount="950.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-11-24"; sender="HDFCBK"; merchant="Flipkart"; amount="3200.00"; cardLast4="1234"; type="CreditCard"},
    @{date="23-11-24"; sender="ICICIB"; merchant="Myntra"; amount="2100.00"; cardLast4="5678"; type="CreditCard"},
    @{date="25-11-24"; sender="AXISBK"; merchant="Ola"; amount="490.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-11-24"; sender="SBIINB"; merchant="McDonald's"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-11-24"; sender="HDFCBK"; merchant="DMart"; amount="2000.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="28-11-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-11-24"; sender="AXISBK"; merchant="Dominos"; amount="980.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="SBIINB"; merchant="Rapido"; amount="152.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="HDFCBK"; merchant="Swiggy"; amount="820.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="ICICIB"; merchant="BookMyShow"; amount="480.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="AXISBK"; merchant="Starbucks"; amount="610.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="SBIINB"; merchant="Ajio"; amount="1700.00"; cardLast4="7890"; type="CreditCard"},
    @{date="30-11-24"; sender="HDFCBK"; merchant="Nykaa"; amount="750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-11-24"; sender="ICICIB"; merchant="Westside"; amount="1248.00"; cardLast4="5678"; type="CreditCard"},

    # ==================== DECEMBER 2024 (28 transactions, ₹24,500) ====================
    # Holiday season peak

    @{date="01-12-24"; sender="AXISBK"; merchant="Netflix"; amount="450.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="01-12-24"; sender="SBIINB"; merchant="Spotify"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="02-12-24"; sender="HDFCBK"; merchant="BigBasket"; amount="2900.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="03-12-24"; sender="ICICIB"; merchant="Swiggy"; amount="920.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="04-12-24"; sender="AXISBK"; merchant="Uber"; amount="600.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="05-12-24"; sender="SBIINB"; merchant="Electricity Bill"; amount="1700.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="06-12-24"; sender="HDFCBK"; merchant="Amazon"; amount="5200.00"; cardLast4="1234"; type="CreditCard"},
    @{date="08-12-24"; sender="ICICIB"; merchant="More Supermarket"; amount="2300.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="10-12-24"; sender="AXISBK"; merchant="Zomato"; amount="820.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="12-12-24"; sender="SBIINB"; merchant="BookMyShow"; amount="550.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="14-12-24"; sender="HDFCBK"; merchant="Flipkart"; amount="3600.00"; cardLast4="1234"; type="CreditCard"},
    @{date="15-12-24"; sender="ICICIB"; merchant="Airtel Broadband"; amount="999.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="16-12-24"; sender="AXISBK"; merchant="Ola"; amount="520.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="17-12-24"; sender="SBIINB"; merchant="PVR Cinemas"; amount="1000.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="18-12-24"; sender="HDFCBK"; merchant="Myntra"; amount="2400.00"; cardLast4="1234"; type="CreditCard"},
    @{date="19-12-24"; sender="ICICIB"; merchant="DMart"; amount="2100.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="20-12-24"; sender="AXISBK"; merchant="Dominos"; amount="1050.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="21-12-24"; sender="SBIINB"; merchant="Swiggy"; amount="880.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="22-12-24"; sender="HDFCBK"; merchant="Starbucks"; amount="650.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="23-12-24"; sender="ICICIB"; merchant="Jio Mobile"; amount="399.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="24-12-24"; sender="AXISBK"; merchant="KFC"; amount="720.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="25-12-24"; sender="SBIINB"; merchant="Amazon Prime"; amount="1500.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="26-12-24"; sender="HDFCBK"; merchant="Rapido"; amount="172.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="27-12-24"; sender="ICICIB"; merchant="Ajio"; amount="1950.00"; cardLast4="5678"; type="CreditCard"},
    @{date="28-12-24"; sender="AXISBK"; merchant="Nykaa"; amount="880.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="29-12-24"; sender="SBIINB"; merchant="Haldirams"; amount="750.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="30-12-24"; sender="HDFCBK"; merchant="Steam"; amount="699.00"; upiRef=Get-UpiRef; type="UPI"},
    @{date="31-12-24"; sender="ICICIB"; merchant="Zomato"; amount="890.00"; upiRef=Get-UpiRef; type="UPI"}
)

# Main execution
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Full Year 2024 SMS Test Data Sender" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Transactions: $($transactions.Count)" -ForegroundColor White
Write-Host "Date Range: Jan 1 - Dec 31, 2024" -ForegroundColor White
Write-Host "Expected Annual Total: ~₹1,80,000" -ForegroundColor White
Write-Host "Payment Mix: 60% UPI, 40% Credit Cards" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan

# Check ADB connection
Write-Host "`nChecking ADB connection..." -ForegroundColor Yellow
try {
    $adbCheck = & $adbPath devices 2>&1
    if ($adbCheck -match "emulator-\d+") {
        Write-Host "✓ Emulator connected" -ForegroundColor Green
    }
    else {
        Write-Host "✗ No emulator found. Please start the emulator first." -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "✗ ADB not found. Please ensure Android SDK is installed." -ForegroundColor Red
    exit 1
}

# Confirm before sending
Write-Host "`nThis will send 240 SMS messages to the emulator." -ForegroundColor Yellow
Write-Host "This will take approximately 3-4 minutes." -ForegroundColor Yellow
$confirmation = Read-Host "`nContinue? (Y/N)"
if ($confirmation -ne 'Y' -and $confirmation -ne 'y') {
    Write-Host "Cancelled by user." -ForegroundColor Yellow
    exit 0
}

# Send transactions in batches
Write-Host "`nSending $($transactions.Count) transactions in batches of $batchSize..." -ForegroundColor Cyan
$successCount = 0
$failureCount = 0
$totalAmount = 0
$batchCount = [Math]::Ceiling($transactions.Count / $batchSize)

for ($batchNum = 0; $batchNum -lt $batchCount; $batchNum++) {
    $startIdx = $batchNum * $batchSize
    $endIdx = [Math]::Min(($batchNum + 1) * $batchSize, $transactions.Count)
    $currentBatch = $transactions[$startIdx..($endIdx - 1)]

    Write-Host "`n--- Batch $($batchNum + 1)/$batchCount (Transactions $($startIdx + 1)-$endIdx) ---" -ForegroundColor Magenta

    for ($i = 0; $i -lt $currentBatch.Count; $i++) {
        $tx = $currentBatch[$i]
        $num = $startIdx + $i + 1

        Write-Host "[$num/$($transactions.Count)] " -NoNewline -ForegroundColor White
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
            Write-Host "  ✓ Sent" -ForegroundColor Green
        }
        else {
            $failureCount++
            Write-Host "  ✗ Failed" -ForegroundColor Red
        }
    }

    # Delay between batches (except for last batch)
    if ($batchNum -lt $batchCount - 1) {
        Write-Host "`nWaiting $batchDelay seconds before next batch..." -ForegroundColor Gray
        Start-Sleep -Seconds $batchDelay
    }
}

# Calculate statistics
$upiCount = ($transactions | Where-Object { $_.type -eq "UPI" }).Count
$creditCardCount = ($transactions | Where-Object { $_.type -eq "CreditCard" }).Count

# Monthly breakdown
$monthlyStats = @{}
foreach ($tx in $transactions) {
    $month = $tx.date.Substring(3, 2)
    if (-not $monthlyStats.ContainsKey($month)) {
        $monthlyStats[$month] = @{ Count = 0; Amount = 0 }
    }
    $monthlyStats[$month].Count++
    $monthlyStats[$month].Amount += [double]$tx.amount
}

# Summary
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Total Sent: $successCount / $($transactions.Count)" -ForegroundColor White
Write-Host "Failed: $failureCount" -ForegroundColor $(if ($failureCount -gt 0) { "Red" } else { "White" })
Write-Host "Total Amount: ₹$([math]::Round($totalAmount, 2))" -ForegroundColor White
Write-Host "UPI Transactions: $upiCount ($(([math]::Round($upiCount * 100.0 / $transactions.Count, 1)))%)" -ForegroundColor White
Write-Host "Credit Card Transactions: $creditCardCount ($(([math]::Round($creditCardCount * 100.0 / $transactions.Count, 1)))%)" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan

# Monthly breakdown
Write-Host "`nMonthly Breakdown:" -ForegroundColor Cyan
$monthNames = @{
    "01" = "January"
    "02" = "February"
    "03" = "March"
    "04" = "April"
    "05" = "May"
    "06" = "June"
    "07" = "July"
    "08" = "August"
    "09" = "September"
    "10" = "October"
    "11" = "November"
    "12" = "December"
}

foreach ($month in ($monthlyStats.Keys | Sort-Object)) {
    $monthName = $monthNames[$month]
    $stats = $monthlyStats[$month]
    $avgPerTx = [math]::Round($stats.Amount / $stats.Count, 2)
    Write-Host "$monthName 2024: $($stats.Count) tx, ₹$([math]::Round($stats.Amount, 2)) (avg ₹$avgPerTx/tx)" -ForegroundColor White
}

# Verification instructions
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Verification Steps" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "1. Wait 5-10 seconds for SMS processing" -ForegroundColor Gray
Write-Host "2. Open Fino app and navigate to Home" -ForegroundColor Gray
Write-Host "3. Check Analytics tab for yearly view" -ForegroundColor Gray
Write-Host "4. Expected total: ~₹1,80,000" -ForegroundColor Gray
Write-Host "5. Expected transactions: ~240" -ForegroundColor Gray
Write-Host "6. Verify monthly distribution:" -ForegroundColor Gray
Write-Host "   - Jan-Feb: Lower (post-holiday)" -ForegroundColor Gray
Write-Host "   - Mar-Apr: Medium" -ForegroundColor Gray
Write-Host "   - May-Aug: Medium-High (summer)" -ForegroundColor Gray
Write-Host "   - Sep-Oct: High (festive)" -ForegroundColor Gray
Write-Host "   - Nov-Dec: Highest (holidays)" -ForegroundColor Gray
Write-Host "=========================================" -ForegroundColor Cyan

if ($successCount -eq $transactions.Count) {
    Write-Host "`n✓ Full year 2024 test data sent successfully!" -ForegroundColor Green
}
else {
    Write-Host "`n⚠ Completed with $failureCount failures." -ForegroundColor Yellow
}

Write-Host ""
