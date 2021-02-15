package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.hypertrack.android.models.Summary
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.DateTimeUtils
import com.hypertrack.android.ui.common.DistanceUtils
import com.hypertrack.android.ui.common.setLinearLayoutManager
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_summary.*

class SummaryFragment : ProgressDialogFragment(R.layout.fragment_tab_summary) {

    private val adapter = SummaryItemsAdapter()

    private val vm: SummaryViewModel by viewModels {
        MyApplication.injector.provideSummaryViewModelFactory(MyApplication.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSummary.setLinearLayoutManager(requireContext())
        rvSummary.adapter = adapter

        vm.summary.observe(viewLifecycleOwner, { summary ->
            displaySummary(summary)
        })

        vm.loadingState.observe(viewLifecycleOwner, { isLoading ->
            if (isLoading) {
                showProgress()
            } else {
                dismissProgress()
            }
        })

        vm.init()

    }

    private fun displaySummary(summary: Summary) {
        val items = listOf(
                SummaryItem(
                        R.drawable.ic_time,
                        getString(R.string.summary_total_tracking_time),
                        DateTimeUtils.secondsToLocalizedString(summary.totalDuration)
                ),
                SummaryItem(
                        R.drawable.ic_car,
                        getString(R.string.summary_drive),
                        DateTimeUtils.secondsToLocalizedString(summary.totalDriveDuration),
                        DistanceUtils.metersToDistanceString(summary.totalDriveDistance)
                ),
                SummaryItem(
                        R.drawable.ic_walk, getString(R.string.summary_walk),
                        DateTimeUtils.secondsToLocalizedString(summary.totalWalkDuration),
                        resources.getString(R.string.steps, summary.stepsCount)
                ),
                SummaryItem(
                        R.drawable.ic_stop,
                        getString(R.string.summary_stop),
                        DateTimeUtils.secondsToLocalizedString(summary.totalStopDuration)
                ),
        )
        adapter.updateItems(items)
    }

    companion object {
        fun newInstance() = SummaryFragment()
    }

}