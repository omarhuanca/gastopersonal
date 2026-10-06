package com.worthit.gastospersonales.business.spent.ui.report

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.ImageShare
import com.worthit.gastospersonales.databinding.FragmentCategoryDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CategoryDetailFragment : Fragment(R.layout.fragment_category_detail) {

    companion object {
        const val ARG_CATEGORY = "category"
        const val ARG_START = "start"
        const val ARG_END = "end"
    }

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryDetailViewModel by viewModels {
        CategoryDetailViewModel.factory(
            requireArguments().getString(ARG_CATEGORY).orEmpty(),
            LocalDate.parse(requireArguments().getString(ARG_START)),
            LocalDate.parse(requireArguments().getString(ARG_END))
        )
    }
    private val adapter = CategoryDetailAdapter()
    private val texts by lazy { CategoryDetailTexts(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoryDetailBinding.bind(view)

        binding.recyclerEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEntries.adapter = adapter
        binding.buttonShare.setOnClickListener { share(viewModel.detail.value) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detail.collect { render(it) }
            }
        }
    }

    private fun render(detail: CategoryDetail) {
        binding.textCategory.text = detail.category
        binding.textPeriod.text = texts.period(detail)
        binding.textTotal.text = texts.total(detail)
        binding.textCount.text = texts.count(detail)
        adapter.submitList(detail.entries)
        binding.textEmpty.visibility = if (detail.entries.isEmpty()) View.VISIBLE else View.GONE
    }

    // Shares the whole detail as image(s) only, with no text.
    private fun share(detail: CategoryDetail) {
        val context = requireContext().applicationContext
        // Views are drawn on the main thread; the files are written off it.
        val pages = CategoryDetailImageRenderer(requireContext()).render(detail, binding.root.width)
        binding.buttonShare.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val uris = withContext(Dispatchers.IO) { ImageShare.save(context, pages, "detalle_categoria") }
            _binding?.buttonShare?.isEnabled = true
            startActivity(ImageShare.chooser(uris, getString(R.string.report_detail_share_chooser)))
        }
    }

    override fun onDestroyView() {
        binding.recyclerEntries.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
