package com.worthit.gastospersonales.business.spent.ui.report

import android.content.Context
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.toDisplayText

// The texts of the detail, used by the screen and the shared image, so they always say the
// same thing.
class CategoryDetailTexts(private val context: Context) {

    private val locale get() = context.resources.configuration.locales[0]

    fun period(detail: CategoryDetail): String =
        context.getString(R.string.report_detail_period, detail.start.toString(), detail.end.toString())

    // One "Total: 500 BOB" line per coin, so a category with several coins shows every amount.
    fun totalLines(totals: List<String>): String =
        if (totals.isEmpty()) context.getString(R.string.report_total, "0")
        else totals.joinToString("\n") { context.getString(R.string.report_total, it) }

    fun total(detail: CategoryDetail): String = totalLines(detail.totals)

    fun count(detail: CategoryDetail): String =
        context.resources.getQuantityString(R.plurals.report_spents_count, detail.count, detail.count)

    fun date(entry: DetailEntry): String = entry.date.toDisplayText(locale)
}
