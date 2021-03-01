package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.content.Context
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
}