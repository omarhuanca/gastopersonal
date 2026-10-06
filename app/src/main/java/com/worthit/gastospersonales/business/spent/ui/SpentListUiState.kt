package com.worthit.gastospersonales.business.spent.ui

import java.time.LocalDate

data class SpentItem(
    val id: Long,
    val description: String,
    val date: LocalDate,
    val amount: String,
    val category: String
)

data class SpentListUiState(
    val spents: List<SpentItem> = emptyList()
)
