package com.worthit.gastospersonales.business.spent.ui.report

import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.core.ui.toDisplayAmount
import java.time.LocalDate

data class DetailEntry(
    val description: String,
    val date: LocalDate,
    val amount: String
)

// Everything shown (and shared) on the detail screen of one category in a date range.
data class CategoryDetail(
    val category: String,
    val start: LocalDate,
    val end: LocalDate,
    val count: Int,
    val totals: List<String>,
    val entries: List<DetailEntry>
)

fun categoryDetail(
    spents: List<Spent>,
    category: String,
    start: LocalDate,
    end: LocalDate
): CategoryDetail {
    val inCategory = spents.filter { it.getCategory().getCode().trim().equals(category.trim(), ignoreCase = true) }
    return CategoryDetail(
        category = inCategory.firstOrNull()?.getCategory()?.getCode() ?: category,
        start = start,
        end = end,
        count = inCategory.size,
        totals = totalsByCoin(inCategory),
        entries = inCategory
            .sortedByDescending { it.getLocalDate() }
            .map {
                DetailEntry(it.getDescription(), it.getLocalDate(), "${it.getAmount().toDisplayAmount()} ${it.getCoin().getCode()}")
            }
    )
}
