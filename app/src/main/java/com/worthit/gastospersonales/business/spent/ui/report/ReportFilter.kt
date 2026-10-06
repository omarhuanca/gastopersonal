package com.worthit.gastospersonales.business.spent.ui.report

import java.time.LocalDate

// The dates being edited (start, end) and the dates the list is built with (applied*).
// Editing the dates changes nothing in the list until applyDraft() is called.
data class ReportFilter(
    val start: LocalDate,
    val end: LocalDate,
    val appliedStart: LocalDate,
    val appliedEnd: LocalDate
) {
    companion object {
        const val DEFAULT_DAYS = 30L

        fun initial(today: LocalDate): ReportFilter {
            val start = today.minusDays(DEFAULT_DAYS)
            return ReportFilter(start, today, start, today)
        }
    }

    // A date that would break "start before end" is ignored.
    fun withStart(date: LocalDate): ReportFilter =
        if (isValidRange(date, end)) copy(start = date) else this

    fun withEnd(date: LocalDate): ReportFilter =
        if (isValidRange(start, date)) copy(end = date) else this

    fun applyDraft(): ReportFilter = copy(appliedStart = start, appliedEnd = end)

    fun hasPendingChanges(): Boolean = start != appliedStart || end != appliedEnd
}
