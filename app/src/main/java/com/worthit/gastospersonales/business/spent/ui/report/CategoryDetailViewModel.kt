package com.worthit.gastospersonales.business.spent.ui.report

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.worthit.gastospersonales.business.spent.domain.SpentRepository
import com.worthit.gastospersonales.business.spent.infrastructure.SpentRepositoryImpl
import com.worthit.gastospersonales.core.database.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class CategoryDetailViewModel(
    spentRepository: SpentRepository,
    category: String,
    start: LocalDate,
    end: LocalDate
) : ViewModel() {

    companion object {
        fun factory(category: String, start: LocalDate, end: LocalDate) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    val app = extras[APPLICATION_KEY] as Application
                    val db = AppDatabase.getInstance(app)
                    return CategoryDetailViewModel(
                        SpentRepositoryImpl(db.spentDao(), db.coinDao(), db.categoryDao()),
                        category,
                        start,
                        end
                    ) as T
                }
            }
    }

    val detail: StateFlow<CategoryDetail> = spentRepository.observeBetween(start, end)
        .map { categoryDetail(it, category, start, end) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CategoryDetail(category, start, end, 0, emptyList(), emptyList())
        )
}
