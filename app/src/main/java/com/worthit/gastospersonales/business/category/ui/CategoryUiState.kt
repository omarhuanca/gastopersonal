package com.worthit.gastospersonales.business.category.ui

import androidx.annotation.StringRes

data class CategoryUiState(
    val categories: List<String> = emptyList(),
    @StringRes val error: Int? = null,
    @StringRes val editError: Int? = null,
    val editDone: Boolean = false
)
