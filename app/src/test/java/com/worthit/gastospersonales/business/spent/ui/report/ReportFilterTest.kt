package com.worthit.gastospersonales.business.spent.ui.report

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class ReportFilterTest {

    private val today = LocalDate.of(2026, 10, 6)
    private val initial = ReportFilter.initial(today)

    @Test
    fun startsWithTheLastThirtyDaysAlreadyApplied() {
        Assert.assertEquals(today.minusDays(30), initial.start)
        Assert.assertEquals(today, initial.end)
        Assert.assertEquals(initial.start, initial.appliedStart)
        Assert.assertEquals(initial.end, initial.appliedEnd)
        Assert.assertFalse(initial.hasPendingChanges())
    }

    @Test
    fun editingDatesDoesNotChangeTheAppliedRange() {
        val edited = initial.withStart(today.minusDays(5))
        Assert.assertEquals(today.minusDays(5), edited.start)
        Assert.assertEquals(initial.appliedStart, edited.appliedStart)
        Assert.assertTrue(edited.hasPendingChanges())
    }

    @Test
    fun applyingCopiesTheDraftToTheAppliedRange() {
        val applied = initial.withStart(today.minusDays(5)).applyDraft()
        Assert.assertEquals(today.minusDays(5), applied.appliedStart)
        Assert.assertEquals(today, applied.appliedEnd)
        Assert.assertFalse(applied.hasPendingChanges())
    }

    @Test
    fun startEqualOrAfterEndIsIgnored() {
        Assert.assertEquals(initial, initial.withStart(today))
        Assert.assertEquals(initial, initial.withStart(today.plusDays(1)))
    }

    @Test
    fun endEqualOrBeforeStartIsIgnored() {
        Assert.assertEquals(initial, initial.withEnd(initial.start))
        Assert.assertEquals(initial, initial.withEnd(initial.start.minusDays(1)))
    }
}
