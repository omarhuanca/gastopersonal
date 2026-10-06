package com.worthit.gastospersonales.business.spent.ui.report

// Where each image page ends (y position) when a tall view is split into pages of at most
// maxPageHeight. A page ends at the bottom of a row whenever one fits, so no row is cut in half.
// Only a row taller than a whole page is cut at the limit.
fun pageBreaks(rowBottoms: List<Int>, totalHeight: Int, maxPageHeight: Int): List<Int> {
    require(maxPageHeight > 0) { "A page needs a positive height" }
    val ends = mutableListOf<Int>()
    var start = 0
    while (start < totalHeight) {
        val limit = start + maxPageHeight
        if (limit >= totalHeight) {
            ends += totalHeight
            break
        }
        val cut = rowBottoms.lastOrNull { it > start && it <= limit } ?: limit
        ends += cut
        start = cut
    }
    return ends
}
