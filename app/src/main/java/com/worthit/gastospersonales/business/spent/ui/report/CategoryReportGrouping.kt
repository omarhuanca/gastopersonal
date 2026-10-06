package com.worthit.gastospersonales.business.spent.ui.report

import com.worthit.gastospersonales.business.spent.domain.Spent
import java.math.BigDecimal
import java.time.LocalDate

data class ReportGroup(
    val category: String,
    val count: Int,
    // One total per coin, e.g. "1400 BOB", because amounts in different coins cannot be added.
    val totals: List<String>
)

// The range is valid only when the start is strictly before the end.
fun isValidRange(start: LocalDate, end: LocalDate): Boolean = start.isBefore(end)

// One "1400 BOB" style total for each coin present in the given spents.
fun totalsByCoin(spents: List<Spent>): List<String> =
    spents
        .groupBy { it.getCoin().getCode().trim().uppercase() }
        .map { (coin, items) ->
            val sum = items.fold(BigDecimal.ZERO) { acc, s -> acc.add(BigDecimal.valueOf(s.getAmount())) }
            // 1400.0 shows as "1400"; 0.3 stays "0.3".
            coin to "${sum.stripTrailingZeros().toPlainString()} $coin"
        }
        .sortedBy { it.first }
        .map { it.second }

fun groupByCategory(spents: List<Spent>): List<ReportGroup> =
    spents
        .groupBy { it.getCategory().getCode().trim().lowercase() }
        .values
        .map { group ->
            ReportGroup(
                category = group.first().getCategory().getCode(),
                count = group.size,
                totals = totalsByCoin(group)
            )
        }
        .sortedBy { it.category.lowercase() }
