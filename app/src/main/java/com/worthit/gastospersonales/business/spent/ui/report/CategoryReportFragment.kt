package com.worthit.gastospersonales.business.spent.ui.report

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.databinding.FragmentCategoryReportBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class CategoryReportFragment : Fragment(R.layout.fragment_category_report) {

    private var _binding: FragmentCategoryReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryReportViewModel by viewModels { CategoryReportViewModel.Factory }
    private val adapter = CategoryReportAdapter { group -> openDetail(group) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoryReportBinding.bind(view)

        binding.recyclerGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerGroups.adapter = adapter

        binding.buttonStart.setOnClickListener { pickStart() }
        binding.buttonEnd.setOnClickListener { pickEnd() }
        binding.buttonApply.setOnClickListener { viewModel.onApply() }
        binding.buttonClear.setOnClickListener { viewModel.onClear() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: CategoryReportUiState) {
        binding.buttonStart.text = getString(R.string.report_start, state.start.toString())
        binding.buttonEnd.text = getString(R.string.report_end, state.end.toString())
        // Apply only makes sense while the chosen dates differ from the ones in use.
        binding.buttonApply.isEnabled = state.pendingChanges
        adapter.submitList(state.groups)
        binding.textEmpty.visibility = if (state.groups.isEmpty()) View.VISIBLE else View.GONE
    }

    // The detail uses the applied range, so it matches the groups on screen, not a pending draft.
    private fun openDetail(group: ReportGroup) {
        val state = viewModel.uiState.value
        findNavController().navigate(
            R.id.categoryDetailFragment,
            bundleOf(
                CategoryDetailFragment.ARG_CATEGORY to group.category,
                CategoryDetailFragment.ARG_START to state.appliedStart.toString(),
                CategoryDetailFragment.ARG_END to state.appliedEnd.toString()
            )
        )
    }

    // The start can be any day up to the day before the end (start must be before end).
    private fun pickStart() {
        val state = viewModel.uiState.value
        val lastValid = state.end.minusDays(1)
        showPicker(state.start, DateValidatorPointBackward.before(lastValid.toUtcMillis())) {
            viewModel.onStartSelected(it)
        }
    }

    // The end can be any day from the day after the start (end must be after start).
    private fun pickEnd() {
        val state = viewModel.uiState.value
        val firstValid = state.start.plusDays(1)
        showPicker(state.end, DateValidatorPointForward.from(firstValid.toUtcMillis())) {
            viewModel.onEndSelected(it)
        }
    }

    private fun showPicker(
        current: LocalDate,
        validator: CalendarConstraints.DateValidator,
        onPicked: (LocalDate) -> Unit
    ) {
        val constraints = CalendarConstraints.Builder()
            .setValidator(validator)
            .setOpenAt(current.toUtcMillis())
            .build()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(current.toUtcMillis())
            .setCalendarConstraints(constraints)
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
        }
        picker.show(childFragmentManager, "report_date_picker")
    }

    private fun LocalDate.toUtcMillis(): Long =
        atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    override fun onDestroyView() {
        binding.recyclerGroups.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
