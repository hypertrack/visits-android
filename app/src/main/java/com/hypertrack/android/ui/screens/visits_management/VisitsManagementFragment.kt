package com.hypertrack.android.ui.screens.visits_management

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatusGroup
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.ui.screens.visits_management.tabs.history.MapViewFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.history.MapViewFragmentOld
import com.hypertrack.android.ui.screens.visits_management.tabs.places.PlacesFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.profile.ProfileFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.summary.SummaryFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.visits.VisitListAdapter
import com.hypertrack.android.ui.screens.visits_management.tabs.visits.VisitsListFragment
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.BuildConfig
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_visits_management.*

class VisitsManagementFragment() : ProgressDialogFragment(R.layout.fragment_visits_management) {

    private val args: VisitsManagementFragmentArgs by navArgs()

    val visitsManagementViewModel: VisitsManagementViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }

    private val tabs = mapOf(
        Tab.MAP to MapViewFragment(),
        Tab.ORDERS to VisitsListFragment.newInstance(),
        Tab.PLACES to PlacesFragment.getInstance(),
        Tab.SUMMARY to SummaryFragment.newInstance(),
        Tab.PROFILE to ProfileFragment()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInvariants()

        visitsManagementViewModel.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        viewAdapter = VisitListAdapter(
            visitsManagementViewModel.visits,
            object : VisitListAdapter.OnListAdapterClick {
                override fun onJobItemClick(position: Int) {
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

        visitsManagementViewModel.visits.observe(viewLifecycleOwner, {
            viewAdapter.notifyDataSetChanged()
            viewAdapter.placeholderListener?.invoke(it.isEmpty())
        })

        viewpager.adapter = object :
            FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            override fun getCount(): Int = tabs.size

            override fun getItem(position: Int): Fragment {
                val fragment = tabs.getValue(Tab.values()[position])
                if (fragment is MapViewFragmentOld) {
                    fragment.arguments = Bundle().apply {
                        putString(
                            MapViewFragmentOld.WEBVIEW_URL,
                            visitsManagementViewModel.deviceHistoryWebUrl
                        )
                    }
                }
                return fragment
            }
        }

        sliding_tabs.setupWithViewPager(viewpager)
        for (i in 0 until sliding_tabs.tabCount) {
            sliding_tabs.getTabAt(i)?.icon =
                ResourcesCompat.getDrawable(
                    resources,
                    Tab.values()[i].iconRes,
                    requireContext().theme
                )
        }

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
                        val messageText = msg.stats.entries
                            .filter { it.value > 0 }
                            .map { entry -> "${entry.value} ${groupNames[entry.key.ordinal]}" }
                            .joinToString(", ")
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
            tvClockHint.setText(
                if (isTracking) {
                    R.string.clock_hint_tracking_on
                } else {
                    R.string.clock_hint_tracking_off
                }
            )
        }
        visitsManagementViewModel.checkInButtonText.observe(viewLifecycleOwner) { label ->
            checkIn.text = when (label) {
                LocalVisitCtaLabel.CHECK_OUT -> getString(R.string.check_out)
                else -> getString(R.string.check_in)
            }
        }

        clockIn.setOnClickListener { visitsManagementViewModel.switchTracking() }
        checkIn.setOnClickListener { visitsManagementViewModel.checkIn() }
        visitsManagementViewModel.showToast.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) Toast
                .makeText(requireContext(), msg, Toast.LENGTH_LONG)
                .show()
        }

        visitsManagementViewModel.error.observe(viewLifecycleOwner, { error ->
            SnackbarUtil.showErrorSnackbar(view, error)
        })

        //moved from onActivityResult
        visitsManagementViewModel.possibleLocalVisitCompletion()

        visitsManagementViewModel.refreshHistory()

        if (args.tab != -1 && args.tab < viewpager.adapter!!.count) {
            viewpager.currentItem = args.tab
        }
    }

    lateinit var viewAdapter: VisitListAdapter

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
        visitsManagementViewModel.showSync.value?.let {
            if (it) {
                dismissSyncNotification()
            }
        }
    }

    companion object {
        const val TAG = "VisitsManagementAct"

    }

    enum class Tab(@DrawableRes val iconRes: Int) {
        MAP(R.drawable.ic_map_tab),
        ORDERS(R.drawable.ic_visits_list_tab),
        PLACES(R.drawable.ic_places),

        //        TIMELINE(R.drawable.,
        SUMMARY(R.drawable.ic_insights_tab),
        PROFILE(R.drawable.ic_profile_tab),
    }
}

