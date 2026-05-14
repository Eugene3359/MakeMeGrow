package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.scipath.makemegrow.R

abstract class BaseDialog : DialogFragment() {

    @get:StringRes
    abstract val titleId: Int?
    @get:StringRes
    abstract val messageId: Int?
    @get:StringRes
    abstract val inputHintId: Int?
    @get:StringRes
    abstract val confirmButtonTextId: Int?
    @get:StringRes
    abstract val cancelButtonTextId: Int?

    abstract val requestKey: String
    abstract val resultKey: String

    protected lateinit var title: TextView
    protected lateinit var message: TextView
    protected lateinit var input: EditText
    protected lateinit var buttonConfirm: Button
    protected lateinit var buttonCancel: Button

    abstract fun onConfirm()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.layout_dialog, container, false)

        title = view.findViewById(R.id.text_title)
        message = view.findViewById(R.id.text_message)
        input = view.findViewById(R.id.input)
        buttonConfirm = view.findViewById(R.id.button_confirm)
        buttonCancel = view.findViewById(R.id.button_cancel)

        titleId?.let {
            title.setText(it)
        } ?: run {
            title.visibility = View.GONE
        }

        messageId?.let {
            message.setText(it)
        } ?: run {
            message.visibility = View.GONE
        }

        inputHintId?.let {
            input.setHint(it)
        } ?: run {
            input.visibility = View.GONE
        }

        confirmButtonTextId?.let(buttonConfirm::setText)
        cancelButtonTextId?.let(buttonCancel::setText)

        buttonConfirm.setOnClickListener {
            onConfirm()
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