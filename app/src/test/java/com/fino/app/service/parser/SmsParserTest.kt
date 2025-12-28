package com.fino.app.service.parser

import com.fino.app.domain.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SmsParserTest {

    private lateinit var parser: SmsParser

    @Before
    fun setup() {
        parser = SmsParser()
    }

    // ==================== UPI TRANSACTION TESTS ====================

    @Test
    fun `parse HDFC UPI debit SMS - extracts correct amount`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"

        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(350.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts merchant name`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals("MY CHICKEN SHOP", result!!.merchantName)
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts transaction date`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals(LocalDate.of(2024, 12, 14), result!!.transactionDate.toLocalDate())
    }

    @Test
    fun `parse HDFC UPI debit SMS - extracts UPI reference`() {
        val sms = "Paid Rs.350.00 to MY CHICKEN SHOP on 14-12-24 using UPI. UPI Ref: 433218765432. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals("433218765432", result!!.reference)
    }

    @Test
    fun `parse SBI UPI debit SMS - different format`() {
        val sms = "Rs.1200 debited from A/c XX1234 to VPA swiggy@upi on 14-12-24. UPI Ref 433218765432 -SBI"

        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(1200.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("swiggy@upi", result.merchantName)
    }

    @Test
    fun `parse UPI SMS with INR instead of Rs`() {
        val sms = "INR 499.00 debited from A/c XX1234 on 14-12-24 for UPI to merchant@ybl. Ref 987654321 -ICICI"

        val result = parser.parse(sms)

        assertEquals(499.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse UPI SMS with comma in amount`() {
        val sms = "Paid Rs.1,25,000.00 to LANDLORD JOHN on 01-12-24 using UPI. UPI Ref: 433218765433. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals(125000.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse UPI SMS with decimal amount`() {
        val sms = "Rs.499.50 debited from A/c XX5678 to VPA amazonpay@upi on 14-12-24. UPI Ref 433218765433 -SBI"

        val result = parser.parse(sms)

        assertEquals(499.50, result!!.amount, 0.01)
    }

    // ==================== CREDIT CARD TRANSACTION TESTS ====================

    @Test
    fun `parse HDFC credit card transaction - extracts amount`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"

        val result = parser.parse(sms)

        assertNotNull(result)
        assertEquals(2340.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse HDFC credit card transaction - extracts card last four digits`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"

        val result = parser.parse(sms)

        assertEquals("4523", result!!.cardLastFour)
    }

    @Test
    fun `parse HDFC credit card transaction - extracts bank name`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"

        val result = parser.parse(sms)

        assertEquals("HDFC", result!!.bankName)
    }

    @Test
    fun `parse ICICI credit card transaction - different format`() {
        val sms = "Alert: ICICI Card ending 8976 used for INR 5550.00 at CROMA ELECTRONICS on 14-Dec-24"

        val result = parser.parse(sms)

        assertEquals(5550.0, result!!.amount, 0.01)
        assertEquals("CROMA ELECTRONICS", result.merchantName)
        assertEquals("8976", result.cardLastFour)
        assertEquals("ICICI", result.bankName)
    }

    @Test
    fun `parse SBI credit card transaction - yet another format`() {
        val sms = "Your SBI Card ending 3456 was used for Rs.649 at NETFLIX.COM on 14/12/2024"

        val result = parser.parse(sms)

        assertEquals(649.0, result!!.amount, 0.01)
        assertEquals("NETFLIX.COM", result.merchantName)
        assertEquals("3456", result.cardLastFour)
        assertEquals("SBI", result.bankName)
    }

    // ==================== CREDIT CARD BILL TESTS ====================

    @Test
    fun `parse HDFC credit card bill - extracts total due`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"

        val result = parser.parseBill(sms)

        assertNotNull(result)
        assertEquals(12450.0, result!!.totalDue, 0.01)
    }

    @Test
    fun `parse HDFC credit card bill - extracts minimum due`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"

        val result = parser.parseBill(sms)

        assertEquals(625.0, result!!.minimumDue!!, 0.01)
    }

    @Test
    fun `parse HDFC credit card bill - extracts due date`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"

        val result = parser.parseBill(sms)

        assertEquals(LocalDate.of(2025, 1, 5), result!!.dueDate)
    }

    @Test
    fun `parse HDFC credit card bill - extracts card last four`() {
        val sms = "Your HDFC Credit Card XX4523 statement is ready. Total Due: Rs.12450. Min Due: Rs.625. Due Date: 05-Jan-25"

        val result = parser.parseBill(sms)

        assertEquals("4523", result!!.cardLastFour)
    }

    @Test
    fun `parse ICICI credit card bill - different format`() {
        val sms = "ICICI Card bill generated. Amount: Rs.3200. Due: 12-Jan-25. Pay now to avoid charges."

        val result = parser.parseBill(sms)

        assertEquals(3200.0, result!!.totalDue, 0.01)
        assertEquals(LocalDate.of(2025, 1, 12), result.dueDate)
    }

    // ==================== SUBSCRIPTION DETECTION TESTS ====================

    @Test
    fun `parse Google Play subscription - flags as likely subscription`() {
        val sms = "Google Play charged Rs.129 to your card ending 4523 for YouTube Premium subscription"

        val result = parser.parse(sms)

        assertTrue(result!!.isLikelySubscription)
        assertEquals("YouTube Premium", result.merchantName)
        assertEquals(129.0, result.amount, 0.01)
    }

    @Test
    fun `parse Netflix subscription - flags as likely subscription`() {
        val sms = "Your Netflix subscription of Rs.649 has been renewed using card XX8976"

        val result = parser.parse(sms)

        assertTrue(result!!.isLikelySubscription)
        assertEquals("Netflix", result.merchantName)
        assertEquals(649.0, result.amount, 0.01)
    }

    @Test
    fun `parse Amazon Prime subscription - flags as likely subscription`() {
        val sms = "Amazon Prime membership renewed. Rs.1499 charged to card ending 4523."

        val result = parser.parse(sms)

        assertTrue(result!!.isLikelySubscription)
        assertTrue(result.merchantName.contains("Amazon Prime", ignoreCase = true))
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `return null for OTP SMS`() {
        val sms = "Your OTP is 123456. Valid for 5 minutes. Do not share with anyone."

        val result = parser.parse(sms)

        assertNull(result)
    }

    @Test
    fun `return null for promotional SMS`() {
        val sms = "Get 50% cashback on your next transaction! Use code SAVE50. T&C apply."

        val result = parser.parse(sms)

        assertNull(result)
    }

    @Test
    fun `return null for balance inquiry SMS`() {
        val sms = "Dear Customer, your account balance is Rs.45,230 as on 14-12-24"

        val result = parser.parse(sms)

        assertNull(result)
    }

    @Test
    fun `return null for payment reminder SMS`() {
        val sms = "Reminder: Your credit card payment of Rs.12,450 is due on 05-Jan-25"

        val result = parser.parse(sms)

        assertNull(result)
    }

    @Test
    fun `handle merchant name with special characters`() {
        val sms = "Paid Rs.500.00 to CAFÉ COFFEE DAY - T.NAGAR on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals("CAFÉ COFFEE DAY - T.NAGAR", result!!.merchantName)
    }

    @Test
    fun `handle very large amount`() {
        val sms = "Paid Rs.99,99,999.00 to PROPERTY DEALER on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals(9999999.0, result!!.amount, 0.01)
    }

    @Test
    fun `handle amount without decimal`() {
        val sms = "Paid Rs.500 to SHOP on 14-12-24 using UPI. UPI Ref: 123456. -HDFC Bank"

        val result = parser.parse(sms)

        assertEquals(500.0, result!!.amount, 0.01)
    }

    // ==================== CONFIDENCE SCORING ====================

    @Test
    fun `high confidence for well-formatted SMS`() {
        val sms = "HDFC Bank Credit Card XX4523 has been used for Rs.2340.00 at AMAZON on 14-12-24 at 14:30:45"

        val result = parser.parse(sms)

        assertTrue(result!!.confidence >= 0.9f)
    }

    @Test
    fun `lower confidence for ambiguous SMS`() {
        val sms = "Transaction of Rs.500 completed"

        val result = parser.parse(sms)

        if (result != null) {
            assertTrue(result.confidence < 0.7f)
        }
    }

    // ==================== ICICI AUTOPAY SUBSCRIPTION TESTS ====================

    @Test
    fun `parse ICICI Autopay JioHotstar - for Autopay pattern`() {
        val sms = "Rs 1499.00 debited from ICICI Bank Savings Account XX494 on 17-Dec-25 towards JioHotstar for Autopay AutoPay Retrieval Ref No.535196959911"

        val result = parser.parse(sms)

        assertNotNull("Should parse JioHotstar autopay SMS", result)
        assertEquals(1499.0, result!!.amount, 0.01)
        assertEquals("JioHotstar", result.merchantName)
        assertEquals(TransactionType.DEBIT, result.type)
        assertTrue("Should be flagged as subscription", result.isLikelySubscription)
        assertEquals("ICICI", result.bankName)
    }

    @Test
    fun `parse ICICI Autopay Spotify - for MERCHANTMANDATE pattern`() {
        val sms = "Rs 179.00 debited from ICICI Bank Savings Account XX494 on 17-Dec-25 towards Spotify India L for MERCHANTMANDATE AutoPay Retrieval Ref No.108775528251"

        val result = parser.parse(sms)

        assertNotNull("Should parse Spotify autopay SMS", result)
        assertEquals(179.0, result!!.amount, 0.01)
        assertEquals("Spotify India L", result.merchantName)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI Autopay Google Play - for GOOGLE AutoPay pattern`() {
        val sms = "Rs 249.00 debited from ICICI Bank Savings Account XX494 on 28-Nov-25 towards Google Play for GOOGLE AutoPay Retrieval Ref No.711353563325"

        val result = parser.parse(sms)

        assertNotNull("Should parse Google Play autopay SMS", result)
        assertEquals(249.0, result!!.amount, 0.01)
        assertEquals("Google Play", result.merchantName)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI Autopay Amazon Prime - for Amazon Prime AutoPay pattern`() {
        val sms = "Rs 1499.00 debited from ICICI Bank Savings Account XX494 on 15-Dec-25 towards Amazon India for Amazon Prime AutoPay Retrieval Ref No.123456789012"

        val result = parser.parse(sms)

        assertNotNull("Should parse Amazon Prime autopay SMS", result)
        assertEquals(1499.0, result!!.amount, 0.01)
        assertEquals("Amazon India", result.merchantName)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI Autopay Success - for GOOGLE AutoPay pattern`() {
        val sms = "Your account has been successfully debited with Rs 199.00 on 27-Nov-25 towards Google Play for GOOGLE AutoPay, RRN 529552463315-ICICI Bank."

        val result = parser.parse(sms)

        assertNotNull("Should parse ICICI success debit SMS", result)
        assertEquals(199.0, result!!.amount, 0.01)
        assertEquals("Google Play", result.merchantName)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI Autopay heroelectronix - for Create Mandate pattern`() {
        val sms = "Rs 149.00 debited from ICICI Bank Savings Account XX494 on 01-Dec-25 towards heroelectronix for Create Mandate AutoPay Retrieval Ref No.533507411477"

        val result = parser.parse(sms)

        assertNotNull("Should parse Create Mandate SMS", result)
        assertEquals(149.0, result!!.amount, 0.01)
        assertEquals("heroelectronix", result.merchantName)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI Autopay with multi-line SMS body`() {
        // Simulate potential newline in SMS
        val sms = "Rs 1499.00 debited from ICICI Bank Savings Account XX494 on 17-Dec-25 towards\nJioHotstar for Autopay AutoPay Retrieval Ref No.535196959911"

        val result = parser.parse(sms)

        assertNotNull("Should handle multi-line SMS", result)
        assertEquals(1499.0, result!!.amount, 0.01)
    }

    // ==================== BILL/UTILITY PAYMENT TESTS ====================

    @Test
    fun `parse electricity bill payment via UPI`() {
        val sms = "Rs.1890 debited from A/c XX1234 to VPA tatapower@ybl on 15-Dec-25 for Electricity Bill Payment. UPI Ref 123456789 -ICICI"

        val result = parser.parse(sms)

        assertNotNull("Should parse electricity bill payment", result)
        assertEquals(1890.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse broadband bill payment`() {
        val sms = "Rs.699 debited from A/c XX5678 to VPA bsnl@icici on 20-Dec-25 for BSNL Broadband. UPI Ref 987654321 -HDFC"

        val result = parser.parse(sms)

        assertNotNull("Should parse broadband bill payment", result)
        assertEquals(699.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse gas bill payment`() {
        val sms = "Rs.450 debited from A/c XX9012 to VPA mahanagar@upi on 10-Dec-25 for Gas Bill. UPI Ref 111222333 -SBI"

        val result = parser.parse(sms)

        assertNotNull("Should parse gas bill payment", result)
        assertEquals(450.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse water bill payment`() {
        val sms = "Rs.320 debited from A/c XX3456 to VPA bwssb@ybl on 05-Dec-25 for Water Bill. UPI Ref 444555666 -AXIS"

        val result = parser.parse(sms)

        assertNotNull("Should parse water bill payment", result)
        assertEquals(320.0, result!!.amount, 0.01)
    }

    // ==================== INSURANCE PAYMENT TESTS ====================

    @Test
    fun `parse LIC premium payment`() {
        val sms = "Dear Customer, Rs.12500 has been debited from your A/c XX1234 towards LIC Premium for Policy No.12345678. -ICICI Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse LIC premium payment", result)
        assertEquals(12500.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertTrue("Should flag as likely recurring", result.isLikelySubscription)
    }

    @Test
    fun `parse HDFC Life insurance premium`() {
        val sms = "HDFC Life Premium of Rs.8000 debited from A/c XX5678 on 15-Dec-25. Policy: 987654321"

        val result = parser.parse(sms)

        assertNotNull("Should parse HDFC Life premium", result)
        assertEquals(8000.0, result!!.amount, 0.01)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse insurance autopay debit`() {
        val sms = "Rs 5000.00 debited from ICICI Bank Savings Account XX494 on 10-Dec-25 towards ICICI Prudential Life for Insurance AutoPay"

        val result = parser.parse(sms)

        assertNotNull("Should parse insurance autopay", result)
        assertEquals(5000.0, result!!.amount, 0.01)
        assertTrue(result.isLikelySubscription)
    }

    // ==================== MINIMUM BALANCE CHARGE TESTS ====================

    @Test
    fun `parse MAB charge ICICI format`() {
        val sms = "Rs.590 debited from A/c XX1234 towards MAB charges for Oct 2025 -ICICI Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse MAB charge", result)
        assertEquals(590.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse AMB non-maintenance charge`() {
        val sms = "Dear Customer, Rs.295 has been debited from A/c XX5678 for non-maintenance of AMB -HDFC Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse AMB charge", result)
        assertEquals(295.0, result!!.amount, 0.01)
    }

    @Test
    fun `parse service charge`() {
        val sms = "Rs.118 incl GST debited from A/c XX9012 towards Service Charges for Nov 2025 -SBI"

        val result = parser.parse(sms)

        assertNotNull("Should parse service charge", result)
        assertEquals(118.0, result!!.amount, 0.01)
    }

    // ==================== NPS/INVESTMENT TESTS ====================

    @Test
    fun `parse NPS contribution`() {
        val sms = "Rs.5000 debited from A/c XX1234 towards NPS contribution. PRAN: 1234567890 -ICICI Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse NPS contribution", result)
        assertEquals(5000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
    }

    @Test
    fun `parse SIP debit`() {
        val sms = "Rs.2500 debited from A/c XX5678 towards SIP for HDFC Balanced Advantage Fund on 15-Dec-25 -HDFC Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse SIP debit", result)
        assertEquals(2500.0, result!!.amount, 0.01)
        assertTrue("SIP should be flagged as recurring", result.isLikelySubscription)
    }

    @Test
    fun `parse mutual fund purchase`() {
        val sms = "Rs.10000 debited from A/c XX9012 for Mutual Fund Purchase - Axis Bluechip Fund. Ref: MF123456 -Axis Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse mutual fund purchase", result)
        assertEquals(10000.0, result!!.amount, 0.01)
    }

    // ==================== EMI/LOAN PAYMENT TESTS ====================

    @Test
    fun `parse home loan EMI`() {
        val sms = "EMI of Rs.25000 for Home Loan A/c 1234567890 debited from A/c XX1234 on 05-Dec-25 -HDFC Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse home loan EMI", result)
        assertEquals(25000.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertTrue("EMI should be flagged as recurring", result.isLikelySubscription)
    }

    @Test
    fun `parse car loan EMI`() {
        val sms = "Car Loan EMI Rs.15000 auto-debited from A/c XX5678 on 10-Dec-25. Loan A/c: 9876543210 -ICICI Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse car loan EMI", result)
        assertEquals(15000.0, result!!.amount, 0.01)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse personal loan EMI with balance`() {
        val sms = "Personal Loan EMI of Rs.8500 paid from A/c XX9012 on 15-Dec-25. Outstanding: Rs.1,50,000 -SBI"

        val result = parser.parse(sms)

        assertNotNull("Should parse personal loan EMI", result)
        assertEquals(8500.0, result!!.amount, 0.01)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse education loan EMI`() {
        val sms = "Education Loan EMI Rs.12000 debited from A/c XX3456 on 20-Dec-25. Ref: EL2024001 -Axis Bank"

        val result = parser.parse(sms)

        assertNotNull("Should parse education loan EMI", result)
        assertEquals(12000.0, result!!.amount, 0.01)
        assertTrue(result.isLikelySubscription)
    }

    // ==================== ICICI BANK SWEEP TESTS ====================

    @Test
    fun `parse ICICI Bank Sweep to OD account`() {
        val sms = "ICICI Bank Acc XX494 debited Rs. 11,629.00 on 24-Dec-25 InfoSweep to OD A.Avl Bal Rs. 6,402.79.To dispute call 18002662 or SMS BLOCK 494 to 9215676766"

        val result = parser.parse(sms)

        assertNotNull("Should parse ICICI Bank Sweep", result)
        assertEquals(11629.0, result!!.amount, 0.01)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals("Sweep to OD Account", result.merchantName)
        assertEquals("494", result.accountLastFour)
        assertEquals("ICICI", result.bankName)
    }

    @Test
    fun `parse ICICI Bank Sweep without balance info`() {
        val sms = "ICICI Bank Acc XX123 debited Rs. 5000.00 on 15-Dec-25 InfoSweep to OD A."

        val result = parser.parse(sms)

        assertNotNull("Should parse ICICI Bank Sweep without balance", result)
        assertEquals(5000.0, result!!.amount, 0.01)
        assertEquals("Sweep to OD Account", result.merchantName)
    }

    // ==================== BANK OF BARODA UPI CREDIT TESTS ====================

    @Test
    fun `parse Bank of Baroda UPI Credit`() {
        val sms = "Dear BOB UPI User: Your account is credited with INR 13500.00 on 2025-12-04 07:43:27 AM by UPI Ref No 286515111106; AvlBal: Rs13500.00 - BOB"

        val result = parser.parse(sms)

        assertNotNull("Should parse BOB UPI Credit", result)
        assertEquals(13500.0, result!!.amount, 0.01)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals("UPI Credit", result.merchantName)
        assertEquals("286515111106", result.reference)
        assertEquals("BOB", result.bankName)
        assertEquals(PaymentChannel.UPI, result.paymentChannel)
    }

    @Test
    fun `parse Bank of Baroda UPI Credit with smaller amount`() {
        val sms = "Dear BOB UPI User: Your account is credited with INR 500.00 on 2025-11-20 02:15:30 PM by UPI Ref No 123456789012; AvlBal: Rs1500.00 - BOB"

        val result = parser.parse(sms)

        assertNotNull("Should parse smaller BOB UPI Credit", result)
        assertEquals(500.0, result!!.amount, 0.01)
        assertEquals(TransactionType.CREDIT, result.type)
    }

    // ==================== AXIS CREDIT CARD BILL TESTS ====================

    @Test
    fun `parse Axis Credit Card Bill`() {
        val sms = "Payment of INR 15236.36 for Axis Bank Credit Card no. XX5519 is due on 01-01-26 with minimum amount due of INR 305."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse Axis CC Bill", result)
        assertEquals(15236.36, result!!.totalDue, 0.01)
        assertEquals(305.0, result.minimumDue!!, 0.01)
        assertEquals("5519", result.cardLastFour)
        assertEquals("AXIS", result.bankName)
        assertEquals(LocalDate.of(2026, 1, 1), result.dueDate)
    }

    @Test
    fun `parse Axis Credit Card Bill with different amount`() {
        val sms = "Payment of INR 8500.00 for Axis Bank Credit Card no. XX1234 is due on 15-02-26 with minimum amount due of INR 170."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse Axis CC Bill with different amount", result)
        assertEquals(8500.0, result!!.totalDue, 0.01)
        assertEquals(170.0, result.minimumDue!!, 0.01)
    }

    // ==================== ICICI STATEMENT EMAIL BILL TESTS ====================

    @Test
    fun `parse ICICI Statement Email notification`() {
        val sms = "ICICI Bank Credit Card XX1016 Statement is sent to vi********di@gmail.com. Total of Rs 1,504.00 or minimum of Rs 100.00 is due by 05-JAN-26."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse ICICI Statement Email", result)
        assertEquals(1504.0, result!!.totalDue, 0.01)
        assertEquals(100.0, result.minimumDue!!, 0.01)
        assertEquals("1016", result.cardLastFour)
        assertEquals("ICICI", result.bankName)
        assertEquals(LocalDate.of(2026, 1, 5), result.dueDate)
    }

    @Test
    fun `parse ICICI Statement Email with larger amount`() {
        val sms = "ICICI Bank Credit Card XX2345 Statement is sent to test@example.com. Total of Rs 25,000.00 or minimum of Rs 1,250.00 is due by 15-FEB-26."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse ICICI Statement Email with larger amount", result)
        assertEquals(25000.0, result!!.totalDue, 0.01)
        assertEquals(1250.0, result.minimumDue!!, 0.01)
    }

    // ==================== ICICI AUTO-DEBIT BILL TESTS ====================

    @Test
    fun `parse ICICI Auto-debit Bill notification`() {
        val sms = "Total Amount Due on ICICI Bank Credit Card XX2000 is INR 2,218.42. Amount will be debited from your bank account on or before 29-Dec-25. Pls ignore if paid."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse ICICI Auto-debit Bill", result)
        assertEquals(2218.42, result!!.totalDue, 0.01)
        assertNull("Minimum due not provided in this format", result.minimumDue)
        assertEquals("2000", result.cardLastFour)
        assertEquals("ICICI", result.bankName)
        assertEquals(LocalDate.of(2025, 12, 29), result.dueDate)
    }

    @Test
    fun `parse ICICI Auto-debit Bill with different card`() {
        val sms = "Total Amount Due on ICICI Bank Credit Card XX5678 is INR 10,500.00. Amount will be debited from your bank account on or before 15-Jan-26. Pls ignore if paid."

        val result = parser.parseBill(sms)

        assertNotNull("Should parse ICICI Auto-debit Bill with different card", result)
        assertEquals(10500.0, result!!.totalDue, 0.01)
        assertEquals("5678", result.cardLastFour)
    }

    // ==================== ICICI AUTOPAY MANDATE REVOCATION TESTS ====================

    @Test
    fun `parse ICICI AutoPay Mandate Revocation - Google`() {
        val sms = "Dear Customer, your AutoPay mandate is successfully revoked towards GOOGLE INDIA DI for Rs 199.00, RRN 182265865362-ICICI Bank."

        val result = parser.parse(sms)

        assertNotNull("Should parse ICICI AutoPay Revocation", result)
        assertEquals(199.0, result!!.amount, 0.01)
        assertEquals("GOOGLE INDIA DI", result.merchantName)
        assertEquals("182265865362", result.reference)
        assertEquals("ICICI", result.bankName)
        assertEquals(PaymentChannel.AUTOPAY, result.paymentChannel)
        assertTrue("Should be flagged as subscription", result.isLikelySubscription)
        assertTrue("Should be flagged as mandate revocation", result.isMandateRevocation)
    }

    @Test
    fun `parse ICICI AutoPay Mandate Revocation - Spotify`() {
        val sms = "Dear Customer, your AutoPay mandate is successfully revoked towards Spotify India for Rs 119.00, RRN 987654321012-ICICI Bank."

        val result = parser.parse(sms)

        assertNotNull("Should parse Spotify mandate revocation", result)
        assertEquals(119.0, result!!.amount, 0.01)
        assertEquals("Spotify India", result.merchantName)
        assertTrue(result.isMandateRevocation)
        assertTrue(result.isLikelySubscription)
    }

    @Test
    fun `parse ICICI AutoPay Mandate Revocation - Netflix`() {
        val sms = "Dear Customer, your AutoPay mandate is successfully revoked towards NETFLIX COM for Rs 649.00, RRN 111222333444-ICICI Bank."

        val result = parser.parse(sms)

        assertNotNull("Should parse Netflix mandate revocation", result)
        assertEquals(649.0, result!!.amount, 0.01)
        assertEquals("NETFLIX COM", result.merchantName)
        assertTrue(result.isMandateRevocation)
    }
}
