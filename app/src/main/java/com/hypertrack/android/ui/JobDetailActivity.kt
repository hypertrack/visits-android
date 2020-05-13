package com.hypertrack.android.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.*
import com.hypertrack.android.adapters.ItemsAdapter
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.utils.HyperTrackInit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.DeliveryStatusViewModel
import com.hypertrack.android.view_models.SingleDeliveryViewModel
import com.hypertrack.android.view_models.UpdateDeliveryViewModel
import com.hypertrack.android.view_models.UploadImageViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_job_detail.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*


class JobDetailActivity : AppCompatActivity(), OnMapReadyCallback {


    private val CAMERA_REQUEST = 1

    private lateinit var singleDeliveryViewModel: SingleDeliveryViewModel

    private lateinit var deliveryStatusViewModel: DeliveryStatusViewModel

    private lateinit var updateDeliveryStatus: UpdateDeliveryViewModel

    private lateinit var uploadImageViewModel: UploadImageViewModel

    private lateinit var getDeliveryIdFromIntent: String

    private lateinit var currentDelivery: Deliveries

    private var photoFile: File? = null

    private var isAnyThingChange = false

    private lateinit var timer: Timer

    private lateinit var addressLatLng: LatLng

    private lateinit var hyperTrackSdk: HyperTrack


