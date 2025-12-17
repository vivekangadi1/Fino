package com.fino.app.service.parser

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating and providing parser instances.
 */
@Singleton
class ParserFactory @Inject constructor() {

    private val _smsParser: SmsParser by lazy { SmsParser() }
    private val _upiParser: UpiTransactionParser by lazy { UpiTransactionParser() }
    private val _creditCardParser: CreditCardTransactionParser by lazy { CreditCardTransactionParser() }
    private val _billParser: CreditCardBillParser by lazy { CreditCardBillParser() }

    fun getSmsParser(): SmsParser = _smsParser
    fun getUpiParser(): UpiTransactionParser = _upiParser
    fun getCreditCardParser(): CreditCardTransactionParser = _creditCardParser
    fun getBillParser(): CreditCardBillParser = _billParser
}
