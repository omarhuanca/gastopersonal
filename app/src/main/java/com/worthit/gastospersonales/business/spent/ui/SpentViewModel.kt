package com.worthit.gastospersonales.business.spent.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.domainErrorRes
import com.worthit.gastospersonales.core.ui.toInputAmount
import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.category.domain.CategoryRepository
import com.worthit.gastospersonales.business.category.infrastructure.CategoryRepositoryImpl
import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.coin.domain.CoinRepository
import com.worthit.gastospersonales.business.coin.infrastructure.CoinRepositoryImpl
import com.worthit.gastospersonales.business.spent.domain.Spent
import com.worthit.gastospersonales.business.spent.domain.SpentRepository
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import com.worthit.gastospersonales.core.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SpentViewModel(
    private val spentId: Long?,
    private val spentRepository: SpentRepository,
    private val coinRepository: CoinRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    companion object {
        fun factory(spentId: Long?) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as Application
                val db = AppDatabase.getInstance(app)
                return SpentViewModel(
                    spentId,
                    SpentRepositoryImpl(db.spentDao(), db.coinDao(), db.categoryDao()),
                    CoinRepositoryImpl(db.coinDao()),
                    CategoryRepositoryImpl(db.categoryDao())
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(SpentUiState(editing = spentId != null))
    val uiState: StateFlow<SpentUiState> = _uiState.asStateFlow()

    init {
        if (spentId != null) loadSpent(spentId)
        viewModelScope.launch {
            coinRepository.observeAll().collect { list ->
                _uiState.update { it.copy(coinCodes = list.map { c -> c.getCode() }) }
            }
        }
        viewModelScope.launch {
            categoryRepository.observeAll().collect { list ->
                _uiState.update { it.copy(categoryCodes = list.map { c -> c.getCode() }) }
            }
        }
    }

    private fun loadSpent(id: Long) {
        viewModelScope.launch {
            val spent = spentRepository.findById(id) ?: return@launch
            _uiState.update {
                it.copy(
                    date = spent.getLocalDate(),
                    prefill = SpentPrefill(
                        description = spent.getDescription(),
                        amount = spent.getAmount().toInputAmount(),
                        coinCode = spent.getCoin().getCode(),
                        categoryCode = spent.getCategory().getCode()
                    )
                )
            }
        }
    }

    fun onPrefillHandled() {
        _uiState.update { it.copy(prefill = null) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onSave(description: String, amountText: String, coinCode: String, categoryCode: String) {
        val amount = amountText.trim().toDoubleOrNull()
        when {
            amount == null -> return showError(R.string.error_amount_not_number)
            coinCode.isBlank() -> return showError(R.string.error_coin_not_selected)
            categoryCode.isBlank() -> return showError(R.string.error_category_not_selected)
        }
        viewModelScope.launch {
            try {
                val spent = Spent(
                    description.trim(),
                    _uiState.value.date,
                    amount!!,
                    Coin(coinCode.trim()),
                    Category(categoryCode.trim()),
                    spentId
                )
                if (spentId != null) spentRepository.update(spent) else spentRepository.save(spent)
                _uiState.update { it.copy(error = null, saved = true) }
            } catch (e: Exception) {
                showError(domainErrorRes(e.message))
            }
        }
    }

    fun onCreateCoin(code: String) {
        viewModelScope.launch {
            try {
                val coin = Coin(code.trim())
                val created = coinRepository.save(coin)
                _uiState.update {
                    if (created) it.copy(coinDialogError = null, createdCoin = coin.getCode())
                    else it.copy(coinDialogError = R.string.coin_already_exists)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(coinDialogError = domainErrorRes(e.message)) }
            }
        }
    }

    fun onNewCoinInputChanged() {
        _uiState.update { it.copy(coinDialogError = null) }
    }

    fun onCoinCreatedHandled() {
        _uiState.update { it.copy(createdCoin = null, coinDialogError = null) }
    }

    fun onCreateCategory(code: String) {
        viewModelScope.launch {
            try {
                val category = Category(code.trim())
                val created = categoryRepository.save(category)
                _uiState.update {
                    if (created) it.copy(categoryDialogError = null, createdCategory = category.getCode())
                    else it.copy(categoryDialogError = R.string.category_already_exists)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(categoryDialogError = domainErrorRes(e.message)) }
            }
        }
    }

    fun onNewCategoryInputChanged() {
        _uiState.update { it.copy(categoryDialogError = null) }
    }

    fun onCategoryCreatedHandled() {
        _uiState.update { it.copy(createdCategory = null, categoryDialogError = null) }
    }

    fun onInputChanged() {
        _uiState.update { it.copy(error = null, saved = false) }
    }

    private fun showError(@StringRes message: Int) {
        _uiState.update { it.copy(error = message, saved = false) }
    }
}
