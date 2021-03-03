package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.HistoryTileType
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
        holder.activitySummary.setTextColor(style.textColorForType(tile.tileType))
        holder.activityTimeFrame.text = tile.timeframe
        if (tile.address != null) {
            holder.activityPlace.text = tile.address
            holder.activityPlace.visibility = View.VISIBLE
        } else {
            holder.activityPlace.visibility = View.GONE
        }
        if (tile.tileType == HistoryTileType.SUMMARY) {
            holder.eventIcon.visibility = View.VISIBLE
            holder.eventIcon.setImageResource(style.summaryIcon())
            holder.activityIcon.visibility  = View.INVISIBLE
            holder.statusStripe.visibility  = View.INVISIBLE
            holder.activityTimeFrame.visibility = View.GONE
        } else {
            holder.eventIcon.visibility = View.INVISIBLE
            holder.statusStripe.visibility  = View.VISIBLE
            holder.activityIcon.visibility  = View.VISIBLE
            holder.activityTimeFrame.visibility = View.VISIBLE
            holder.statusStripe.setImageResource(style.statusImageForTile(tile.tileType))
        }
        holder.itemView.setOnClickListener { onClick(tile) }
    }

    override fun getItemCount(): Int = tiles.value?.size?:0
}

class TimeLineTile(holder: View) : RecyclerView.ViewHolder(holder) {
    val activityIcon: AppCompatImageView = holder.findViewById(R.id.ivActivityIcon)
    val activitySummary: AppCompatTextView = holder.findViewById(R.id.tvActivitySummary)
    val activityPlace: AppCompatTextView = holder.findViewById(R.id.tvActivityPlace)
    val activityTimeFrame: AppCompatTextView  = holder.findViewById(R.id.tvTimeframe)
    val statusStripe: AppCompatImageView  = holder.findViewById(R.id.ivStatusStripe)
    val eventIcon: AppCompatImageView  = holder.findViewById(R.id.ivEventIcon)
}

interface TimelineStyle {
    @DrawableRes fun summaryIcon(): Int
    @DrawableRes fun iconForStatus(status: Status):  Int
    @DrawableRes fun statusImageForTile(type: HistoryTileType): Int
    @ColorInt fun textColorForType(tileType: HistoryTileType): Int
}