package com.hypertrack.android.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hypertrack.android.LOCATION_REQUEST_CODE
import com.hypertrack.android.askLocationPermission
import com.hypertrack.android.showProgress
import com.hypertrack.android.utils.HyperTrackInit
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.android.view_models.DriverLoginViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_checkin_screen.*


class CheckInActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CheckInAct"

    }

    private var driverLoginModel: DriverLoginViewModel? = null
    private var isBranchInitialized = false

    lateinit var selectedDriverId: String

    private lateinit var myPreferences: MyPreferences


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        myPreferences = MyPreferences(this@CheckInActivity, Gson())

        myPreferences.restoreRepository() ?: run {
            // init Branch IO if no repo (pk was persisted)
            isBranchInitialized = true
        }


        setContentView(R.layout.activity_checkin_screen)
        init()
    }

    override fun onResume() {
        super.onResume()
        if (isBranchInitialized) {
        // Branch get params

        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (isBranchInitialized) {
            // Branch get params
        }

    }

    // All initialize here
    private fun init() {

        clickListeners()

        initCheckInViewModel()

        showProgress()

    }


    // initialize check in view model
    private fun initCheckInViewModel() {


    }


    // ALl click listeners
    private fun clickListeners() {


        btnCheckIn.setOnClickListener {

            val getDeviceId = HyperTrackInit.getAccess(applicationContext).deviceID

            Log.i("Hyper Device Id ", getDeviceId)

            btnCheckIn.isEnabled = false
            showProgress()
            driverLoginModel?.callCheckInMethod(selectedDriverId)
        }
    }

    // get permission callback
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // CHeck location request code
        when (requestCode) {

            LOCATION_REQUEST_CODE -> {

                // check granted permissions
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showProgress()
                    btnCheckIn.isEnabled = false
                    driverLoginModel?.callCheckInMethod(selectedDriverId)

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to the permissions")
                        }
                    }
                }
            }
        }
    }

    // Show Message when user decline location permission
    private fun showMessageOKCancel(message: String) {
        AlertDialog.Builder(this@CheckInActivity)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, which -> askLocationPermission() }
            .setNegativeButton("Cancel") { dialog, which ->
                // Dismiss the dialog and close the application because location permission is mandatory
                dialog?.dismiss()
                finish()
            }
            .create()
            .show()
    }


}