package com.hypertrack.android.ui.screens.visits_management.tabs.visits

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.*
import com.hypertrack.android.ui.common.nullIfEmpty
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.inflate_header_item.view.*

// Job adapter (Multiple type jobs Pending,Completed,Visited)
class VisitListAdapter(
        private val visits: LiveData<List<VisitListItem>>,
        onclick: OnListAdapterClick
) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClick: OnListAdapterClick = onclick

    var placeholderListener: ((Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.inflate_header_item -> {
                return HeaderViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.inflate_header_item, parent, false)
                )
            }
            else -> {
                return VisitViewHolder(LayoutInflater.from(parent.context)
                        .inflate(R.layout.inflater_visit_item, parent, false)
                )
            }
        }
    }


    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = visits.value?.get(holder.adapterPosition)) {
            is HeaderVisitItem -> {
                val headerView = holder as HeaderViewHolder
                headerView.tvHeaderText.text = holder.itemView.context.resources
                    .getStringArray(R.array.visit_state_group_names)[item.status.ordinal]
                headerView.itemView.divider.setGoneState(position == 0)
            }
            is Visit -> {
                val visitView = holder as VisitViewHolder
                val address = item.address.toString().nullIfEmpty()
                visitView.tvAddress.text = address
                visitView.tvAddress.setGoneState(address == null)
                visitView.tvDescription.text = item.state.toString().toLowerCase().capitalize()
                visitView.tvTitle.text = item.visit_id
                visitView.ivCompass.visibility = if (item.isVisited) View.VISIBLE else View.GONE
                visitView.ivNoteIcon.visibility = if (item.hasNotes()) View.VISIBLE else View.GONE

                visitView.ivPhotosStateIcon.setGoneState(item.photos.isEmpty())
                when {
                    item.photos.any { it.state == VisitPhotoState.ERROR } -> {
                        visitView.ivPhotosStateIcon.setImageResource(R.drawable.ic_photo_error)
                    }
                    item.photos.any { it.state == VisitPhotoState.NOT_UPLOADED } -> {
                        visitView.ivPhotosStateIcon.setImageResource(R.drawable.ic_photo_loading)
                    }
                    else -> {
                        visitView.ivPhotosStateIcon.setImageResource(R.drawable.ic_photo)
                    }
                }
            }
        }
        holder.itemView.setOnClickListener { onItemClick.onJobItemClick(holder.layoutPosition) }
    }

    override fun getItemCount(): Int {
        val count = visits.value?.size ?: 0
        placeholderListener?.invoke(count == 0)
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return if (visits.value?.get(position) is HeaderVisitItem)
            R.layout.inflate_header_item
        else {
            R.layout.inflater_visit_item
        }

    }

    private inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvHeaderText: TextView = view.findViewById(R.id.tvHeader) as TextView

    }


    inner class VisitViewHolder(holder: View) : RecyclerView.ViewHolder(holder) {

        internal var tvTitle: TextView = holder.findViewById(R.id.tvTitle) as TextView
        internal var tvAddress: TextView = holder.findViewById(R.id.tvAddress) as TextView
        internal var tvDescription: TextView = holder.findViewById(R.id.tvDescription) as TextView
        internal var ivPhotosStateIcon: ImageView =
                holder.findViewById(R.id.ivPhotosStateIcon) as AppCompatImageView
        internal var ivNoteIcon: ImageView =
                holder.findViewById(R.id.ivNote) as AppCompatImageView
        internal var ivCompass: ImageView =
                holder.findViewById(R.id.ivCompass) as AppCompatImageView
    }


    interface OnListAdapterClick {
        fun onJobItemClick(position: Int)
    }

}

// Create address from Visit Object
fun createAddress(address: Address): String {

    return address.street.plus("\n").plus(address.city).plus(", ").plus(address.country)
            .plus("-${address.postalCode}")

}