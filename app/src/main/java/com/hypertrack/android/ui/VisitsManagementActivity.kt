package com.hypertrack.android.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.adapters.VisitListAdapter
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitStatusGroup
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.TrackingStateValue
import com.hypertrack.android.view_models.StatusString
import com.hypertrack.android.view_models.VisitsManagementViewModel
import com.hypertrack.android.view_models.VisitsStats
import com.hypertrack.logistics.android.github.BuildConfig
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_visits_management.*


class VisitsManagementActivity : ProgressDialogActivity() {

    private val visitsManagementViewModel : VisitsManagementViewModel by viewModels {
        (application as MyApplication).injector.provideVisitsManagementViewModelFactory(applicationContext)
    }
    lateinit var viewAdapter: RecyclerView.Adapter<*>
    lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_visits_management)
        checkInvariants()

        viewAdapter = VisitListAdapter(visitsManagementViewModel.visits, object: VisitListAdapter.OnListAdapterClick{
            override fun onJobItemClick(position: Int) {
                Log.d(TAG, "Clicked visit at position $position")
                val visit = visitsManagementViewModel.visits.value?.get(position)
                visit?.let { if (it is Visit) showVisitDetails(it, position) }
            }
        })
        viewManager = LinearLayoutManager(this)


        visitsManagementViewModel.visits
            .observe(this, {
                visits -> Log.d(TAG, "Got visits $visits")
                viewAdapter.notifyDataSetChanged()
            })

        viewpager.adapter = SimpleFragmentPagerAdapter(
            supportFragmentManager, this, viewAdapter, viewManager,
            visitsManagementViewModel.deviceHistoryWebViewUrl
        )

        sliding_tabs.setupWithViewPager(viewpager)

        visitsManagementViewModel.statusBarColor.observe(this) { color ->
            tvTrackerStatus.visibility = if (color == null) View.GONE else View.VISIBLE
            color?.let { tvTrackerStatus.setBackgroundColor(getColor(it)) }
        }

        visitsManagementViewModel.statusBarMessage.observe(this) { msg ->
            when (msg) {
                is StatusString -> tvTrackerStatus.setText(msg.stringId)
                is VisitsStats -> {
                    if (msg.stats.isEmpty())
                        tvTrackerStatus.setText(R.string.no_planned_visits)
                    else {
                        val groupNames = resources.getStringArray(R.array.visit_state_group_names)
                        val messageText = msg.stats.entries.filter { it.value > 0 }
                            .fold(getString(R.string.empty_string)) { acc, entry ->
                                acc + "${entry.value} ${groupNames[entry.key.ordinal]} ${resources.getQuantityString(R.plurals.item_plurals, entry.value)} "
                            }
                        Log.v(TAG, "Created message text $messageText")
                        tvTrackerStatus.text = messageText
                    }

                }
            }

        }
//
//        visitsManagementViewModel.statusLabel.observe(this) { stateAndLabel ->
//            val invisible = -1
//            val colorId =  when (stateAndLabel.first) {
//                TrackingStateValue.ERROR, TrackingStateValue.DEVICE_DELETED -> R.color.colorTrackingError
//                TrackingStateValue.STOP -> R.color.colorTrackingStopped
//                TrackingStateValue.TRACKING -> {R.color.colorTrackingActive}
//                else -> invisible
//            }
//
//            if (colorId == invisible) {
//                tvTrackerStatus.visibility = View.GONE
//            } else {
//                tvTrackerStatus.visibility = View.VISIBLE
//                tvTrackerStatus.setBackgroundColor(getColor(colorId))
//            }
//
//            tvTrackerStatus.text = stateAndLabel.second
//        }

        visitsManagementViewModel.showSpinner.observe(this) { show ->
            if(show) showProgress() else dismissProgress()
        }
        visitsManagementViewModel.enableCheckIn.observe(this) { enabled ->
            checkIn.isEnabled = enabled
        }
        if (visitsManagementViewModel.showCheckIn)
            checkIn.visibility = View.VISIBLE
        else
            checkIn.visibility = View.GONE
        visitsManagementViewModel.clockInButtonText.observe(this) { clockIn.text = it }
        visitsManagementViewModel.checkInButtonText.observe(this) { checkIn.text = it }

        ivRefresh.setOnClickListener { visitsManagementViewModel.refreshVisits() }
        clockIn.setOnClickListener { visitsManagementViewModel.switchTracking() }
        checkIn.setOnClickListener { visitsManagementViewModel.checkIn() }
        visitsManagementViewModel.showToast.observe(this) { msg ->
            if (msg.isNotEmpty()) Toast
                .makeText(this, msg, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        visitsManagementViewModel.refreshVisits()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent with extras ${intent?.extras}")
        if (intent?.action == Intent.ACTION_SYNC) {
            visitsManagementViewModel.refreshVisits()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")
        data?.dataString?.toIntOrNull()?.let { position ->
            Log.d(TAG, "Item in pos $position was changed")
            viewAdapter.notifyItemChanged(position)
            visitsManagementViewModel.possibleLocalVisitCompletion()
        }
    }

    private fun showVisitDetails(visit: Visit, position: Int) = startActivityForResult(
        Intent(this@VisitsManagementActivity, VisitDetailsActivity::class.java)
            .putExtra(KEY_EXTRA_VISIT_ID, visit._id)
            .putExtra(KEY_EXTRA_VISIT_POS, position.toString()),
        42
    )

    private fun checkInvariants() {
        if (BuildConfig.DEBUG) {
            if (resources.getStringArray(R.array.visit_state_group_names).size != VisitStatusGroup.values().size) {
                error("visit_state_group_names array doesn't contain enough members to represent " +
                        "all the VisitStatusGroup values")
            }
        }
    }

    companion object { const val TAG = "VisitsManagementAct" }

}

const val KEY_EXTRA_VISIT_ID = "delivery_id"
const val KEY_EXTRA_VISIT_POS = "delivery_position"
