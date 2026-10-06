package com.worthit.gastospersonales.core.ui

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class DateFormattingTest {

    private val date = LocalDate.of(2026, 9, 30)

    @Test
    fun englishUsesMonthNameFirst() {
        Assert.assertEquals("Sep 30, 2026", date.toDisplayText(Locale.forLanguageTag("en")))
    }

    @Test
    fun spanishIsNotTheIsoFormat() {
        val text = date.toDisplayText(Locale.forLanguageTag("es"))
        Assert.assertNotEquals(date.toString(), text)
        Assert.assertTrue(text.contains("2026"))
        Assert.assertTrue(text.startsWith("30"))
    }
}
