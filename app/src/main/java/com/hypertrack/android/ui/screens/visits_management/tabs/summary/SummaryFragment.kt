package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.hypertrack.android.models.Summary
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_summary.*
import kotlinx.android.synthetic.main.progress_bar.*

class SummaryFragment : ProgressDialogFragment(R.layout.fragment_tab_summary) {

    private val adapter = SummaryItemsAdapter()

    private val vm: SummaryViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvSummary.setLinearLayoutManager(requireContext())
        rvSummary.adapter = adapter

        displayLoadingState(true)

        vm.summary.observe(viewLifecycleOwner, { summary ->
            displaySummary(summary)
            displayLoadingState(false)
        })

        vm.error.observe(viewLifecycleOwner, { error ->
            srlSummary.isRefreshing = false
            //todo
            SnackbarUtil.showErrorSnackbar(
                view, /*error.error?.message.toString()*/
                MyApplication.context.getString(R.string.history_error)
            )
        })

        srlSummary.setOnRefreshListener {
            vm.refreshSummary()
        }
    }

    private fun displaySummary(summary: Summary) {
        val items = listOf(
            SummaryItem(
                R.drawable.ic_ht_eta,
                getString(R.string.summary_total_tracking_time),
                DateTimeUtils.secondsToLocalizedString(summary.totalDuration)
            ),
            SummaryItem(
                R.drawable.ic_ht_drive,
                getString(R.string.summary_drive),
                DateTimeUtils.secondsToLocalizedString(summary.totalDriveDuration),
                DistanceUtils.metersToDistanceString(summary.totalDriveDistance)
            ),
            SummaryItem(
                R.drawable.ic_ht_walk, getString(R.string.summary_walk),
                DateTimeUtils.secondsToLocalizedString(summary.totalWalkDuration),
                resources.getString(R.string.steps, summary.stepsCount)
            ),
            SummaryItem(
                R.drawable.ic_ht_stop,
                getString(R.string.summary_stop),
                DateTimeUtils.secondsToLocalizedString(summary.totalStopDuration)
            ),
        )
        adapter.updateItems(items)
    }

    private fun displayLoadingState(isLoading: Boolean) {
        srlSummary.isRefreshing = isLoading
    }

    companion object {
        fun newInstance() = SummaryFragment()
    }

}