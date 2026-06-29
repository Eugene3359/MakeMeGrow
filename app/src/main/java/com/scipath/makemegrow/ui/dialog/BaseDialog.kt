package com.scipath.makemegrow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.scipath.makemegrow.R
import com.scipath.makemegrow.databinding.LayoutDialogBinding

abstract class BaseDialog : DialogFragment() {

    @get:StringRes
    abstract val titleId: Int?
    @get:StringRes
    abstract val messageId: Int?
    @get:StringRes
    protected open val confirmButtonTextId: Int? = R.string.confirm
    @get:StringRes
    protected open val cancelButtonTextId: Int? = R.string.cancel

    abstract val requestKey: String
    abstract val resultKey: String

    protected lateinit var binding: LayoutDialogBinding

    abstract fun onConfirm()

    protected open fun onCancel() {
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutDialogBinding.inflate(inflater, container, false)

        titleId?.let {
            binding.textTitle.setText(it)
        } ?: run {
            binding.textTitle.visibility = View.GONE
        }

        messageId?.let {
            binding.textMessage.setText(it)
        } ?: run {
            binding.textMessage.visibility = View.GONE
        }

        binding.input.visibility = View.GONE
        binding.radioGroup.visibility = View.GONE

        confirmButtonTextId?.let(binding.buttonConfirm::setText)
        cancelButtonTextId?.let(binding.buttonCancel::setText)

        binding.buttonConfirm.setOnClickListener {
            onConfirm()
        }

        binding.buttonCancel.setOnClickListener {
            onCancel()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    protected fun setResult(builder: Bundle.() -> Unit) {
        parentFragmentManager.setFragmentResult(
            requestKey,
            Bundle().apply(builder)
        )
    }
}