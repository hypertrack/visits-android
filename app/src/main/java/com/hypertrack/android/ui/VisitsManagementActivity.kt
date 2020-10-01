package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.adapters.VisitListAdapter
import com.hypertrack.android.models.Visit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.TrackingStateValue
import com.hypertrack.android.view_models.VisitsManagementViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_visits_management.*


class VisitsManagementActivity : ProgressDialogActivity() {

    private val visitsManagementViewModel : VisitsManagementViewModel by viewModels {
        (application as MyApplication).injector.provideVisitsManagementViewModelFactory(applicationContext)
    }
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_visits_management)

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

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        visitsManagementViewModel.statusLabel.observe(this) { stateAndLabel ->
            val invisible = -1
            val colorId =  when (stateAndLabel.first) {
                TrackingStateValue.ERROR -> R.color.colorTrackingError
                TrackingStateValue.STOP -> R.color.colorTrackingStopped
                TrackingStateValue.TRACKING -> {R.color.colorTrackingActive}
                else -> invisible
            }

            if (colorId == invisible) {
                tvTrackerStatus.visibility = View.GONE
            } else {
                tvTrackerStatus.visibility = View.VISIBLE
                tvTrackerStatus.setBackgroundColor(getColor(colorId))
            }

            tvTrackerStatus.text = stateAndLabel.second


        }
        visitsManagementViewModel.showSpinner.observe(this) { show ->
            if(show) showProgress() else dismissProgress()
        }
        visitsManagementViewModel.enableCheckIn.observe(this) { enabled ->
            checkIn.isEnabled = enabled
        }
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

    companion object { const val TAG = "VisitsManagementAct" }

}

const val KEY_EXTRA_VISIT_ID = "delivery_id"
const val KEY_EXTRA_VISIT_POS = "delivery_position"
