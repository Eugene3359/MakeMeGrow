package com.scipath.makemegrow.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.scipath.makemegrow.databinding.SpinnerItemLargeBinding

class CategoryArrayAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    var selectedPosition = 0
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    fun getBinding(convertView: View?, parent: ViewGroup): SpinnerItemLargeBinding {
        val binding = if (convertView == null) {
            SpinnerItemLargeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        } else {
            SpinnerItemLargeBinding.bind(convertView)
        }

        return binding
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = getBinding(convertView, parent)

        binding.text.setTypeface(null, Typeface.BOLD)
        binding.text.text = items[position]

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = getBinding(convertView, parent)

        binding.text.text = items[position]
        if (position == selectedPosition) {
            binding.text.setTypeface(null, Typeface.BOLD)
        } else {
            binding.text.setTypeface(null, Typeface.NORMAL)
        }

        return binding.root
    }
}