package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.Status
import com.hypertrack.logistics.android.github.R

class TimelineTileItemAdapter(
    private val tiles: LiveData<List<HistoryTile>>,
    private val style: TimelineStyle,
    private val onClick: (HistoryTile) -> Unit
) : RecyclerView.Adapter<TimeLineTile>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineTile {
        return TimeLineTile(LayoutInflater.from(parent.context)
            .inflate(R.layout.inflater_history_tile_item, parent, false))
    }

    override fun onBindViewHolder(holder: TimeLineTile, position: Int) {
        val tile = tiles.value?.get(position)?:return
        holder.activityIcon.setImageResource(style.iconForStatus(tile.status))
        holder.activitySummary.text = tile.description
        if (tile.address != null) {
            holder.activityPlace.text = tile.address
            holder.activityPlace.visibility = View.VISIBLE
        } else {
            holder.activityPlace.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(tile) }
    }

    override fun getItemCount(): Int = tiles.value?.size?:0
}

class TimeLineTile(holder: View) : RecyclerView.ViewHolder(holder) {
    val activityIcon: AppCompatImageView = holder.findViewById(R.id.ivActivityIcon)
    val activitySummary: AppCompatTextView = holder.findViewById(R.id.tvActivitySummary)
    val activityPlace: AppCompatTextView = holder.findViewById(R.id.tvActivityPlace)
}

interface TimelineStyle {
    @DrawableRes fun iconForStatus(status: Status):  Int
}