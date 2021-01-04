package com.hypertrack.android.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.models.Visit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_visit_detail.*
import java.io.File
import java.io.IOException
import java.util.*


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
            etVisitNote.setText(text)
        }

        listOf(
            viewModel.takePictureButton to tvTakePicture,
            viewModel.pickUpButton to tvPickUp,
            viewModel.checkInButton to tvCheckIn,
            viewModel.checkOutButton to tvCheckOut,
            viewModel.cancelButton to tvCancel,

        ).forEach { (model, view) ->
            view.visibility = if (model.value == true) View.VISIBLE else View.GONE
            view.isEnabled = model.value == true
            model.observe(this) { enabled ->
                view.visibility = if (enabled) View.VISIBLE else View.GONE
                view.isEnabled = enabled
            }
        }

        viewModel.showToast.observe(this) {show ->
            if (show)
                Toast.makeText(this, getString(R.string.vist_note_updated), Toast.LENGTH_LONG).show()
        }

        setActionListeners()
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync(this)

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
        customerNoteGroup.visibility = if (newValue.customerNote.isEmpty()) View.GONE else View.VISIBLE
        val takePictureButtonDisabled = tvTakePicture.visibility == View.GONE
        val hasNoPreview = newValue.icon == null
        val pictureGroupVisitility =
            if (hasNoPreview && takePictureButtonDisabled) View.GONE else View.VISIBLE
        Log.v(TAG, "Picture group visibility is $pictureGroupVisitility")
        visitPreviewGroup.visibility = pictureGroupVisitility
        ivVisitPic.visibility = if (newValue.icon == null) View.GONE else View.VISIBLE
        ivVisitPic.setImageBitmap(newValue.icon)
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

        listOf(
            tvTakePicture to this::dispatchTakePictureIntent,
            tvPickUp to viewModel::onPickUpClicked,
            tvCheckIn to viewModel::onCheckInClicked,
            tvCheckOut to viewModel::onCheckOutClicked,
            tvCancel to viewModel::onCancelClicked
        ).forEach { (view, action) ->
            view.setOnClickListener {
                disableHandlers()
                action()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAG, "Got image capture result $resultCode")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            currentPhotoPath?.let { viewModel.onPictureResult(it) }
        }
    }

    private fun dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .also { takePictureIntent ->
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Toast.makeText(this, getString(R.string.cannot_create_file_msg), Toast.LENGTH_LONG).show()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.hypertrack.logistics.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
    }


    private fun disableHandlers() =
        listOf<View>(
            tvTakePicture,
            tvPickUp,
            tvCheckIn,
            tvCheckOut,
            tvCancel,
            etVisitNote
        ).forEach { it.isEnabled = false }


    override fun onBackPressed() {
        val data = Intent()
        data.data = Uri.parse(visitPosition)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = "${Date().time}"
        val storageDir: File = cacheDir
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        const val TAG = "VisitDetailsActivity"
        const val REQUEST_IMAGE_CAPTURE = 1
    }

}


