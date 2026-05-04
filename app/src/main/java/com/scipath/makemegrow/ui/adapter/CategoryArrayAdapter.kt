package com.scipath.makemegrow.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.scipath.makemegrow.R

class CategoryArrayAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    var selectedPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_large, parent, false)

        val text: TextView = view.findViewById(R.id.text)
        text.setTypeface(null, Typeface.BOLD)
        text.text = items[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_large, parent, false)

        val text: TextView = view.findViewById(R.id.text)
        text.text = items[position]
        if (position == selectedPosition) {
            text.setTypeface(null, Typeface.BOLD)
        } else {
            text.setTypeface(null, Typeface.NORMAL)
        }

        return view
    }
}