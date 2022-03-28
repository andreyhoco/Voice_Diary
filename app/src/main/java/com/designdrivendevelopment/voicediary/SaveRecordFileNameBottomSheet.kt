package com.designdrivendevelopment.voicediary

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout

class SaveRecordFileNameBottomSheet : BottomSheetDialogFragment() {
    private var enteredNameTextField: TextInputLayout? = null
    private var saveButton: Button? = null
    private var success = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_rename_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!success) setFragmentResult(RESULT_ENTER_FILENAME_KEY, Bundle())
    }

    private fun setupListeners() {
        enteredNameTextField?.error = null
        saveButton?.setOnClickListener {
            val text = enteredNameTextField?.editText?.text?.toString().orEmpty()
            if (text.isEmpty()) {
                enteredNameTextField?.error = getString(R.string.error_missing_filename)
                return@setOnClickListener
            }
            val bundle = Bundle().apply {
                putString(ENTERED_FILENAME, text)
                putString(OLD_FILENAME, arguments?.getString(OLD_FILENAME).orEmpty())
            }
            success = true
            setFragmentResult(RESULT_ENTER_FILENAME_KEY, bundle)
            dismiss()
        }
    }

    private fun initViews(view: View) {
        enteredNameTextField = view.findViewById(R.id.text_entered_filename)
        saveButton = view.findViewById(R.id.save_filename)
    }

    private fun clearViews() {
        enteredNameTextField = null
        saveButton = null
    }

    companion object {
        const val RESULT_ENTER_FILENAME_KEY = "enter_filename_key"
        const val ENTERED_FILENAME = "entered_filename"
        const val OLD_FILENAME = "old_filename"
        const val SAVE_RECORD_FILENAME_TAG = "save_record_filename_tag"

        @JvmStatic
        fun newInstance(oldFilename: String?) = SaveRecordFileNameBottomSheet().apply {
            if (oldFilename != null) arguments = Bundle().apply {
                putString(OLD_FILENAME, oldFilename)
            }
        }
    }
}