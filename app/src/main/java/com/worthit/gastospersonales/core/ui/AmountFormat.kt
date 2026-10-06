package com.worthit.gastospersonales.core.ui

import java.math.BigDecimal

// Amounts are plain numbers everywhere. Double.toString switches to "1.0E7" from 10,000,000 up,
// so every place that shows an amount goes through these two functions instead.

// For reading: at least one decimal, like before ("500.0", "7.5", "10000000.0").
fun Double.toDisplayAmount(): String {
    val value = BigDecimal.valueOf(this)
    return (if (value.scale() <= 0) value.setScale(1) else value).toPlainString()
}

// For the amount input: no trailing zeros ("30", "7.5", "12345678.9").
fun Double.toInputAmount(): String = BigDecimal.valueOf(this).stripTrailingZeros().toPlainString()
