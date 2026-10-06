package com.worthit.gastospersonales.business.spent.ui.report

import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.spent.domain.Spent
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class CategoryReportGroupingTest {

    private val d1 = LocalDate.of(2026, 10, 1)
    private val d2 = LocalDate.of(2026, 10, 5)

    private fun spent(description: String, date: LocalDate, amount: Double, coin: String, category: String) =
        Spent(description, date, amount, Coin(coin), Category(category))

    @Test
    fun rangeIsValidOnlyWhenStartIsBeforeEnd() {
        Assert.assertTrue(isValidRange(d1, d2))
        Assert.assertFalse(isValidRange(d2, d1))
        Assert.assertFalse(isValidRange(d1, d1))
    }

    @Test
    fun emptyListHasNoGroups() {
        Assert.assertTrue(groupByCategory(emptyList()).isEmpty())
    }

    @Test
    fun groupsSpentsByCategoryIgnoringCase() {
        val groups = groupByCategory(
            listOf(
                spent("a", d1, 10.0, "BOB", "Comida"),
                spent("b", d2, 5.0, "BOB", "comida"),
                spent("c", d1, 7.0, "BOB", "Viajes")
            )
        )
        Assert.assertEquals(2, groups.size)
        Assert.assertEquals(2, groups.first { it.category.equals("comida", true) }.count)
        Assert.assertEquals(1, groups.first { it.category == "Viajes" }.count)
    }

    @Test
    fun totalsAreKeptPerCoin() {
        val group = groupByCategory(
            listOf(
                spent("a", d1, 10.5, "BOB", "Comida"),
                spent("b", d2, 4.5, "BOB", "Comida"),
                spent("c", d2, 20.0, "USD", "Comida")
            )
        ).single()
        Assert.assertEquals(listOf("15 BOB", "20 USD"), group.totals)
    }

    @Test
    fun wholeTotalsShowNoDecimalsAndBigOnesStayPlain() {
        val group = groupByCategory(
            listOf(
                spent("a", d1, 500.0, "BOB", "Comida"),
                spent("b", d2, 900.0, "BOB", "Comida")
            )
        ).single()
        // 1400.0 must not turn into "1.4E+3".
        Assert.assertEquals(listOf("1400 BOB"), group.totals)
    }

    @Test
    fun decimalTotalsKeepTheirDecimals() {
        val group = groupByCategory(
            listOf(
                spent("a", d1, 1400.5, "BOB", "Comida"),
                spent("b", d2, 0.25, "BOB", "Comida")
            )
        ).single()
        Assert.assertEquals(listOf("1400.75 BOB"), group.totals)
    }

    @Test
    fun sumKeepsDecimalsExact() {
        val group = groupByCategory(
            listOf(
                spent("a", d1, 0.1, "BOB", "Comida"),
                spent("b", d2, 0.2, "BOB", "Comida")
            )
        ).single()
        Assert.assertEquals(listOf("0.3 BOB"), group.totals)
    }

    @Test
    fun groupsAreSortedByCategoryName() {
        val groups = groupByCategory(
            listOf(
                spent("a", d1, 1.0, "BOB", "Viajes"),
                spent("b", d1, 1.0, "BOB", "comida")
            )
        )
        Assert.assertEquals(listOf("comida", "Viajes"), groups.map { it.category })
    }
}
