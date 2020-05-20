package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.repository.Delivery
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.DeliveryStatusViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_job_detail.*


class DeliveryDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var deliveryStatusViewModel: DeliveryStatusViewModel

    private val deliveryPosition: String
        get() = intent?.getStringExtra(KEY_EXTRA_DELIVERY_POS)?:""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)


        val deliveryId = intent?.getStringExtra(KEY_EXTRA_DELIVERY_ID)!!
        deliveryStatusViewModel = (application as MyApplication).injector
            .provideDeliveryStatusViewModel(this.applicationContext, deliveryId)

        deliveryStatusViewModel.delivery.observe(this, Observer { updateView(it) }
        )

        addActionListeners()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap?) {

        p0?.let { map ->
            val latLng = deliveryStatusViewModel.getLatLng()
            val label = deliveryStatusViewModel.getLabel()

            map.addMarker(MarkerOptions().position(latLng).title(label))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
//            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
//                val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Your Current Location")
//                map.addMarker(markerOptions)
//
//            }
            map.uiSettings.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
            }



        }
    }

    private fun updateView(newValue: Delivery) {
        tvCustomerNote.text = newValue.customerNote
        tvAddress.text = newValue.address.street
        if (newValue.deliveryNote != etDeliveryNote.text.toString()) {
            etDeliveryNote.setText(newValue.deliveryNote)
        }
        val completeEnabled = !newValue.isCompleted
        Log.d(TAG, "Complete button enabled is $completeEnabled")
        tvComplete.isEnabled = completeEnabled
        tvComplete.background = getDrawable(
            if (completeEnabled) R.drawable.bg_button
            else R.drawable.bg_button_disabled
        )
        tvComplete.text = if (completeEnabled)
            getString(R.string.mark_completed)
        else getString(R.string.completed)

    }

    private fun addActionListeners() {
        ivBack.setOnClickListener {
            deliveryStatusViewModel.onBackPressed()
            onBackPressed()
        }
        tvComplete.setOnClickListener {
            Log.d(TAG, "Complete button pressed")
            tvComplete.isEnabled = false
            deliveryStatusViewModel.onMarkedCompleted()
        }

        etDeliveryNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) =
                deliveryStatusViewModel.onDeliveryNoteChanged(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }


    override fun onBackPressed() {
        val data = Intent()
        data.data = Uri.parse(deliveryPosition)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {const val TAG = "DeliveryDetailActivity"}

}


