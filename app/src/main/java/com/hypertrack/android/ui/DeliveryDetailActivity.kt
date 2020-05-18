package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.KEY_EXTRA_DELIVERY_ID
import com.hypertrack.android.navigateTo
import com.hypertrack.android.repository.Delivery
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.Injector
import com.hypertrack.android.view_models.DeliveryStatusViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_job_detail.*


private const val CAMERA_REQUEST = 1

class DeliveryDetailActivity : AppCompatActivity(), OnMapReadyCallback {



    private lateinit var deliveryStatusViewModel: DeliveryStatusViewModel


    override fun onMapReady(p0: GoogleMap?) {

        p0?.let { map ->
            val latLng = deliveryStatusViewModel.getLatLng()
            val label = deliveryStatusViewModel.getLabel()

            map.addMarker(MarkerOptions().position(latLng).title(label))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
                val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude)).title("Your Current Location")
                map.addMarker(markerOptions)

            }
            map.uiSettings.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
            }



        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)


        val deliveryId = intent?.getStringExtra(KEY_EXTRA_DELIVERY_ID)!!
        deliveryStatusViewModel = Injector.provideDeliveryStatusViewModel(this.applicationContext, deliveryId)

        deliveryStatusViewModel.delivery.observe(this, Observer { updateView(it) }
        )

        addActionListeners()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)

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
        tvComplete.setBackgroundColor(if (completeEnabled) getColor(R.color.colorBlack) else getColor(R.color.colorBtnDisable))

    }

    private fun addActionListeners() {
        ivBack.setOnClickListener { onBackPressed() }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST) { /* TODO Denys*/ }
    }


    // open camera intent and capture image
    private fun dispatchTakePictureIntent() =
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST)


    override fun onBackPressed() {

        setResult(Activity.RESULT_OK)
        navigateTo(Destination.LIST_VIEW)
    }

    companion object {const val TAG = "DeliveryDetailActivity"}

}


