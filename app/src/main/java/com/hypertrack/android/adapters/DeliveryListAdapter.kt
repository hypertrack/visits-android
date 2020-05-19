package com.hypertrack.android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.repository.*
import com.hypertrack.logistics.android.github.R

// Job adapter (Multiple type jobs Pending,Completed,Visited)
class DeliveryListAdapter(
    private val deliveries: LiveData<List<DeliveryListItem>>,
    onclick: OnListAdapterClick
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClick: OnListAdapterClick = onclick




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.inflate_header_item -> {

                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.inflate_header_item, parent, false)

                return HeaderViewHolder(itemView)
            }

            else -> {

                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.inflater_delivery_item, parent, false)

                return DeliveryViewHolder(itemView)

            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = deliveries.value?.get(holder.adapterPosition)) {
            is HeaderDeliveryItem -> {
                val headerView = holder as HeaderViewHolder
                headerView.tvHeaderText.text = item.text
            }
            is Delivery -> {
                val deliveryView = holder as DeliveryViewHolder
                deliveryView.tvDescription.text =
                    createAddress(item.address)
                deliveryView.tvTitle.text = item.delivery_id
                deliveryView.ivCameraIcon.visibility = if (item.hasPicture()) View.VISIBLE else View.INVISIBLE
                deliveryView.ivCompass.visibility = if (item.status == VISITED) View.VISIBLE else View.INVISIBLE
                deliveryView.ivNoteIcon.visibility = if (item.hasNotes()) View.VISIBLE else View.INVISIBLE

            }
        }

        holder.itemView.setOnClickListener {
            onItemClick.onJobItemClick(holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return deliveries.value?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (deliveries.value?.get(position) is HeaderDeliveryItem)
            R.layout.inflate_header_item
        else {
            R.layout.inflater_delivery_item
        }

    }

    private inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var tvHeaderText: TextView = view.findViewById(R.id.tvHeader) as TextView

    }


    inner class DeliveryViewHolder(holder: View) : RecyclerView.ViewHolder(holder) {

        internal var tvTitle: TextView = holder.findViewById(R.id.tvTitle) as TextView
        internal var tvDescription: TextView = holder.findViewById(R.id.tvDescription) as TextView
        internal var ivCameraIcon: ImageView =
            holder.findViewById(R.id.ivCameraIcon) as AppCompatImageView
        internal var ivNoteIcon: ImageView =
            holder.findViewById(R.id.ivNote) as AppCompatImageView
        internal var ivCompass: ImageView =
            holder.findViewById(R.id.ivCompass) as AppCompatImageView
    }


    interface OnListAdapterClick {
        fun onJobItemClick(position: Int)
    }

}

// Create address from Delivery Object
fun createAddress(address: Address): String {

    return address.street.plus("\n").plus(address.city).plus(", ").plus(address.country)
        .plus("-${address.postalCode}")

}