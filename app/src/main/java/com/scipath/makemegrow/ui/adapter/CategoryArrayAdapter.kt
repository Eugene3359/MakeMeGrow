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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            SpinnerItemLargeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        } else {
            SpinnerItemLargeBinding.bind(convertView)
        }

        binding.text.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        binding.text.text = items[position]

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            SpinnerItemLargeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        } else {
            SpinnerItemLargeBinding.bind(convertView)
        }

        binding.text.text = items[position]
        if (position == selectedPosition) {
            binding.text.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        } else {
            binding.text.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }

        return binding.root
    }
}