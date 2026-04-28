package com.fino.app.presentation.screens.insights

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.AccountRepository
import com.fino.app.data.repository.BillRepository
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.CreditCardRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.Bill
import com.fino.app.domain.model.BillEntityStatus
import com.fino.app.domain.model.CreditCard
import com.fino.app.domain.model.Transaction
import com.fino.app.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class BillCategorySlice(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class BillDetailUiState(
    val bill: Bill? = null,
    val card: CreditCard? = null,
    val issuerLabel: String? = null,
    val cycleStart: LocalDate? = null,
    val cycleEnd: LocalDate? = null,
    val dueDate: LocalDate? = null,
    val totalDue: Double = 0.0,
    val minimumDue: Double? = null,
    val categorySlices: List<BillCategorySlice> = emptyList(),
    val daysUntilDue: Int = 0,
    val isPaid: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val creditCardRepository: CreditCardRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val billId: Long = savedStateHandle["billId"] ?: 0L

    private val _uiState = MutableStateFlow(BillDetailUiState())
    val uiState: StateFlow<BillDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val resolvedBill = billRepository.getById(billId)
                ?: billRepository.getByCreditCard(billId).maxByOrNull { it.cycleEnd }

            if (resolvedBill != null) {
                collectForBill(resolvedBill)
            } else {
                collectForCreditCard(billId)
            }
        }
    }

    private suspend fun collectForBill(bill: Bill) {
        val account = bill.accountId?.let { accountRepository.getById(it) }
        val issuerFallback = bill.payeeName
            ?: account?.displayName
            ?: "Bill"

        combine(
            transactionRepository.getAllTransactionsFlow(),
            creditCardRepository.getActiveCardsFlow(),
            categoryRepository.getAllActive()
        ) { transactions, cards, categories ->
            val matchedCard = bill.creditCardId?.let { ccId ->
                cards.firstOrNull { it.id == ccId }
            }
            val catMap = categories.associateBy { it.id }
            val cycleStart = Instant.ofEpochMilli(bill.cycleStart)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val cycleEnd = Instant.ofEpochMilli(bill.cycleEnd)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val dueDate = Instant.ofEpochMilli(bill.dueDate)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            val cycleTxns = transactions.filter { txn ->
                txn.type == TransactionType.DEBIT &&
                    inCycle(txn, cycleStart, cycleEnd) &&
                    belongsToBill(txn, bill, matchedCard)
            }
            val slices = buildSlices(cycleTxns, catMap)
            val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate).toInt()

            BillDetailUiState(
                bill = bill,
                card = matchedCard,
                issuerLabel = matchedCard?.let { "${it.bankName} ****${it.lastFourDigits}" }
                    ?: issuerFallback,
                cycleStart = cycleStart,
                cycleEnd = cycleEnd,
                dueDate = dueDate,
                totalDue = bill.totalDue,
                minimumDue = bill.minDue,
                categorySlices = slices,
                daysUntilDue = days,
                isPaid = bill.status == BillEntityStatus.PAID,
                isLoading = false
            )
        }.collect { s -> _uiState.update { s } }
    }

    private suspend fun collectForCreditCard(cardId: Long) {
        combine(
            transactionRepository.getAllTransactionsFlow(),
            creditCardRepository.getActiveCardsFlow(),
            categoryRepository.getAllActive()
        ) { transactions, cards, categories ->
            val card = cards.firstOrNull { it.id == cardId }
                ?: return@combine BillDetailUiState(isLoading = false)
            val catMap = categories.associateBy { it.id }
            val cycleEnd = card.effectiveDueDate ?: LocalDate.now()
            val cycleStart = cycleEnd.minusMonths(1).withDayOfMonth(
                card.billingCycleDay ?: 1
            )
            val cardTxns = transactions.filter { txn ->
                txn.type == TransactionType.DEBIT &&
                    txn.creditCardId == card.id &&
                    !txn.transactionDate.toLocalDate().isBefore(cycleStart) &&
                    !txn.transactionDate.toLocalDate().isAfter(cycleEnd)
            }
            val total = cardTxns.sumOf { it.amount }
            val slices = buildSlices(cardTxns, catMap)
            val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), cycleEnd).toInt()
            BillDetailUiState(
                bill = null,
                card = card,
                issuerLabel = "${card.bankName} ****${card.lastFourDigits}",
                cycleStart = cycleStart,
                cycleEnd = cycleEnd,
                dueDate = cycleEnd,
                totalDue = card.effectiveDueAmount.takeIf { it > 0 } ?: total,
                minimumDue = card.minimumDue,
                categorySlices = slices,
                daysUntilDue = days,
                isPaid = false,
                isLoading = false
            )
        }.collect { s -> _uiState.update { s } }
    }

    private fun inCycle(
        txn: Transaction,
        cycleStart: LocalDate,
        cycleEnd: LocalDate
    ): Boolean {
        val date = txn.transactionDate.toLocalDate()
        return !date.isBefore(cycleStart) && !date.isAfter(cycleEnd)
    }

    private fun belongsToBill(
        txn: Transaction,
        bill: Bill,
        matchedCard: CreditCard?
    ): Boolean {
        if (matchedCard != null && txn.creditCardId == matchedCard.id) return true
        if (bill.accountId != null && txn.accountId == bill.accountId) return true
        return false
    }

    private fun buildSlices(
        txns: List<Transaction>,
        catMap: Map<Long, com.fino.app.domain.model.Category>
    ): List<BillCategorySlice> {
        val total = txns.sumOf { it.amount }
        if (total <= 0) return emptyList()
        return txns.groupBy { it.categoryId }
            .map { (catId, group) ->
                val sum = group.sumOf { it.amount }
                BillCategorySlice(
                    categoryName = catId?.let { catMap[it]?.name } ?: "Uncategorized",
                    amount = sum,
                    percentage = (sum / total).toFloat()
                )
            }
            .sortedByDescending { it.amount }
            .take(6)
    }

    fun markPaid() {
        viewModelScope.launch {
            val state = _uiState.value
            val bill = state.bill
            if (bill != null) {
                billRepository.markPaid(bill.id, bill.totalDue)
            } else {
                state.card?.let { creditCardRepository.markAsPaid(it.id) }
            }
        }
    }
}
