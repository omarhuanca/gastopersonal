package com.worthit.gastospersonales.core.ui

import androidx.annotation.StringRes
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl

// Domain exceptions carry English messages; the UI translates them by their constant.
@StringRes
fun domainErrorRes(message: String?): Int = when (message) {
    Category.CATEGORY_CODE_EMPTY, Coin.COIN_CODE_EMPTY -> R.string.error_code_empty
    Spent.DESCRIPTION_CAN_NOT_BE_EMPTY -> R.string.error_description_empty
    Spent.AMOUNT_CAN_NOT_BE_NEGATIVE -> R.string.error_amount_negative
    SpentRepositoryImpl.COIN_NOT_FOUND -> R.string.error_coin_not_found
    SpentRepositoryImpl.CATEGORY_NOT_FOUND -> R.string.error_category_not_found
    else -> R.string.error_unknown
}