    // Now map ready setback
    override fun onMapReady(p0: GoogleMap?) {

        googleMap = p0!!

        fetchCurrentLocation()

        //val current = LatLng(40.7128, -74.0060)
        //googleMap.addMarker(MarkerOptions().position(current).title("New York"))
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(current))
    }

    private var onAdapterItemClick: ItemsAdapter.OnScanListItemClick? = null

    private var onItemsAdapter: ItemsAdapter? = null

    private var linearLayoutManager: LinearLayoutManager? = null

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_job_detail)

        init()

    }

    // Fetch user current location
    private fun fetchCurrentLocation() {

        // Construct a FusedLocationProviderClient.
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val task = fusedLocationProviderClient.lastLocation

        task.addOnSuccessListener {
            if (it != null) {

                val latLng = LatLng(it.latitude, it.longitude);
                val markerOptions = MarkerOptions().position(latLng).title("Your Current Location")
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f))
                googleMap.addMarker(markerOptions)

            }
        }

        googleMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = false
        }

    }

    // initialize all variables
    private fun init() {

        if (intent != null && intent.hasExtra(KEY_EXTRA_DELIVERY_ID))
            getDeliveryIdFromIntent = intent.getStringExtra(KEY_EXTRA_DELIVERY_ID)!!
        else
            finish()

        hyperTrackSdk = HyperTrackInit.getAccess(applicationContext)

        setUpMap()

        onItemClick()

        initRecyclerView()

        initObservable()

        initDeliveryStatusObservable()

        initDeliveryUpdateObservable()

        initClicks()

        initImageUploadObservable()
    }

    // Set up google after some delay so our screen not ditch frames
    private fun setUpMap() {

        Handler().postDelayed({
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync(this)

        }, 1000)
    }

    // initialize recycler view
    private fun initRecyclerView() {

        linearLayoutManager =
            LinearLayoutManager(this@JobDetailActivity, LinearLayoutManager.VERTICAL, false)

        onItemsAdapter = ItemsAdapter(this@JobDetailActivity, onAdapterItemClick!!)

        rvItems.layoutManager = linearLayoutManager

        rvItems.adapter = onItemsAdapter

    }

    // init single driver response and observables
    private fun initObservable() {

        showProgressBar()
        singleDeliveryViewModel = SingleDeliveryViewModel(this.application)

        singleDeliveryViewModel.callDeliveryMethod(getDeliveryIdFromIntent)

        singleDeliveryViewModel.deliveryModel?.observe(this, Observer {

            currentDelivery = it

            if (currentDelivery != null) {

                setDataOnViews()
            } else {
                dismissProgressBar()
                showToast("The delivery item may be removed or any backend error occurred")
            }

        })
    }

    // update delivery status observables
    private fun initDeliveryStatusObservable() {

        deliveryStatusViewModel = DeliveryStatusViewModel(this.application)

        deliveryStatusViewModel.deliveryStatus?.observe(this, Observer {
            dismissProgressBar()

            if (it != null) {

                if (it.status == "completed") {

                    tvComplete.visibility = View.GONE

                    // add trip marker when delivery status change to complete
                    hyperTrackSdk.addTripMarker(
                        mapOf(
                            "deliveryId" to getDeliveryIdFromIntent,
                            "deliveryStatus" to "complete",
                            "deliveryNote" to currentDelivery.deliveryNote,
                            "deliveryPicture" to currentDelivery.deliveryPicture
                        )
                    )


                } else
                    tvComplete.visibility = View.VISIBLE
                isAnyThingChange = true
                showToast("Delivery Status Updated Successfully")
            } else {
                showToast("Something went wrong. please try again")
            }

        })

    }

    // Image upload Observable and Callback Response
    private fun initImageUploadObservable() {

        uploadImageViewModel = UploadImageViewModel(this.application)

        uploadImageViewModel.updateImageModel?.observe(this, Observer {

            if (it != null) {
                currentDelivery = it
                isAnyThingChange = true
                showToast("Delivery image Updated Successfully")
                setDataOnViews()
            } else {
                dismissProgressBar()
                showToast("Something went wrong. please try again")
            }

        })

    }

    // Update Delivery Note and remove image observables Callback Response
    private fun initDeliveryUpdateObservable() {

        updateDeliveryStatus = UpdateDeliveryViewModel(this.application)

        updateDeliveryStatus.updateModel?.observe(this, Observer {
            dismissProgressBar()

            if (it != null) {
                //finish()
                isAnyThingChange = true

                if (it.deliveryPicture.isNullOrEmpty()) {
                    currentDelivery.deliveryPicture = ""
                    setDataOnViews()
                }
                // showToast("Delivery Updated Successfully")
            } else {
                showToast("Something went wrong. please try again")
            }
        })

    }


    // initialize of all click listeners
    private fun initClicks() {

        ivBack.setOnClickListener {

            onBackPressed()
        }

        ivClose?.setOnClickListener {

            showProgressBar()

            val json = JSONObject()
            json.put("deliveryPicture", "")
            updateDeliveryStatus.callUpdateDelivery(
                getDeliveryIdFromIntent,
                json.toString()
            )
        }

        tvTakePicture.setOnClickListener {

            if (askCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }

        tvComplete?.setOnClickListener {

            showProgressBar()

            deliveryStatusViewModel.callStatusMethod(getDeliveryIdFromIntent, "complete")
        }

        tvAddress.setOnClickListener {


            if (::addressLatLng.isInitialized) {

                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${addressLatLng.latitude},${addressLatLng.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent);
                }
            } else {
                showAlertMessage("Address not found. Please try again", false)
            }

        }

        etDeliveryNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                timer = Timer()

                timer.schedule(object : TimerTask() {
                    override fun run() {

                        val getDeliveryValue = etDeliveryNote.text.toString().trim()

                        if (getDeliveryValue != currentDelivery.deliveryNote) {
                            val json = JSONObject()
                            json.put("deliveryNote", getDeliveryValue)
                            updateDeliveryStatus.callUpdateDelivery(
                                getDeliveryIdFromIntent,
                                json.toString()
                            )
                        }
                    }

                }, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (::timer.isInitialized) {
                    timer.cancel()
                }
            }

        })

    }

    // set all values on Views
    private fun setDataOnViews() {

        addressLatLng = getLocationFromAddress(createAddress(currentDelivery.address))!!

        println("The Converted Lat lng is $addressLatLng")

        tvTitle.text = currentDelivery.label

        tvAddress.text = createAddress(currentDelivery.address)

        tvCustomerNote.text = currentDelivery.customerNote

        if (!currentDelivery.deliveryNote.isNullOrEmpty())
            etDeliveryNote.setText(currentDelivery.deliveryNote)

        onItemsAdapter?.updateList(currentDelivery.items)

        if (currentDelivery.status == "completed") {
            tvComplete.visibility = View.GONE
        } else {
            tvComplete.visibility = View.VISIBLE
        }

        if (!currentDelivery.deliveryPicture.isNullOrEmpty()) {

            tvTakePicture.text = getString(R.string.replace_picture)

            Picasso.with(this@JobDetailActivity).load(currentDelivery.deliveryPicture)
                .into(ivDeliveryPic)

            ivClose.visibility = View.VISIBLE
            ivDeliveryPic.visibility = View.VISIBLE
        } else {
            ivClose.visibility = View.GONE
            ivDeliveryPic.visibility = View.GONE
            tvTakePicture.text = getString(R.string.take_picture)

        }

        // check date and time

        if (currentDelivery.status == "completed") {

            tvCompleteTime.visibility = View.VISIBLE
            tvVisitTime.visibility = View.VISIBLE
            tvCompleteTime.text =
                "Completed ".plus(convertSeverDateToTime(currentDelivery.completedAt))

            setVisitedDateView()

        } else {

            tvVisitTime.visibility = View.VISIBLE
            tvCompleteTime.visibility = View.GONE

            setVisitedDateView()

        }

        dismissProgressBar()
    }

    // create visited date after check all conditions
    private fun setVisitedDateView() {
        if (!currentDelivery.createdAt.isNullOrEmpty() && !currentDelivery.exitedAt.isNullOrEmpty()) {
            tvVisitTime.text = "Visited ".plus(
                convertSeverDateToTime(currentDelivery.enteredAt)
                    .plus(" - ").plus(convertSeverDateToTime(currentDelivery.exitedAt))
            )
        } else if (!currentDelivery.enteredAt.isNullOrEmpty()) {
            tvVisitTime.text = "Visited ".plus(convertSeverDateToTime(currentDelivery.enteredAt))
        } else {
            tvVisitTime.visibility = View.GONE

            tvVisitTime.text = "Visited - No time found"
        }
    }

    // on recycler view customer click
    private fun onItemClick() {

        onAdapterItemClick = object : ItemsAdapter.OnScanListItemClick {
            override fun onScanClick(position: Int, type: String) {

                showToast("Working on scan functionality")
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // CHeck location request code
        when (requestCode) {

            CAMERA_PERMISSION_REQUEST_CODE -> {

                // check granted permissions
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    dispatchTakePictureIntent()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Context.CAMERA_SERVICE)) {
                            showMessageOKCancel("You need to allow access to the permissions")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST) {

            println("FILE PATH IS ${photoFile?.path}")

            val result = encodeImage(photoFile!!)

            println("Encode-> $result")

            if (result != null && result.isNotEmpty()) {

                val request =
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), result)

                val json = JSONObject()
                json.put("deliveryPicture", result)
                showProgressBar()
                uploadImageViewModel.callUpdateImage(getDeliveryIdFromIntent, request)
            }
        }
    }

    // Show Message when user decline location permission
    private fun showMessageOKCancel(message: String, finishActiivty: Boolean = true) {
        AlertDialog.Builder(this@JobDetailActivity)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which -> askCameraPermission() }
            .setNegativeButton("Cancel") { dialog, which ->
                // Dismiss the dialog and close the application because location permission is mandatory
                dialog?.dismiss()
                if (finishActiivty)
                    finish()
            }
            .create()
            .show()
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_Delivery_PIC", ".jpg", storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            absolutePath
        }
    }

    // open camera intent and capture image
    private fun dispatchTakePictureIntent() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                photoFile = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        MyApplication.activity = this

    }

    override fun onStop() {
        super.onStop()
        MyApplication.activity = null
    }

    override fun onBackPressed() {
        if (isAnyThingChange)
            setResult(DELIVERY_UPDATE_RESULT_CODE)

        startActivity(
            Intent(this@JobDetailActivity, ListActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        )

        finish()
    }

}


