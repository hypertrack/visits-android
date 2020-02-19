package com.hypertrack.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.TYPE_HEADER
import com.hypertrack.android.TYPE_ITEM
import com.hypertrack.android.convertSeverDateToTime
import com.hypertrack.android.createAddress
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.response.HeaderItem
import com.hypertrack.logistics.android.github.R

// Job adapter (Multiple type jobs Pending,Completed,Visited)
class JobListAdapters(context: Context, onclick: OnListAdapterClick) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClick: OnListAdapterClick = onclick

    private var layoutInflater: LayoutInflater? = null

    private var currentList: ArrayList<Any>? = arrayListOf()

    init {

        layoutInflater = LayoutInflater.from(context)
    }

    fun updateList(mList: ArrayList<Any>) {

        this.currentList = mList

        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {

            TYPE_ITEM -> {

                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.inflater_completed_item, parent, false)

                return CompletedViewHolder(itemView)

            }
            TYPE_HEADER -> {

                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.inflate_header_item, parent, false)

                return HeaderViewHolder(itemView)
            }
            else -> null!!
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = currentList!![holder.adapterPosition]

        if (holder is HeaderViewHolder) {

            if (item is HeaderItem)
                holder.tvHeaderText.text = item.text
        }
//        } else if (holder is PendingViewHolder) {
//
//            holder.tvTitle.text = (item as Deliveries).label
//            holder.tvDescription.text = createAddress(item.address)
//
//
//            if (!item.enteredAt.isNullOrEmpty() && item.exitedAt.isNullOrEmpty()) {
//                holder.ivLocationIcon.visibility = View.VISIBLE
//            } else {
//                holder.ivLocationIcon.visibility = View.GONE
//            }
//            holder.itemView.setOnClickListener {
//                onItemClick.onJobItemClick(holder.adapterPosition, "pending")
//            }
//
//            // Show Image based on image
//            if (!item.deliveryPicture.isNullOrEmpty()) {
//                holder.ivCameraIcon.visibility = View.VISIBLE
//            } else {
//                holder.ivCameraIcon.visibility = View.GONE
//            }
//
//            // show delivery note icon based on delivery note
//            if (!item.deliveryNote.isNullOrEmpty()) {
//                holder.ivNoteIcon.visibility = View.VISIBLE
//            } else {
//                holder.ivNoteIcon.visibility = View.GONE
//            }
//
//        } else if (holder is VisitedViewHolder) {
//
//            holder.tvTitle.text = (item as Deliveries).label
//
//            // Show Image based on image
//            if (!item.deliveryPicture.isNullOrEmpty()) {
//                holder.ivPicture.visibility = View.VISIBLE
//            } else {
//                holder.ivPicture.visibility = View.GONE
//            }
//
//            // show delivery note icon based on delivery note
//            if (!item.deliveryNote.isNullOrEmpty()) {
//                holder.ivNote.visibility = View.VISIBLE
//            } else {
//                holder.ivNote.visibility = View.GONE
//            }
//            if (!item.enteredAt.isNullOrEmpty() && !item.exitedAt.isNullOrEmpty()) {
//                holder.tvDescription.text = convertSeverDateToTime(item.enteredAt).plus(" - ")
//                    .plus(convertSeverDateToTime(item.exitedAt))
//
//            } else {
//                holder.tvDescription.text = convertSeverDateToTime(item.enteredAt)
//
//            }
//            if (!item.enteredAt.isNullOrEmpty() && item.exitedAt.isNullOrEmpty()) {
//                holder.ivCompass.visibility = View.VISIBLE
//            } else {
//                holder.ivCompass.visibility = View.GONE
//            }
//
//            holder.itemView.setOnClickListener {
//                onItemClick.onJobItemClick(holder.adapterPosition, "visited")
//            }
//        }
        else if (holder is CompletedViewHolder) {

            holder.tvTitle.text = (item as Deliveries).label
            if (item.status == "completed") {
                holder.tvDescription.text =
                    "Completed at " + convertSeverDateToTime(item.completedAt)
            } else {
                if (!item.enteredAt.isNullOrEmpty() && !item.exitedAt.isNullOrEmpty()) {
                    holder.tvDescription.text = "Visited at ".plus(
                        convertSeverDateToTime(item.enteredAt).plus(" - ")
                            .plus(convertSeverDateToTime(item.exitedAt))
                    )

                } else if (!item.enteredAt.isNullOrEmpty()) {
                    holder.tvDescription.text =
                        "Visited at ".plus(convertSeverDateToTime(item.enteredAt))

                } else {

                    holder.tvDescription.text = createAddress(item.address)
                }

            }

            if (!item.deliveryPicture.isNullOrEmpty()) {
                holder.ivCameraIcon.visibility = View.VISIBLE
            } else {
                holder.ivCameraIcon.visibility = View.GONE
            }

            // show delivery note icon based on delivery note
            if (!item.deliveryNote.isNullOrEmpty()) {
                holder.ivNoteIcon.visibility = View.VISIBLE
            } else {
                holder.ivNoteIcon.visibility = View.GONE
            }

            if (!item.enteredAt.isNullOrEmpty() && item.exitedAt.isNullOrEmpty() && item.completedAt.isNullOrEmpty()) {
                holder.ivCompass.visibility = View.VISIBLE
            } else {
                holder.ivCompass.visibility = View.GONE
            }


            holder.itemView.setOnClickListener {
                onItemClick.onJobItemClick(holder.adapterPosition)
            }

        }

    }

    override fun getItemCount(): Int {
        return currentList?.size!!
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList!![position] is HeaderItem)
            TYPE_HEADER
        else {
            TYPE_ITEM
        }

    }

    private inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var tvHeaderText: TextView

        init {
            tvHeaderText = view.findViewById(R.id.tvHeader) as TextView
        }
    }


    inner class CompletedViewHolder(holder: View) : RecyclerView.ViewHolder(holder) {

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