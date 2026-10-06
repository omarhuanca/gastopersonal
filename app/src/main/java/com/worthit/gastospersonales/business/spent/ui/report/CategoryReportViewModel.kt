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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class CategoryReportUiState(
    // The dates shown in the buttons (the draft).
    val start: LocalDate,
    val end: LocalDate,
    // Groups built with the applied dates.
    val groups: List<ReportGroup> = emptyList(),
    val pendingChanges: Boolean = false,
    // The range the groups were built with; the detail screen opens with these dates.
    val appliedStart: LocalDate = start,
    val appliedEnd: LocalDate = end
)

class CategoryReportViewModel(spentRepository: SpentRepository) : ViewModel() {

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as Application
                val db = AppDatabase.getInstance(app)
                return CategoryReportViewModel(
                    SpentRepositoryImpl(db.spentDao(), db.coinDao(), db.categoryDao())
                ) as T
            }
        }
    }

    private val filter = MutableStateFlow(ReportFilter.initial(LocalDate.now()))

    // Only a change of the applied dates reloads the list, not editing the draft.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val groups = filter
        .map { it.appliedStart to it.appliedEnd }
        .distinctUntilChanged()
        .flatMapLatest { (start, end) ->
            spentRepository.observeBetween(start, end).map { groupByCategory(it) }
        }

    val uiState: StateFlow<CategoryReportUiState> = combine(filter, groups) { f, g ->
        CategoryReportUiState(f.start, f.end, g, f.hasPendingChanges(), f.appliedStart, f.appliedEnd)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        filter.value.let { CategoryReportUiState(it.start, it.end) }
    )

    // The date pickers already block invalid days; ReportFilter keeps the rule anyway.
    fun onStartSelected(date: LocalDate) {
        filter.value = filter.value.withStart(date)
    }

    fun onEndSelected(date: LocalDate) {
        filter.value = filter.value.withEnd(date)
    }

    fun onApply() {
        filter.value = filter.value.applyDraft()
    }

    // Back to the default range (last 30 days), applied right away.
    fun onClear() {
        filter.value = ReportFilter.initial(LocalDate.now())
    }
}
