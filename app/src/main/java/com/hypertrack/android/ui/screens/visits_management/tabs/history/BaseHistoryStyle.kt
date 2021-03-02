package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.hypertrack.android.models.Status
import com.hypertrack.logistics.android.github.R

class BaseHistoryStyle(private val context: Context) : HistoryStyle {
    override val activeColor: Int
        get() = context.resources.getColor(R.color.colorHistoryActiveSegment, context.theme)
    override val driveSelectionColor: Int
        get() = context.resources.getColor(R.color.colorHistorySelectedSegmentDrive, context.theme)
    override val walkSelectionColor: Int
        get() = context.resources.getColor(R.color.colorHistorySelectedSegmentWalk, context.theme)
    override val stopSelectionColor: Int
        get() = context.resources.getColor(R.color.colorHistorySelectedSegmentStop, context.theme)
    override val outageSelectionColor: Int
        get() = context.resources.getColor(R.color.colorHistoryOutageSegment, context.theme)

    override fun colorForStatus(status: Status): Int =
        when (status) {
            Status.STOP -> stopSelectionColor
            Status.DRIVE -> driveSelectionColor
            Status.WALK -> walkSelectionColor
            Status.OUTAGE -> outageSelectionColor
            else -> activeColor
        }

    override fun markerForStatus(status: Status): Bitmap =
        ResourcesCompat.getDrawable(context.resources, R.drawable.ic_ht_status_marker_boundary, context.theme)!!.toBitmap()
}