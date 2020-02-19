package com.hypertrack.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.hypertrack.android.response.DriverList
import com.hypertrack.logistics.android.github.R

// Create custom spinner for Driver list dropdown
class CustomSpinnerAdapter(context: Context, @LayoutRes resource: Int,
    objects: ArrayList<DriverList>) : ArrayAdapter<DriverList?>(context, resource, 0,
    objects as List<DriverList?>) {

    private var mInflater: LayoutInflater? = null
    private var mContext: Context? = null
    private var mResource = 0
    private var driverLists: ArrayList<DriverList> = ArrayList()

    override fun getDropDownView(
        position: Int, convertView: View?,
        parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, parent)
    }

    private fun createItemView(position: Int, parent: ViewGroup): View {

        val view = mInflater!!.inflate(mResource, parent, false)
        val offTypeTv = view.findViewById<View>(R.id.tvSpinnerText) as TextView
        offTypeTv.text = driverLists[position].name
        return view
    }

    init {
        mContext = context
        mInflater = LayoutInflater.from(context)
        mResource = resource
        driverLists = objects
    }
}