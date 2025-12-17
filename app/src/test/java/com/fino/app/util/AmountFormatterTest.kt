package com.fino.app.util

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AmountFormatterTest {

    private lateinit var formatter: AmountFormatter

    @Before
    fun setup() {
        formatter = AmountFormatter()
    }

    @Test
    fun `format small amount with rupee symbol`() {
        assertEquals("₹350", formatter.format(350.0))
    }

    @Test
    fun `format amount with paise`() {
        assertEquals("₹350.5", formatter.format(350.50))
    }

    @Test
    fun `format thousands with comma`() {
        assertEquals("₹1,234", formatter.format(1234.0))
    }

    @Test
    fun `format lakhs in compact notation`() {
        assertEquals("₹1.2L", formatter.formatCompact(120000.0))
    }

    @Test
    fun `format thousands in compact notation`() {
        assertEquals("₹45.2K", formatter.formatCompact(45230.0))
    }

    @Test
    fun `format sub-thousand not compact`() {
        assertEquals("₹999", formatter.formatCompact(999.0))
    }

    @Test
    fun `format crores in compact notation`() {
        assertEquals("₹1.5Cr", formatter.formatCompact(15000000.0))
    }

    @Test
    fun `format zero`() {
        assertEquals("₹0", formatter.format(0.0))
    }

    @Test
    fun `format negative amount`() {
        assertEquals("-₹500", formatter.format(-500.0))
    }
}
