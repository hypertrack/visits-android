package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
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
            adapter.updateItems(summary)
            displayLoadingState(false)
        })

        vm.error.observe(viewLifecycleOwner, { error ->
            srlSummary.isRefreshing = false
            SnackbarUtil.showErrorSnackbar(
                view,
                error.error?.message ?: MyApplication.context.getString(R.string.history_error)
            )
        })

        srlSummary.setOnRefreshListener {
            vm.refreshSummary()
        }
    }

    private fun displayLoadingState(isLoading: Boolean) {
        srlSummary.isRefreshing = isLoading
    }

    companion object {
        fun newInstance() = SummaryFragment()
    }

}