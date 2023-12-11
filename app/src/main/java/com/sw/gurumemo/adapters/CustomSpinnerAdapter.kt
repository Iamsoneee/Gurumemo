package com.sw.gurumemo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.sw.gurumemo.R

class CustomSpinnerAdapter(private val context: Context, private val list: Array<String>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var text: String? = null
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
       return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertedView = convertView
        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.item_spinner, parent, false)
        }

        text = list[position]
        convertedView?.findViewById<TextView>(R.id.spinner_text)?.text = text

        return convertedView!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertedView = convertView
        if (convertedView == null) {
            convertedView = inflater.inflate(R.layout.item_spinner, parent, false)
        }

        text = list[position]
        convertedView?.findViewById<TextView>(R.id.spinner_text)?.text = text

        return convertedView!!
    }


}