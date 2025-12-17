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
}
