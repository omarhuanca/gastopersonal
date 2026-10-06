package com.worthit.gastospersonales.business.spent.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.StringRes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.worthit.gastospersonales.R
import com.worthit.gastospersonales.databinding.DialogNewItemBinding
import com.worthit.gastospersonales.databinding.FragmentSpentFormBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class SpentFormFragment : Fragment(R.layout.fragment_spent_form) {

    private var _binding: FragmentSpentFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SpentViewModel by viewModels { SpentViewModel.factory(spentIdArgument()) }

    private var shownCoins: List<String>? = null
    private var shownCategories: List<String>? = null
    private var closing = false
    private var coinDefaultApplied = false
    private var categoryDefaultApplied = false

    // The system speech recognizer shows its own UI, so no microphone permission is needed.
    private val voiceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spoken = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                if (!spoken.isNullOrBlank()) appendToDescription(spoken)
            }
        }

    private var newItemDialog: AlertDialog? = null
    private var newItemLayout: TextInputLayout? = null

    companion object {
        const val ARG_SPENT_ID = "spentId"
        private const val NO_ID = -1L
        private const val DEFAULT_COIN = "BOB"
        private const val DEFAULT_CATEGORY = "General"
    }

    private fun spentIdArgument(): Long? =
        arguments?.getLong(ARG_SPENT_ID, NO_ID)?.takeIf { it != NO_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpentFormBinding.bind(view)
        if (spentIdArgument() != null) {
            // The navigation label says "New spent"; editing overrides it after the destination change.
            requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
                ?.setTitle(R.string.spent_edit_title)
        }
        shownCoins = null
        shownCategories = null
        coinDefaultApplied = false
        categoryDefaultApplied = false

        binding.editDescription.doAfterTextChanged { viewModel.onInputChanged() }
        binding.editAmount.doAfterTextChanged { viewModel.onInputChanged() }
        binding.buttonDate.setOnClickListener { showDatePicker() }
        binding.layoutDescription.setEndIconOnClickListener { startVoiceInput() }
        binding.dropdownCoin.setOnItemClickListener { parent, _, position, _ ->
            if (parent.getItemAtPosition(position) == getString(R.string.coin_new_option)) {
                binding.dropdownCoin.setText("", false)
                showNewItemDialog(
                    title = R.string.coin_new_title,
                    hint = R.string.coin_code_hint,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
                    onSave = viewModel::onCreateCoin,
                    onInputChanged = viewModel::onNewCoinInputChanged
                )
            }
        }
        binding.dropdownCategory.setOnItemClickListener { parent, _, position, _ ->
            if (parent.getItemAtPosition(position) == getString(R.string.category_new_option)) {
                binding.dropdownCategory.setText("", false)
                showNewItemDialog(
                    title = R.string.category_new_title,
                    hint = R.string.category_code_hint,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
                    onSave = viewModel::onCreateCategory,
                    onInputChanged = viewModel::onNewCategoryInputChanged
                )
            }
        }
        binding.buttonSave.setOnClickListener {
            viewModel.onSave(
                binding.editDescription.text.toString(),
                binding.editAmount.text.toString(),
                binding.dropdownCoin.text.toString(),
                binding.dropdownCategory.text.toString()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: SpentUiState) {
        binding.buttonDate.text = getString(R.string.spent_date_button, state.date.toString())
        state.prefill?.let { prefill ->
            binding.editDescription.setText(prefill.description)
            binding.editAmount.setText(prefill.amount)
            binding.dropdownCoin.setText(prefill.coinCode, false)
            binding.dropdownCategory.setText(prefill.categoryCode, false)
            viewModel.onPrefillHandled()
        }
        binding.layoutDescription.error = state.error?.let { getString(it) }

        if (state.coinCodes != shownCoins) {
            shownCoins = state.coinCodes
            binding.dropdownCoin.setAdapter(
                dropdownAdapter(state.coinCodes + getString(R.string.coin_new_option))
            )
        }
        if (state.categoryCodes != shownCategories) {
            shownCategories = state.categoryCodes
            binding.dropdownCategory.setAdapter(
                dropdownAdapter(state.categoryCodes + getString(R.string.category_new_option))
            )
        }
        // A new spent starts with the default coin and category, when they still exist.
        if (spentIdArgument() == null) {
            if (!coinDefaultApplied) {
                coinDefaultApplied = applyDefault(binding.dropdownCoin, state.coinCodes, DEFAULT_COIN)
            }
            if (!categoryDefaultApplied) {
                categoryDefaultApplied =
                    applyDefault(binding.dropdownCategory, state.categoryCodes, DEFAULT_CATEGORY)
            }
        }
        newItemLayout?.error = (state.coinDialogError ?: state.categoryDialogError)
            ?.let { getString(it) }
        state.createdCoin?.let { code ->
            newItemDialog?.dismiss()
            binding.dropdownCoin.setText(code, false)
            viewModel.onCoinCreatedHandled()
        }
        state.createdCategory?.let { code ->
            newItemDialog?.dismiss()
            binding.dropdownCategory.setText(code, false)
            viewModel.onCategoryCreatedHandled()
        }
        if (state.saved && !closing) {
            closing = true
            findNavController().popBackStack()
        }
    }

    // Selects the stored code that matches `default` (ignoring case) if the field is still empty.
    // Returns true once the field has a value, so the default is applied only one time.
    private fun applyDefault(field: AutoCompleteTextView, codes: List<String>, default: String): Boolean {
        if (field.text.isNotBlank()) return true
        val match = codes.firstOrNull { it.equals(default, ignoreCase = true) } ?: return false
        field.setText(match, false)
        return true
    }

    private fun dropdownAdapter(items: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)

    private fun showNewItemDialog(
        @StringRes title: Int,
        @StringRes hint: Int,
        inputType: Int,
        onSave: (String) -> Unit,
        onInputChanged: () -> Unit
    ) {
        val dialogBinding = DialogNewItemBinding.inflate(layoutInflater)
        dialogBinding.layoutNewItem.hint = getString(hint)
        dialogBinding.editNewItem.inputType = inputType
        dialogBinding.editNewItem.doAfterTextChanged { onInputChanged() }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        // Overriding the click keeps the dialog open so a validation error can be shown in it.
        dialog.setOnShowListener {
            dialogBinding.editNewItem.requestFocus()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                onSave(dialogBinding.editNewItem.text.toString())
            }
        }
        dialog.setOnDismissListener {
            newItemDialog = null
            newItemLayout = null
            onInputChanged()
        }
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        newItemDialog = dialog
        newItemLayout = dialogBinding.layoutNewItem
        dialog.show()
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            .putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                resources.configuration.locales[0].toLanguageTag()
            )
            .putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt))
        try {
            voiceLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.voice_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    private fun appendToDescription(spoken: String) {
        val current = binding.editDescription.text.toString().trim()
        val combined = if (current.isEmpty()) spoken.trim() else "$current ${spoken.trim()}"
        binding.editDescription.setText(combined)
        binding.editDescription.setSelection(combined.length)
    }

    private fun showDatePicker() {
        val current = viewModel.uiState.value.date
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            val date: LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            viewModel.onDateSelected(date)
        }
        picker.show(childFragmentManager, "date_picker")
    }

    override fun onDestroyView() {
        newItemDialog?.dismiss()
        _binding = null
        super.onDestroyView()
    }
}
