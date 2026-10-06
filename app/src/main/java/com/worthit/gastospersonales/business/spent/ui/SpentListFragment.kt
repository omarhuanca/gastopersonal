package com.worthit.gastospersonales.business.spent.ui

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.core.ui.ImageShare
import com.worthit.gastospersonales.databinding.FragmentSpentListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpentListFragment : Fragment(R.layout.fragment_spent_list) {

    private var _binding: FragmentSpentListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SpentListViewModel by viewModels { SpentListViewModel.Factory }
    private val adapter = SpentAdapter(
        onShare = { item -> share(item) },
        onEdit = { item -> edit(item) },
        onDelete = { item -> confirmDelete(item) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpentListBinding.bind(view)

        binding.recyclerSpents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSpents.adapter = adapter
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.spentFormFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.spents)
                    binding.textEmpty.visibility =
                        if (state.spents.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    // A spent is shared as an image only, with no text.
    private fun share(item: SpentItem) {
        val context = requireContext().applicationContext
        // The view is drawn on the main thread; the file is written off it.
        val page = SpentImageRenderer(requireContext()).render(item, binding.root.width)
        viewLifecycleOwner.lifecycleScope.launch {
            val uris = withContext(Dispatchers.IO) { ImageShare.save(context, listOf(page), "gasto") }
            startActivity(ImageShare.chooser(uris, getString(R.string.spent_share_chooser)))
        }
    }

    private fun edit(item: SpentItem) {
        findNavController().navigate(
            R.id.spentFormFragment,
            bundleOf(SpentFormFragment.ARG_SPENT_ID to item.id)
        )
    }

    private fun confirmDelete(item: SpentItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.spent_delete_title)
            .setMessage(R.string.spent_delete_message)
            .setPositiveButton(R.string.accept) { _, _ -> viewModel.onDelete(item.id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        binding.recyclerSpents.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
