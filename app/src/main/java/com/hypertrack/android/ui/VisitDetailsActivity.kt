package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.models.Visit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.ButtonLabel
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_visit_detail.*


class VisitDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: VisitDetailsViewModel

    private val visitPosition: String
        get() = intent?.getStringExtra(KEY_EXTRA_VISIT_POS)?:""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_detail)


        val visitId = intent?.getStringExtra(KEY_EXTRA_VISIT_ID)!!
        viewModel = (application as MyApplication).injector
            .provideVisitStatusViewModel(this.applicationContext, visitId)

        viewModel.visit.observe(this) { updateView(it) }

        viewModel.visitNote.observe(this) { (text, isEditable) ->
            Log.v(TAG, "visitNote text $text isEditable $isEditable")
            etVisitNote.isEnabled = isEditable
            text.let {  }
            etVisitNote.setText(text)
        }

        viewModel.upperButton.observe(this) { (text, isEnabled) ->
            Log.v(TAG, "upperButton button label $text isEnabled $isEnabled")
            setButtonText(tvPickUp, text)
            setButtonEnabled(tvPickUp, isEnabled)
        }

        viewModel.lowerButton.observe(this) { (text, isEnabled) ->
            Log.v(TAG, "lowerButton button label $text isEnabled $isEnabled")
            setButtonText(tvCheckIn, text)
            setButtonEnabled(tvCheckIn, isEnabled)
        }

        viewModel.showToast.observe(this) {show ->
            if (show)
                Toast.makeText(this, getString(R.string.vist_note_updated), Toast.LENGTH_LONG).show()
        }

        setActionListeners()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)

    }

    private fun setButtonEnabled(button: AppCompatTextView, enabled: Boolean) {
        button.isEnabled = enabled
        button.background = ContextCompat.getDrawable(this,
            if (enabled) R.drawable.bg_button
            else R.drawable.bg_button_disabled
        )
    }

    private fun setButtonText(button: AppCompatTextView, text: ButtonLabel) {
        when (text) {
            ButtonLabel.PICK_UP -> button.setText(R.string.pick_up)
            ButtonLabel.CHECK_OUT -> button.setText(R.string.check_out)
            ButtonLabel.CANCEL -> button.setText(R.string.cancel)
            ButtonLabel.CHECK_IN -> button.setText(R.string.check_in)
        }
    }

    override fun onMapReady(p0: GoogleMap?) {

        p0?.let { map ->
            map.uiSettings.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
            }
            val latLng = viewModel.getLatLng() ?: return
            Log.d(TAG, "Got latlng $latLng")
            val label = viewModel.getLabel()

            map.addMarker(MarkerOptions().position(latLng).title(label))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
        }
    }

    private fun updateView(newValue: Visit) {
        Log.v(TAG, "updated view with value $newValue")
        tvCustomerNote.text = newValue.customerNote
        tvAddress.text = newValue.address.street
        if (newValue.visitNote != etVisitNote.text.toString()) {
            etVisitNote.setText(newValue.visitNote)
        }

    }

    private fun setActionListeners() {
        ivBack.setOnClickListener {
            viewModel.onBackPressed()
            onBackPressed()
        }

        etVisitNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) =
                viewModel.onVisitNoteChanged(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        tvPickUp.setOnClickListener {
            Log.d(TAG, "Upper button pressed")
            // stop interactions to avoid simultaneous Complete & Cancel click
            tvPickUp.isEnabled = false
            etVisitNote.isEnabled = false
            tvCheckIn.isEnabled = false
            viewModel.onUpperButtonClicked()
        }

        tvCheckIn.setOnClickListener {
            Log.d(TAG, "Lower button pressed")
            // stop interactions to avoid simultaneous Complete & Cancel click
            tvPickUp.isEnabled = false
            etVisitNote.isEnabled = false
            tvCheckIn.isEnabled = false
            viewModel.onLowerButtonClicked()
        }
    }


    override fun onBackPressed() {
        val data = Intent()
        data.data = Uri.parse(visitPosition)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {const val TAG = "VisitDetailsActivity"}

}


