package com.worthit.gastospersonales.business.spent.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.worthit.gastospersonales.business.spent.domain.SpentRepository
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import com.worthit.gastospersonales.core.database.AppDatabase
import com.worthit.gastospersonales.core.ui.toDisplayAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpentListViewModel(private val repository: SpentRepository) : ViewModel() {

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as Application
                val db = AppDatabase.getInstance(app)
                return SpentListViewModel(
                    SpentRepositoryImpl(db.spentDao(), db.coinDao(), db.categoryDao())
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(SpentListUiState())
    val uiState: StateFlow<SpentListUiState> = _uiState.asStateFlow()

    fun onDelete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    init {
        viewModelScope.launch {
            repository.observeAll().collect { list ->
                _uiState.value = SpentListUiState(
                    list.map {
                        SpentItem(
                            id = it.getId() ?: 0,
                            description = it.getDescription(),
                            date = it.getLocalDate(),
                            amount = "${it.getAmount().toDisplayAmount()} ${it.getCoin().getCode()}",
                            category = it.getCategory().getCode()
                        )
                    }
                )
            }
        }
    }
}
