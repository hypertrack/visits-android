package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.adapters.VisitListAdapter
import com.hypertrack.android.repository.Visit
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
            .observe(this, Observer {
                visits -> Log.d(TAG, "Got visits $visits")
                viewAdapter.notifyDataSetChanged()
            })

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        visitsManagementViewModel.statusLabel.observe(this, Observer { stateAndLabel ->
            val invisible = -1
            val colorId =  when (stateAndLabel.first) {
                TrackingStateValue.ERROR -> R.color.colorTrackingError
                TrackingStateValue.STOP -> R.color.colorTrackingStopped
                TrackingStateValue.TRACKING -> R.color.colorTrackingActive
                else -> invisible
            }

            if (colorId == invisible) {
                tvTrackerStatus.visibility = View.GONE
            } else {
                tvTrackerStatus.visibility = View.VISIBLE
                tvTrackerStatus.setBackgroundColor(getColor(colorId))
            }

            tvTrackerStatus.text = stateAndLabel.second


        })

        visitsManagementViewModel.showSpinner.observe(this, Observer { show ->
            if(show) showProgress() else dismissProgress()
        })

        visitsManagementViewModel.enableCheckin.observe(this, Observer { enabled ->
            checkIn.isEnabled = enabled
        })

        ivRefresh.setOnClickListener { visitsManagementViewModel.refreshVisits() }
        clockIn.setOnClickListener { visitsManagementViewModel.switchTracking() }
        checkIn.setOnClickListener {
            visitsManagementViewModel.checkin()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.dataString?.toIntOrNull()?.let { position ->
            Log.d(TAG, "Item in pos $position was changed")
            viewAdapter.notifyItemChanged(position)
        }
    }

    private fun showVisitDetails(visit: Visit, position: Int) = startActivityForResult(
        Intent(this@VisitsManagementActivity, VisitDetailsActivity::class.java)
            .putExtra(KEY_EXTRA_VISIT_ID, visit._id)
            .putExtra(KEY_EXTRA_VISIT_POS, position.toString()),
        42
    )

    companion object { const val TAG = "ListActivity" }

}

const val KEY_EXTRA_VISIT_ID = "delivery_id"
const val KEY_EXTRA_VISIT_POS = "delivery_position"
