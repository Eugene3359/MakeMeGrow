package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.scipath.makemegrow.R

class AddCategoryDialog : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "add_category_request"
        const val RESULT_KEY_NAME = "category_name"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_add_category, container, false)

        val input: EditText = view.findViewById(R.id.input_category_name)
        val buttonAdd: Button = view.findViewById(R.id.button_add)
        val buttonCancel: Button = view.findViewById(R.id.button_cancel)

        buttonAdd.setOnClickListener {
            val name = input.text.toString()
            if (name.isNotEmpty()) {
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    Bundle().apply {
                        putString(RESULT_KEY_NAME, name)
                    }
                )
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.enter_category_name,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}