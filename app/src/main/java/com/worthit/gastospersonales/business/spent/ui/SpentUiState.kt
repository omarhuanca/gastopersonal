package com.worthit.gastospersonales.business.spent.ui

import androidx.annotation.StringRes
import java.time.LocalDate

// Values of the spent being edited, applied once to the form inputs.
data class SpentPrefill(
    val description: String,
    val amount: String,
    val coinCode: String,
    val categoryCode: String
)

data class SpentUiState(
    val coinCodes: List<String> = emptyList(),
    val categoryCodes: List<String> = emptyList(),
    val date: LocalDate = LocalDate.now(),
    @StringRes val error: Int? = null,
    val saved: Boolean = false,
    @StringRes val coinDialogError: Int? = null,
    val createdCoin: String? = null,
    @StringRes val categoryDialogError: Int? = null,
    val createdCategory: String? = null,
    val editing: Boolean = false,
    val prefill: SpentPrefill? = null
)
