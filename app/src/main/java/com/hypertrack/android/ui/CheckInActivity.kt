package com.hypertrack.android.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.hypertrack.android.*
import com.hypertrack.android.utils.HyperTrackInit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.android.view_models.CheckInViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_checkin_screen.*
import org.json.JSONObject


class CheckInActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CheckInAct"

    }


    private var checkInModel: CheckInViewModel? = null

    lateinit var selectedDriverId: String

    private var myPreferences: MyPreferences? = null

    private lateinit var checkInJson: JSONObject


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        myPreferences = MyPreferences(this@CheckInActivity)

        if (!myPreferences?.getDriverValue()?.driver_id.isNullOrEmpty()) {
            startActivity(Intent(this@CheckInActivity, ListActivity::class.java))
        } else {
            setContentView(R.layout.activity_checkin_screen)

            init()
        }
    }

    override fun onResume() {
        super.onResume()
        MyApplication.activity = this

    }

    // All initialize here
    private fun init() {

        clickListeners()

        initCheckInViewModel()

        showProgress()


    }


    // initialize check in view model
    private fun initCheckInViewModel() {

        checkInModel = CheckInViewModel(this.application)

        checkInModel?.changeModel?.observe(this, Observer {

            dismissBar()

//            if (it != null) {
//
//                myPreferences?.saveDriverDetail(Gson().toJson(it))
//
//                if (it._id.isNotEmpty()) {
//                    startActivity(Intent(this, ListActivity::class.java))
//                    btnCheckIn.isEnabled = true
//                    finish()
//                }
//            } else {
//                btnCheckIn.isEnabled = true
//            }
        })

    }


    // ALl click listeners
    private fun clickListeners() {


        btnCheckIn.setOnClickListener {

            val getDeviceId = HyperTrackInit.getAccess(applicationContext).deviceID

            Log.i("Hyper Device Id ", getDeviceId)

            btnCheckIn.isEnabled = false
            showProgress()
            checkInModel?.callCheckInMethod(selectedDriverId)
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
                    checkInModel?.callCheckInMethod(selectedDriverId)

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