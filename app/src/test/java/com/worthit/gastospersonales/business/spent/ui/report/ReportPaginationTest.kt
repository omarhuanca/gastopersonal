package com.worthit.gastospersonales.business.spent.ui.report

import org.junit.Assert
import org.junit.Test

class ReportPaginationTest {

    @Test
    fun aShortViewIsASinglePage() {
        Assert.assertEquals(listOf(900), pageBreaks(listOf(300, 600, 900), 900, 4096))
    }

    @Test
    fun aTallViewBreaksAtTheLastRowThatFits() {
        // Rows end at 400, 800, 1200, 1600. Pages hold 1000: the first one ends at 800, not 1000.
        val ends = pageBreaks(listOf(400, 800, 1200, 1600), 1600, 1000)
        Assert.assertEquals(listOf(800, 1600), ends)
    }

    @Test
    fun aRowTallerThanAPageIsCutAtTheLimit() {
        val ends = pageBreaks(listOf(2500), 2500, 1000)
        Assert.assertEquals(listOf(1000, 2000, 2500), ends)
    }

    @Test
    fun theLastPageAlwaysEndsAtTheTotalHeight() {
        val ends = pageBreaks(listOf(300, 600, 900, 1200, 1500), 1650, 1000)
        Assert.assertEquals(1650, ends.last())
        Assert.assertTrue(ends.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun noPageIsTallerThanTheLimit() {
        val bottoms = (1..40).map { it * 170 }
        val ends = pageBreaks(bottoms, 40 * 170, 4096)
        val heights = listOf(0) + ends
        Assert.assertTrue(heights.zipWithNext().all { (a, b) -> b - a <= 4096 })
    }
}
