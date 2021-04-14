package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.graphics.Typeface
import android.text.TextUtils
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.views.dao.Trip
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TripsAdapter : RecyclerView.Adapter<TripsAdapter.MyViewHolder>() {
    private val dataset: MutableList<Trip> = ArrayList()
    private var onItemClickListener: OnItemClickListener? = null
    private var selectedPos = RecyclerView.NO_POSITION
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var tripTitle: TextView = v.findViewById(R.id.trip_title)
        var tripValue: TextView = v.findViewById(R.id.trip_value)
        var tripIcon: ImageView = v.findViewById(R.id.trip_icon)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        // create a new view
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_trip_item, parent, false)
        return MyViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val item = dataset[position]
        var origin = R.drawable.starting_position
        var destination = R.drawable.destination
        if (item.status == "completed") {
            origin = R.drawable.departure_sd_c
            destination = R.drawable.arrival_sd_c
        }
        if (item.destination == null) {
            holder.tripTitle.setText(R.string.trip_started_from)
            holder.tripValue.text = item.startDate?.let { DATE_FORMAT.format(it) } ?: ""
            holder.tripIcon.setImageResource(origin)
        } else {
            holder.tripTitle.setText(R.string.trip_to)
            if (!TextUtils.isEmpty(item.destination!!.getAddress())) {
                holder.tripValue.text = item.destination!!.getAddress()
            } else {
                val latLng = String.format(
                    holder.itemView.context.getString(R.string.lat_lng),
                    item.destination!!.latitude, item.destination!!.longitude
                )
                holder.tripValue.text = latLng
            }
            holder.tripIcon.setImageResource(destination)
        }
        holder.itemView.isSelected = selectedPos == position
        holder.itemView.setOnClickListener { view ->
            if (selectedPos != position) {
                selectedPos = position
                onItemClickListener?.onItemClick(this@TripsAdapter, view, position)
                notifyDataSetChanged()
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataset.size
    }

    fun getItem(position: Int): Trip? {
        return if (dataset.isEmpty()) null else dataset[position]
    }

    fun setSelection(position: Int) {
        if (selectedPos != position) {
            selectedPos = position
            notifyDataSetChanged()
        }
    }

    fun addAll(items: Collection<Trip>) {
        dataset.addAll(items)
    }

    fun clear() {
        dataset.clear()
    }

    fun update(items: Collection<Trip>?) {
        dataset.clear()
        dataset.addAll(items!!)
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(adapter: RecyclerView.Adapter<*>?, view: View?, position: Int)
    }

    companion object {
        private val STYLE_NORMAL: CharacterStyle = StyleSpan(Typeface.NORMAL)
        private val DATE_FORMAT = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    }
}