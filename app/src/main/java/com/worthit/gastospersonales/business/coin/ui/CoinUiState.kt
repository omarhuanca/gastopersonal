package com.worthit.gastospersonales.business.coin.ui

import androidx.annotation.StringRes

data class CoinUiState(
    val coins: List<String> = emptyList(),
    @StringRes val error: Int? = null,
    val saved: Boolean = false,
    @StringRes val editError: Int? = null,
    val editDone: Boolean = false
)
