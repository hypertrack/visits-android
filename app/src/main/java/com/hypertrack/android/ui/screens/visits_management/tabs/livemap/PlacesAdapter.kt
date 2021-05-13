package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.logistics.android.github.R
import java.util.*

class PlacesAdapter : RecyclerView.Adapter<PlacesAdapter.MyViewHolder>() {
    private val dataset: MutableList<PlaceModel> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var icon: ImageView = v.findViewById(R.id.icon)
        var name: TextView = v.findViewById(R.id.name)
        var address: TextView = v.findViewById(R.id.address)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_place_item, parent, false)
        return MyViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val item = dataset[position]
        holder.icon.setImageResource(if (item.isRecent) R.drawable.ic_history else R.drawable.ic_places)
        holder.name.text = item.primaryText
        holder.address.text = item.secondaryText
        holder.itemView.setOnClickListener { view ->
            onItemClickListener?.onItemClick(this@PlacesAdapter, view, position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataset.size
    }

    fun getItem(position: Int): PlaceModel? {
        return if (dataset.isEmpty()) null else dataset[position]
    }

    fun addAll(items: Collection<PlaceModel>?) {
        dataset.addAll(items!!)
    }

    fun clear() {
        dataset.clear()
    }

    interface OnItemClickListener {
        fun onItemClick(adapter: RecyclerView.Adapter<*>?, view: View?, position: Int)
    }
}