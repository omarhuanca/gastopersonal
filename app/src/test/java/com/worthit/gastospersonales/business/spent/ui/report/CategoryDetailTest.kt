package com.worthit.gastospersonales.business.spent.ui.report

import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.spent.domain.Spent
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class CategoryDetailTest {

    private val start = LocalDate.of(2026, 9, 6)
    private val end = LocalDate.of(2026, 10, 6)
    private val d1 = LocalDate.of(2026, 10, 1)
    private val d2 = LocalDate.of(2026, 10, 5)

    private fun spent(description: String, date: LocalDate, amount: Double, coin: String, category: String) =
        Spent(description, date, amount, Coin(coin), Category(category))

    private val spents = listOf(
        spent("old", d1, 500.0, "BOB", "General"),
        spent("new", d2, 900.0, "BOB", "general"),
        spent("other", d2, 20.0, "USD", "Viajes")
    )

    @Test
    fun onlyTheRequestedCategoryIsKeptIgnoringCase() {
        val detail = categoryDetail(spents, "GENERAL", start, end)
        Assert.assertEquals(2, detail.count)
        Assert.assertEquals(listOf("new", "old"), detail.entries.map { it.description })
    }

    @Test
    fun totalMatchesTheGroupedSummary() {
        val detail = categoryDetail(spents, "General", start, end)
        val group = groupByCategory(spents).first { it.category.equals("General", true) }
        Assert.assertEquals(listOf("1400 BOB"), detail.totals)
        Assert.assertEquals(group.totals, detail.totals)
        Assert.assertEquals(group.count, detail.count)
    }

    @Test
    fun entriesGoFromNewestToOldestWithCoinInTheAmount() {
        val detail = categoryDetail(spents, "General", start, end)
        Assert.assertEquals(listOf(d2, d1), detail.entries.map { it.date })
        Assert.assertEquals("900.0 BOB", detail.entries.first().amount)
    }

    @Test
    fun keepsTheRangeItWasOpenedWith() {
        val detail = categoryDetail(spents, "General", start, end)
        Assert.assertEquals(start, detail.start)
        Assert.assertEquals(end, detail.end)
    }

    @Test
    fun aCategoryWithoutSpentsGivesAnEmptyDetail() {
        val detail = categoryDetail(spents, "Salud", start, end)
        Assert.assertEquals("Salud", detail.category)
        Assert.assertEquals(0, detail.count)
        Assert.assertTrue(detail.totals.isEmpty())
        Assert.assertTrue(detail.entries.isEmpty())
    }
}
