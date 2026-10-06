package com.worthit.gastospersonales.business.spent.ui

import androidx.annotation.StringRes
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl

enum class SpentFormField { Description, Amount, Coin, Category }

data class SpentFormError(val field: SpentFormField, @StringRes val message: Int)

// Checks the inputs before a Spent is built. The first problem found is returned.
fun validateSpentInput(amountText: String, coinCode: String, categoryCode: String): SpentFormError? = when {
    // "NaN" and 320 digits parse as NaN / Infinity; those are not amounts.
    amountText.trim().toDoubleOrNull()?.isFinite() != true ->
        SpentFormError(SpentFormField.Amount, R.string.error_amount_not_number)
    coinCode.isBlank() ->
        SpentFormError(SpentFormField.Coin, R.string.error_coin_not_selected)
    categoryCode.isBlank() ->
        SpentFormError(SpentFormField.Category, R.string.error_category_not_selected)
    else -> null
}

// The field a domain or repository error message belongs to, so the error is shown under it.
// Anything unknown stays on the description, the first field of the form.
fun spentErrorField(message: String?): SpentFormField = when (message) {
    Spent.AMOUNT_CAN_NOT_BE_NEGATIVE, Spent.AMOUNT_MUST_BE_FINITE ->SpentFormField.Amount
    Spent.COIN_CAN_NOT_BE_NULL, SpentRepositoryImpl.COIN_NOT_FOUND -> SpentFormField.Coin
    Spent.CATEGORY_CAN_NOT_BE_NULL, SpentRepositoryImpl.CATEGORY_NOT_FOUND -> SpentFormField.Category
    else -> SpentFormField.Description
}
