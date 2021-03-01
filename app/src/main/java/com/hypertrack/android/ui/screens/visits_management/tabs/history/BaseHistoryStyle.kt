package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.content.Context
import com.hypertrack.android.models.Status
import com.hypertrack.logistics.android.github.R

class BaseHistoryStyle(private val context: Context) : HistoryStyle {
    override val activeColor: Int
        get() = context.resources.getInteger(R.integer.colorHistoryActiveSegment)
    override val driveSelectionColor: Int
        get() = context.resources.getInteger(R.integer.colorHistorySelectedSegmentDrive)
    override val walkSelectionColor: Int
        get() = context.resources.getInteger(R.integer.colorHistorySelectedSegmentWalk)
    override val stopSelectionColor: Int
        get() = context.resources.getInteger(R.integer.colorHistorySelectedSegmentStop)
    override val outageSelectionColor: Int
        get() = context.resources.getInteger(R.integer.colorHistoryOutageSegment)

    override fun colorForStatus(status: Status): Int =
        when (status) {
            Status.STOP -> stopSelectionColor
            Status.DRIVE -> driveSelectionColor
            Status.WALK -> walkSelectionColor
            Status.OUTAGE -> outageSelectionColor
            else -> activeColor
        }
}