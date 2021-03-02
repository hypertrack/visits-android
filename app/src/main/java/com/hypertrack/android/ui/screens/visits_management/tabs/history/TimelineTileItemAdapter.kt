package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.logistics.android.github.R

class TimelineTileItemAdapter(
    private val tiles: LiveData<List<TimeLineTile>>,
    private val style: HistoryStyle
) : RecyclerView.Adapter<TimeLineTile>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineTile {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: TimeLineTile, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = tiles.value?.size?:0
}

class TimeLineTile(holder: View) : RecyclerView.ViewHolder(holder) {
    val activityIcon: AppCompatImageView = holder.findViewById(R.id.ivActivityIcon)
    val activitySummary: AppCompatTextView = holder.findViewById(R.id.tvActivitySummary)
    val activityPlace: AppCompatTextView = holder.findViewById(R.id.tvActivityPlace)
}