package com.worthit.gastospersonales.core.ui

import org.junit.Assert
import org.junit.Test

class AmountFormatTest {

    @Test
    fun displayKeepsOneDecimalForWholeAmounts() {
        Assert.assertEquals("500.0", 500.0.toDisplayAmount())
        Assert.assertEquals("0.0", 0.0.toDisplayAmount())
    }

    @Test
    fun displayKeepsRealDecimals() {
        Assert.assertEquals("7.5", 7.5.toDisplayAmount())
        Assert.assertEquals("0.25", 0.25.toDisplayAmount())
    }

    @Test
    fun displayNeverUsesScientificNotation() {
        Assert.assertEquals("10000000.0", 10_000_000.0.toDisplayAmount())
        Assert.assertEquals("12345678.9", 12345678.9.toDisplayAmount())
        Assert.assertEquals("100000000000.0", 1.0E11.toDisplayAmount())
    }

    @Test
    fun inputDropsTrailingZeros() {
        Assert.assertEquals("30", 30.0.toInputAmount())
        Assert.assertEquals("7.5", 7.5.toInputAmount())
        Assert.assertEquals("0", 0.0.toInputAmount())
    }

    @Test
    fun inputNeverUsesScientificNotationNorOverflows() {
        Assert.assertEquals("10000000", 10_000_000.0.toInputAmount())
        Assert.assertEquals("12345678.9", 12345678.9.toInputAmount())
        // The old code turned whole numbers into Long, which overflows above 9.2e18.
        Assert.assertEquals("100000000000000000000", 1.0E20.toInputAmount())
    }

    @Test
    fun inputTextParsesBackToTheSameAmount() {
        listOf(0.0, 7.5, 30.0, 12345678.9, 10_000_000.0, 0.1).forEach {
            Assert.assertEquals(it, it.toInputAmount().toDouble(), 0.0)
        }
    }
}
