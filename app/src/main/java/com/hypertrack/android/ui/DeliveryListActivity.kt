package com.hypertrack.android.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

    private var dialog: Dialog? = null

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

        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }

        ivRefresh.setOnClickListener { deliveryListViewModel.rerfeshDeliveries() }

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
        deliveryListViewModel.showSpinner.observe(this, Observer { show ->
            if(show) showProgress() else dismissProgress()
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

    private fun showProgress() {

        val newDialog = dialog ?: Dialog(this)
        newDialog.setCancelable(false)
        newDialog.setContentView(R.layout.dialog_progress_bar)
        newDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        newDialog.show()

        dialog = newDialog
    }

    private fun dismissProgress() = dialog?.dismiss()

    companion object { const val TAG = "ListActivity" }

}

const val KEY_EXTRA_DELIVERY_ID = "delivery_id"
const val KEY_EXTRA_DELIVERY_POS = "delivery_position"
