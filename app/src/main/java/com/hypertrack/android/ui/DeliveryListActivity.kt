package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.DELIVERY_UPDATE_RESULT_CODE
import com.hypertrack.android.KEY_EXTRA_DELIVERY_ID
import com.hypertrack.android.adapters.DeliveryListAdapter
import com.hypertrack.android.repository.Delivery
import com.hypertrack.android.showProgressBar
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.view_models.DeliveryListViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_job_listing.*


class DeliveryListActivity : AppCompatActivity() {

    private val deliveryListViewModel : DeliveryListViewModel by viewModels {
        Injector.provideListActivityViewModelFactory(applicationContext)
    }
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_listing)

        viewAdapter = DeliveryListAdapter(deliveryListViewModel.deliveries, object: DeliveryListAdapter.OnListAdapterClick{
            override fun onJobItemClick(position: Int) {
                Log.d(TAG, "Clicked delivery at position $position")
                val delivery = deliveryListViewModel.deliveries.value?.get(position)
                delivery?.let { if (it is Delivery) showDeliveryDetails(it) }
            }
        })

        viewManager = LinearLayoutManager(this)


        deliveryListViewModel.deliveries
            .observe(this, Observer {
                deliveries -> Log.d(TAG, "Got deliveries $deliveries")
                viewAdapter.notifyDataSetChanged()
            })

        val rV = recyclerView
        rV.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

    }


    // Implement click from adapter class
    private fun showDeliveryDetails(delivery: Delivery) {

                startActivityForResult(
                    Intent(this@DeliveryListActivity, DeliveryDetailActivity::class.java)
                        .putExtra(KEY_EXTRA_DELIVERY_ID, delivery._id),
                    DELIVERY_UPDATE_RESULT_CODE
                )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == DELIVERY_UPDATE_RESULT_CODE) {
            showProgressBar()
            // TODO Denys - refetch deliveries ??
        }
    }

    private fun makeTrackerStatus() {

        var createStatusText = ""

        val isTracking = false

        if (isTracking) {
            createStatusText = "Location tracking is active"

            tvTrackerStatus.setBackgroundColor(
                ContextCompat.getColor(this, R.color.colorListStatus))

        } else {
            createStatusText = "Location tracking is inactive"

            tvTrackerStatus.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }

    }

    companion object {const val TAG = "ListActivity"}

}

