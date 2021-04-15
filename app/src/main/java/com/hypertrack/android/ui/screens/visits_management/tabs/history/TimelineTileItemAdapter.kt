package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.GeoTagMarker
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.HistoryTileType
import com.hypertrack.android.models.Status
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.logistics.android.github.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.inflater_history_tile_item.view.*

class TimelineTileItemAdapter(
    private val tiles: LiveData<List<HistoryTile>>,
    private val style: TimelineStyle,
    private val onClick: (HistoryTile) -> Unit,
    private val onCopyClickListener: ((HistoryTile) -> Unit)? = null
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
            holder.notch.visibility = View.VISIBLE
        } else if (!tile.isStatusTile) {
            holder.eventIcon.visibility = View.VISIBLE
            holder.eventIcon.setImageResource(style.eventIcon())
            holder.activityIcon.visibility = View.INVISIBLE
            holder.statusStripe.visibility = View.VISIBLE
            holder.activityTimeFrame.visibility = View.VISIBLE
            holder.notch.visibility = View.INVISIBLE
        } else {
            holder.eventIcon.visibility = View.INVISIBLE
            holder.statusStripe.visibility = View.VISIBLE
            holder.activityIcon.visibility = View.VISIBLE
            holder.activityTimeFrame.visibility = View.VISIBLE
            holder.statusStripe.setImageResource(style.statusImageForTile(tile.tileType))
            holder.notch.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener { onClick(tile) }
        holder.containerView.bCopy.setGoneState(
            !(tile.marker is GeoTagMarker
                    && tile.marker.metadataType == GeoTagMarker.TYPE_VISIT_ADDED)
        )
        holder.containerView.bCopy.setOnClickListener {
            onCopyClickListener?.invoke(tile)
        }
    }

    override fun getItemCount(): Int = tiles.value?.size?:0
}

class TimeLineTile(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {
    val activityIcon: AppCompatImageView = containerView.findViewById(R.id.ivActivityIcon)
    val activitySummary: AppCompatTextView = containerView.findViewById(R.id.tvActivitySummary)
    val activityPlace: AppCompatTextView = containerView.findViewById(R.id.tvActivityPlace)
    val activityTimeFrame: AppCompatTextView = containerView.findViewById(R.id.tvTimeframe)
    val statusStripe: AppCompatImageView = containerView.findViewById(R.id.ivStatusStripe)
    val eventIcon: AppCompatImageView = containerView.findViewById(R.id.ivEventIcon)
    val notch: View = containerView.findViewById(R.id.notch)
}

interface TimelineStyle {
    @DrawableRes fun summaryIcon(): Int
    @DrawableRes fun eventIcon(): Int
    @DrawableRes fun iconForStatus(status: Status):  Int
    @DrawableRes fun statusImageForTile(type: HistoryTileType): Int
    @ColorInt fun textColorForType(tileType: HistoryTileType): Int
    val summaryPeekHeight: Int
}