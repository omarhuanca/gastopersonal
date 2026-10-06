package com.worthit.gastospersonales.business.category.ui

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.databinding.DialogNewItemBinding
import com.worthit.gastospersonales.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment(R.layout.fragment_category) {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels { CategoryViewModel.Factory }
    private val adapter = CategoryAdapter(
        onEdit = { code -> showEditDialog(code) },
        onDelete = { code -> viewModel.onDelete(code) }
    )

    private var editDialog: AlertDialog? = null
    private var editLayout: TextInputLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCategoryBinding.bind(view)

        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCategories.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: CategoryUiState) {
        adapter.submitList(state.categories)
        binding.textEmpty.visibility = if (state.categories.isEmpty()) View.VISIBLE else View.GONE
        state.error?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
        editLayout?.error = state.editError?.let { getString(it) }
        if (state.editDone) {
            editDialog?.dismiss()
            viewModel.onEditHandled()
        }
    }

    private fun showEditDialog(currentCode: String) {
        val dialogBinding = DialogNewItemBinding.inflate(layoutInflater)
        dialogBinding.layoutNewItem.hint = getString(R.string.category_code_hint)
        dialogBinding.editNewItem.setText(currentCode)
        dialogBinding.editNewItem.setSelection(currentCode.length)
        dialogBinding.editNewItem.doAfterTextChanged { viewModel.onEditInputChanged() }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.category_edit_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        // Overriding the click keeps the dialog open so a validation error can be shown in it.
        dialog.setOnShowListener {
            dialogBinding.editNewItem.requestFocus()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                viewModel.onEdit(currentCode, dialogBinding.editNewItem.text.toString())
            }
        }
        dialog.setOnDismissListener {
            editDialog = null
            editLayout = null
            viewModel.onEditInputChanged()
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        editDialog = dialog
        editLayout = dialogBinding.layoutNewItem
        dialog.show()
    }

    override fun onDestroyView() {
        editDialog?.dismiss()
        binding.recyclerCategories.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
