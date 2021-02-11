package com.hypertrack.android.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hypertrack.android.ui.VisitsManagementActivity
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragment
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_visits_list.*

class VisitsListFragment: Fragment(R.layout.fragment_visits_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = (parentFragment as VisitsManagementFragment).viewAdapter
        }
        srlVisits.setOnRefreshListener {
            this.activity?.let {
                val hostActivity = it as VisitsManagementActivity
                hostActivity.visitsManagementViewModel.refreshVisits {
                    // Log.v(TAG, "refresh visits finished callback")
                    if (srlVisits.isRefreshing) {
                        srlVisits.isRefreshing = false
                    }
                }

            }
        }
    }

    companion object {
        fun newInstance() = VisitsListFragment()
    }
}