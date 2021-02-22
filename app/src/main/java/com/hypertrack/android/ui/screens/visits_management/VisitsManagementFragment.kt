package com.hypertrack.android.ui.screens.visits_management

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.adapters.VisitListAdapter
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatusGroup
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.ui.screens.visits_management.tabs.history.MapViewFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.profile.ProfileFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.summary.SummaryFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.visits.VisitsListFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.BuildConfig
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_visits_management.*

class VisitsManagementFragment() : ProgressDialogFragment(R.layout.fragment_visits_management) {

    val visitsManagementViewModel: VisitsManagementViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInvariants()

        viewAdapter = VisitListAdapter(
                visitsManagementViewModel.visits,
                object : VisitListAdapter.OnListAdapterClick {
                    override fun onJobItemClick(position: Int) {
                        // Log.d(TAG, "Clicked visit at position $position")
                        val visit = visitsManagementViewModel.visits.value?.get(position)
                        visit?.let {
                            if (it is Visit) {
                                findNavController().navigate(
                                        VisitsManagementFragmentDirections.actionVisitManagementFragmentToVisitDetailsFragment(
                                                it._id
                                        )
                                )
                            }
                        }
                    }
                }
        )

        visitsManagementViewModel.visits.observe(viewLifecycleOwner, { visits ->
            // Log.d(TAG, "Got visits $visits")
            viewAdapter.notifyDataSetChanged()
        })

        viewpager.adapter = object :
                FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            private val fragments = listOf(
                    VisitsListFragment.newInstance(),
                    MapViewFragment(),
                    SummaryFragment.newInstance(),
                    ProfileFragment()
            )
            private val tabTitles = resources.getStringArray(R.array.tab_names)

            init {
                check(tabTitles.size == fragments.size)
            }

            override fun getCount(): Int = fragments.size

            override fun getItem(position: Int): Fragment = fragments[position]

            override fun getPageTitle(position: Int): CharSequence? = tabTitles[position]

        }

        sliding_tabs.setupWithViewPager(viewpager)

        visitsManagementViewModel.statusBarColor.observe(viewLifecycleOwner) { color ->
            tvTrackerStatus.visibility = if (color == null) View.GONE else View.VISIBLE
            color?.let { tvTrackerStatus.setBackgroundColor(requireContext().getColor(it)) }
        }

        visitsManagementViewModel.statusBarMessage.observe(viewLifecycleOwner) { msg ->
            when (msg) {
                is StatusString -> tvTrackerStatus.setText(msg.stringId)
                is VisitsStats -> {
                    if (msg.stats.isEmpty())
                        tvTrackerStatus.setText(R.string.no_planned_visits)
                    else {
                        val groupNames = resources.getStringArray(R.array.visit_state_group_names)
                        val messageText = msg.stats.entries.filter { it.value > 0 }
                                .fold(getString(R.string.empty_string)) { acc, entry ->
                                    acc + "${entry.value} ${groupNames[entry.key.ordinal]} "
                                }
                        // Log.v(TAG, "Created message text $messageText")
                        tvTrackerStatus.text = messageText
                    }

                }
            }

        }

        visitsManagementViewModel.showSpinner.observe(viewLifecycleOwner) { show ->
            if (show) showProgress() else dismissProgress()
        }
        visitsManagementViewModel.showSync.observe(viewLifecycleOwner) { show ->
            if (show) showSyncNotification() else dismissSyncNotification()
        }
        visitsManagementViewModel.enableCheckIn.observe(viewLifecycleOwner) { enabled ->
            checkIn.isEnabled = enabled
        }
        if (visitsManagementViewModel.showCheckIn)
            checkIn.visibility = View.VISIBLE
        else
            checkIn.visibility = View.GONE
        visitsManagementViewModel.clockInButtonText.observe(viewLifecycleOwner) {
            clockIn.text = it
        }
        visitsManagementViewModel.isTracking.observe(viewLifecycleOwner) { isTracking ->
            tvClockHint.setText(if (isTracking) {
                R.string.clock_hint_tracking_on
            } else {
                R.string.clock_hint_tracking_off
            })
        }
        visitsManagementViewModel.checkInButtonText.observe(viewLifecycleOwner) {
            checkIn.text = it
        }

        clockIn.setOnClickListener { visitsManagementViewModel.switchTracking() }
        checkIn.setOnClickListener { visitsManagementViewModel.checkIn() }
        visitsManagementViewModel.showToast.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) Toast
                    .makeText(requireContext(), msg, Toast.LENGTH_LONG)
                    .show()
        }

        visitsManagementViewModel.error.observe(viewLifecycleOwner, { error ->
            SnackbarUtil.showErrorSnackbar(view, error.toString())
        })

        //moved from onActivityResult
        visitsManagementViewModel.possibleLocalVisitCompletion()

        visitsManagementViewModel.refreshHistory()
    }

    lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onResume() {
        super.onResume()
        refreshVisits()
    }

    fun refreshVisits() {
        visitsManagementViewModel.refreshVisits { }
    }

    private fun checkInvariants() {
        if (BuildConfig.DEBUG) {
            if (resources.getStringArray(R.array.visit_state_group_names).size != VisitStatusGroup.values().size) {
                error(
                        "visit_state_group_names array doesn't contain enough members to represent " +
                                "all the VisitStatusGroup values"
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Log.d(TAG, "onPause")
        visitsManagementViewModel.showSync.value?.let {
            if (it) {
                dismissSyncNotification()
            }
        }
    }

    companion object {
        const val TAG = "VisitsManagementAct"
        const val KEY_EXTRA_VISIT_ID = "delivery_id"
        const val KEY_EXTRA_VISIT_POS = "delivery_position"
    }

}

