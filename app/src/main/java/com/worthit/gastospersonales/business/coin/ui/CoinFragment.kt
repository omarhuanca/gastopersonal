package com.worthit.gastospersonales.business.coin.ui

import android.os.Bundle
import android.text.InputType
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
import com.google.android.material.textfield.TextInputLayout
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.databinding.DialogNewItemBinding
import com.worthit.gastospersonales.databinding.FragmentCoinBinding
import kotlinx.coroutines.launch

class CoinFragment : Fragment(R.layout.fragment_coin) {

    private var _binding: FragmentCoinBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CoinViewModel by viewModels { CoinViewModel.Factory }
    private val adapter = CoinAdapter(
        onEdit = { code -> showEditDialog(code) },
        onDelete = { code -> viewModel.onDelete(code) }
    )

    private var editDialog: AlertDialog? = null
    private var editLayout: TextInputLayout? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCoinBinding.bind(view)

        binding.recyclerCoins.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCoins.adapter = adapter

        binding.editCode.doAfterTextChanged { viewModel.onInputChanged() }
        binding.buttonSave.setOnClickListener {
            viewModel.onSave(binding.editCode.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: CoinUiState) {
        adapter.submitList(state.coins)
        binding.layoutCode.error = state.error?.let { getString(it) }
        if (state.saved) binding.editCode.text?.clear()
        editLayout?.error = state.editError?.let { getString(it) }
        if (state.editDone) {
            editDialog?.dismiss()
            viewModel.onEditHandled()
        }
    }

    private fun showEditDialog(currentCode: String) {
        val dialogBinding = DialogNewItemBinding.inflate(layoutInflater)
        dialogBinding.layoutNewItem.hint = getString(R.string.coin_code_hint)
        dialogBinding.editNewItem.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        dialogBinding.editNewItem.setText(currentCode)
        dialogBinding.editNewItem.setSelection(currentCode.length)
        dialogBinding.editNewItem.doAfterTextChanged { viewModel.onEditInputChanged() }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.coin_edit_title)
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
        binding.recyclerCoins.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
