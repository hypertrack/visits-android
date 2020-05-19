package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hypertrack.android.adapters.DeliveryListAdapter
import com.hypertrack.android.repository.Delivery
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.utils.TrackingStateValue
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
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_job_listing)

        viewAdapter = DeliveryListAdapter(deliveryListViewModel.deliveries, object: DeliveryListAdapter.OnListAdapterClick{
            override fun onJobItemClick(position: Int) {
                Log.d(TAG, "Clicked delivery at position $position")
                val delivery = deliveryListViewModel.deliveries.value?.get(position)
                delivery?.let { if (it is Delivery) showDeliveryDetails(it, position) }
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

        deliveryListViewModel.trackingState.observe(this, Observer { state ->
            val invisible = -1
            val colorId =  when (state) {
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

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.dataString?.toIntOrNull()?.let { position ->
            Log.d(TAG, "Item in pos $position was changed")
            viewAdapter.notifyItemChanged(position)
        }
    }

    private fun showDeliveryDetails(delivery: Delivery, position: Int) = startActivityForResult(
        Intent(this@DeliveryListActivity, DeliveryDetailActivity::class.java)
            .putExtra(KEY_EXTRA_DELIVERY_ID, delivery._id)
            .putExtra(KEY_EXTRA_DELIVERY_POS, position.toString()),
        42
    )

    companion object { const val TAG = "ListActivity" }

}

const val KEY_EXTRA_DELIVERY_ID = "delivery_id"
const val KEY_EXTRA_DELIVERY_POS = "delivery_position"
