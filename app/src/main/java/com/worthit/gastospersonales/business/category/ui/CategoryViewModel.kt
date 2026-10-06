package com.worthit.gastospersonales.business.category.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.domainErrorRes
import com.worthit.gastospersonales.business.category.domain.Category
import com.worthit.gastospersonales.business.category.domain.CategoryRepository
import com.worthit.gastospersonales.business.category.infrastructure.CategoryRepositoryImpl
import com.worthit.gastospersonales.core.database.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as Application
                val dao = AppDatabase.getInstance(app).categoryDao()
                return CategoryViewModel(CategoryRepositoryImpl(dao)) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAll().collect { list ->
                _uiState.update { it.copy(categories = list.map { c -> c.getCode() }) }
            }
        }
    }

    fun onDelete(code: String) {
        viewModelScope.launch {
            val deleted = repository.delete(Category(code))
            _uiState.update { it.copy(error = if (deleted) null else R.string.category_in_use) }
        }
    }

    fun onEdit(currentCode: String, newCode: String) {
        viewModelScope.launch {
            try {
                val renamed = repository.rename(Category(currentCode), Category(newCode.trim()))
                _uiState.update {
                    if (renamed) it.copy(error = null, editError = null, editDone = true)
                    else it.copy(editError = R.string.category_already_exists)
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

    fun onErrorShown() {
        _uiState.update { it.copy(error = null) }
    }
}
