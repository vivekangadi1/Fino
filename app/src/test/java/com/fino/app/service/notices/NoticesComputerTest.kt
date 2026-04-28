package com.fino.app.service.notices

import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.NoticeType
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class NoticesComputerTest {

    private lateinit var computer: NoticesComputer
    private val selectedDate: LocalDate = LocalDate.of(2026, 4, 15)

    @Before
    fun setup() {
        computer = NoticesComputer()
    }

    private fun txn(
        amount: Double,
        merchant: String = "Amazon",
        categoryId: Long? = 1L,
        date: LocalDateTime = selectedDate.atStartOfDay(),
        isRecurring: Boolean = false
    ) = Transaction(
        amount = amount,
        type = TransactionType.DEBIT,
        merchantName = merchant,
        categoryId = categoryId,
        transactionDate = date,
        isRecurring = isRecurring
    )

    @Test
    fun `returns too-early notice when only current has activity and nothing else stages`() {
        // Single small txn — no category change, no merchant rise, no largest_txn (under 1000),
        // no CC, no subs, no weekend signal, no cashback, no new merchants, no pace.
        // Previous has same merchant dated BEFORE April (March) so NEW_MERCHANTS doesn't fire,
        // and prev total keeps pace under 25%.
        val current = listOf(txn(amount = 200.0, merchant = "Corner Shop"))
        val previous = listOf(
            txn(amount = 180.0, merchant = "Corner Shop", date = selectedDate.minusMonths(1).atStartOfDay())
        )
        val notices = computer.compute(
            current = current,
            previous = previous,
            categoryNames = mapOf(1L to "Groceries"),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = emptyList(),
            allTransactions = current + previous,
            cashbackTotal = 0.0
        )
        assertEquals(1, notices.size)
        assertEquals("Too early to call it", notices[0].title)
    }

    @Test
    fun `fires CATEGORY_CHANGE when category grows significantly`() {
        val current = listOf(
            txn(amount = 4000.0, merchant = "Zomato", categoryId = 1L),
            txn(amount = 2000.0, merchant = "Swiggy", categoryId = 1L)
        )
        val previous = listOf(
            txn(amount = 1000.0, merchant = "Zomato", categoryId = 1L)
        )
        val notices = computer.compute(
            current = current,
            previous = previous,
            categoryNames = mapOf(1L to "Food"),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = emptyList(),
            allTransactions = current + previous,
            cashbackTotal = 0.0
        )
        assertTrue(notices.any { it.type == NoticeType.CATEGORY_CHANGE })
    }

    @Test
    fun `fires BILL_DUE when CC has unpaid due within 10 days`() {
        val card = CreditCard(
            id = 1,
            bankName = "HDFC",
            lastFourDigits = "1234",
            previousDue = 5000.0,
            previousDueDate = LocalDate.now().plusDays(3),
            minimumDue = 500.0,
            isPaid = false
        )
        val notices = computer.compute(
            current = emptyList(),
            previous = emptyList(),
            categoryNames = emptyMap(),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = listOf(card),
            allTransactions = emptyList(),
            cashbackTotal = 0.0
        )
        val bill = notices.firstOrNull { it.type == NoticeType.BILL_DUE }
        assertNotNull(bill)
        assertEquals("BILL|1", bill?.routeJson)
    }

    @Test
    fun `does not fire BILL_DUE when card is paid`() {
        val card = CreditCard(
            id = 1,
            bankName = "HDFC",
            lastFourDigits = "1234",
            previousDue = 5000.0,
            previousDueDate = LocalDate.now().plusDays(3),
            isPaid = true
        )
        val notices = computer.compute(
            current = emptyList(),
            previous = emptyList(),
            categoryNames = emptyMap(),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = listOf(card),
            allTransactions = emptyList(),
            cashbackTotal = 0.0
        )
        assertFalse(notices.any { it.type == NoticeType.BILL_DUE })
    }

    @Test
    fun `fires SUBS when recurring transactions exist`() {
        val current = listOf(
            txn(amount = 499.0, merchant = "Netflix", isRecurring = true),
            txn(amount = 149.0, merchant = "Spotify", isRecurring = true)
        )
        val notices = computer.compute(
            current = current,
            previous = emptyList(),
            categoryNames = emptyMap(),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = emptyList(),
            allTransactions = current,
            cashbackTotal = 0.0
        )
        val subs = notices.firstOrNull { it.type == NoticeType.SUBS }
        assertNotNull(subs)
        assertEquals("SUBSCRIPTIONS", subs?.routeJson)
    }

    @Test
    fun `fires CASHBACK when cashbackTotal is positive`() {
        val notices = computer.compute(
            current = listOf(txn(amount = 200.0, merchant = "Shop")),
            previous = emptyList(),
            categoryNames = emptyMap(),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = emptyList(),
            allTransactions = emptyList(),
            cashbackTotal = 450.0
        )
        assertTrue(notices.any { it.type == NoticeType.CASHBACK })
    }

    @Test
    fun `fires LARGEST_TXN for large debit`() {
        val big = txn(amount = 8500.0, merchant = "Flipkart", categoryId = 1L)
        val notices = computer.compute(
            current = listOf(big),
            previous = emptyList(),
            categoryNames = mapOf(1L to "Shopping"),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = emptyList(),
            allTransactions = listOf(big),
            cashbackTotal = 0.0
        )
        assertTrue(notices.any { it.type == NoticeType.LARGEST_TXN })
    }

    @Test
    fun `all returned notices are ranked in order`() {
        val card = CreditCard(
            id = 1,
            bankName = "ICICI",
            lastFourDigits = "4321",
            previousDue = 3000.0,
            previousDueDate = LocalDate.now().plusDays(2),
            isPaid = false
        )
        val current = listOf(
            txn(amount = 6000.0, merchant = "Amazon", categoryId = 1L),
            txn(amount = 499.0, merchant = "Netflix", isRecurring = true)
        )
        val notices = computer.compute(
            current = current,
            previous = listOf(txn(amount = 2000.0, merchant = "Amazon", categoryId = 1L)),
            categoryNames = mapOf(1L to "Shopping"),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = listOf(card),
            allTransactions = current,
            cashbackTotal = 100.0
        )
        assertTrue(notices.isNotEmpty())
        notices.forEachIndexed { idx, n -> assertEquals(idx, n.rankOrder) }
    }

    @Test
    fun `limits output to 8 notices maximum`() {
        // Synthesize signals that trigger many distinct notice types.
        val card = CreditCard(
            id = 1,
            bankName = "HDFC",
            lastFourDigits = "0000",
            previousDue = 1000.0,
            previousDueDate = LocalDate.now().plusDays(1),
            isPaid = false
        )
        val merchants = (1..12).map { "Merchant$it" }
        val current = merchants.map { txn(amount = 2000.0, merchant = it, categoryId = 1L) } +
            txn(amount = 499.0, merchant = "Netflix", isRecurring = true)
        val previous = merchants.map { txn(amount = 1000.0, merchant = it, categoryId = 1L) }
        val notices = computer.compute(
            current = current,
            previous = previous,
            categoryNames = mapOf(1L to "Shopping"),
            period = NoticesComputer.Period.MONTH,
            selectedDate = selectedDate,
            creditCards = listOf(card),
            allTransactions = current + previous,
            cashbackTotal = 50.0
        )
        assertTrue(notices.size <= 8)
    }
}
