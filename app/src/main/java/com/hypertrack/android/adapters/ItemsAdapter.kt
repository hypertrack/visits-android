package com.hypertrack.android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.response.Items
import com.hypertrack.logistics.android.github.R

// Job lists item adapters
class ItemsAdapter(context: Context, onclick: OnScanListItemClick) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


      private var onItemClick: OnScanListItemClick? = null

    private var layoutInflater: LayoutInflater? = null

    private var itemsList: ArrayList<Items>

    init {

        this.itemsList = arrayListOf()

        this.onItemClick = onclick

        layoutInflater = LayoutInflater.from(context)
    }

    fun updateList(list: ArrayList<Items>) {

        itemsList = list

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_scan_item, parent, false)

        return ItemViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ItemViewHolder) {

            holder.tvItemId?.text = "#".plus(itemsList[holder.adapterPosition].item_id)
        }

    }

    override fun getItemCount(): Int {
        return itemsList.size
    }


    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvItemId: TextView? = null

        init {

            tvItemId = view.findViewById(R.id.tvItemId)
        }
    }


    interface OnScanListItemClick {

        fun onScanClick(position: Int, type: String = "")
    }

}