package com.worthit.gastospersonales.business.coin.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.domainErrorRes
import com.worthit.gastospersonales.business.coin.domain.Coin
import com.worthit.gastospersonales.business.coin.domain.CoinRepository
import com.worthit.gastospersonales.business.coin.infrastructure.CoinRepositoryImpl
import com.worthit.gastospersonales.core.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoinViewModel(private val repository: CoinRepository) : ViewModel() {

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as Application
                val dao = AppDatabase.getInstance(app).coinDao()
                return CoinViewModel(CoinRepositoryImpl(dao)) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CoinUiState())
    val uiState: StateFlow<CoinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { list ->
                _uiState.update { it.copy(coins = list.map { c -> c.getCode() }) }
            }
        }
    }

    fun onSave(code: String) {
        viewModelScope.launch {
            try {
                val created = repository.save(Coin(code.trim()))
                _uiState.update {
                    if (created) it.copy(error = null, saved = true)
                    else it.copy(error = R.string.coin_already_exists, saved = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = domainErrorRes(e.message), saved = false) }
            }
        }
    }

    fun onDelete(code: String) {
        viewModelScope.launch {
            try {
                repository.delete(Coin(code))
            } catch (e: SQLiteConstraintException) {
                _uiState.update { it.copy(error = R.string.coin_in_use) }
            }
        }
    }

    fun onEdit(currentCode: String, newCode: String) {
        viewModelScope.launch {
            try {
                val renamed = repository.rename(Coin(currentCode), Coin(newCode.trim()))
                _uiState.update {
                    if (renamed) it.copy(error = null, editError = null, editDone = true)
                    else it.copy(editError = R.string.coin_already_exists)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(editError = domainErrorRes(e.message)) }
            }
        }
    }

    fun onEditInputChanged() {
        _uiState.update { it.copy(editError = null) }
    }

    fun onEditHandled() {
        _uiState.update { it.copy(editDone = false, editError = null) }
    }

    fun onInputChanged() {
        _uiState.update { it.copy(error = null, saved = false) }
    }
}
