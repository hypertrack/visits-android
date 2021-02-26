package com.hypertrack.android.ui.screens.visit_details

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.models.Visit
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_visit_detail.*
import java.io.File
import java.io.IOException
import java.util.*


class VisitDetailsFragment : ProgressDialogFragment(R.layout.fragment_visit_detail) {

    private val args: VisitDetailsFragmentArgs by navArgs()

    private lateinit var viewModel: VisitDetailsViewModel

    private lateinit var cancelDialog: AlertDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val visitId = args.visitId
        viewModel = MyApplication.injector
                .provideVisitStatusViewModel(MyApplication.context, visitId)

        viewModel.visit.observe(viewLifecycleOwner) { updateView(it) }

        viewModel.visitNote.observe(viewLifecycleOwner) { (text, isEditable) ->
            // Log.v(TAG, "visitNote text $text isEditable $isEditable")
            etVisitNote.isEnabled = isEditable
            etVisitNote.setText(text)
        }

        viewModel.pickUpButton.observe(viewLifecycleOwner, { visible ->
            tvPickUp.setGoneState(!visible)
            divider.setGoneState(!visible)
        })

        viewModel.takePictureButton.observe(viewLifecycleOwner, { visible ->
            tvTakePicture.setGoneState(!visible)
        })

        listOf(
                viewModel.checkOutButton to tvCheckOut,
                viewModel.cancelButton to tvCancel,
        ).forEach { (model, view) ->
            model.observe(viewLifecycleOwner) { enabled ->
                view.visibility = if (enabled) View.VISIBLE else View.GONE
                view.isEnabled = enabled
                divider2.setGoneState(!tvCheckOut.isVisible && !tvCancel.isVisible)
            }
        }

        viewModel.showToast.observe(viewLifecycleOwner) { show ->
            if (show) {
                Toast.makeText(requireContext(), getString(R.string.vist_note_updated), Toast.LENGTH_LONG).show()
            }
        }

        setActionListeners()
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync {
            onMapReady(it)
        }

        tvCancel.setOnClickListener {
            cancelDialog.show()
        }

        cancelDialog = AlertDialog.Builder(requireContext())
                .setMessage(R.string.cancel_visit_confirmation)
                .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                    viewModel.onCancelClicked()
                })
                .setNegativeButton(R.string.no, null)
                .create()
    }

    fun onMapReady(p0: GoogleMap?) {

        p0?.let { map ->
            map.uiSettings.apply {
                isMyLocationButtonEnabled = true
                isZoomControlsEnabled = true
            }
            val latLng = viewModel.getLatLng() ?: return
            // Log.d(TAG, "Got latlng $latLng")
            val label = viewModel.getLabel()

            map.addMarker(MarkerOptions().position(latLng).title(label))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
        }
    }

    private fun updateView(newValue: Visit) {
        // Log.v(TAG, "updated view with value $newValue")
        tvCustomerNote.text = newValue.customerNote
        customerNoteGroup.visibility = if (newValue.customerNote.isEmpty()) View.GONE else View.VISIBLE
        val takePictureButtonDisabled = tvTakePicture.visibility == View.GONE
        val hasNoPreview = newValue.getBitmap() == null
        val pictureGroupVisitility =
                if (hasNoPreview && takePictureButtonDisabled) View.GONE else View.VISIBLE
        // Log.v(TAG, "Picture group visibility is $pictureGroupVisitility")
        visitPreviewGroup.visibility = pictureGroupVisitility
        ivVisitPic.visibility = if (newValue.getBitmap() == null) View.GONE else View.VISIBLE
        ivVisitPic.setImageBitmap(newValue.getBitmap())
        tvAddress.text = newValue.address.street
        if (newValue.visitNote != etVisitNote.text.toString()) {
            etVisitNote.setText(newValue.visitNote)
        }

    }

    private fun setActionListeners() {
        ivBack.setOnClickListener {
            viewModel.onBackPressed()
            mainActivity().onBackPressed()
        }

        etVisitNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) =
                    viewModel.onVisitNoteChanged(s.toString())

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        tvTakePicture.setOnClickListener {
            dispatchTakePictureIntent()
        }

        listOf(
                tvPickUp to viewModel::onPickUpClicked,
                tvCheckOut to viewModel::onCheckOutClicked,
        ).forEach { (view, action) ->
            view.setOnClickListener {
                disableHandlers()
                action()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Log.v(TAG, "Got image capture result $resultCode")
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            currentPhotoPath?.let { viewModel.onPictureResult(it) }
        }
    }

    private fun dispatchTakePictureIntent() {
        // Log.d(TAG, "dispatchTakePictureIntent")
        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .also { takePictureIntent ->
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Toast.makeText(requireContext(), getString(R.string.cannot_create_file_msg), Toast.LENGTH_LONG).show()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                                requireContext(),
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
                    tvCheckOut,
                    tvCancel,
                    etVisitNote
            ).forEach { it.isEnabled = false }

    var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = "${Date().time}"
        val storageDir: File = MyApplication.context.cacheDir
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