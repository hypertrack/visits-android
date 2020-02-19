package com.hypertrack.android.ui

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.hypertrack.android.*
import com.hypertrack.android.adapters.CustomSpinnerAdapter
import com.hypertrack.android.response.DriverList
import com.hypertrack.android.utils.HyperTrackInit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.android.view_models.CheckInViewModel
import com.hypertrack.android.view_models.DriverListViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.activity_checkin_screen.*
import org.json.JSONObject


class CheckInActivity : AppCompatActivity() {

    companion object {
        const val TAG = "CheckInAct"

    }

    private var customSpinnerAdapter: CustomSpinnerAdapter? = null

    lateinit var driversList: ArrayList<DriverList>

    private var checkInModel: CheckInViewModel? = null

    private var driverModel: DriverListViewModel? = null


    lateinit var selectedDriverId: String

    lateinit var getFcmToken: String

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

        getFcmToken()

        initDriverModel()

        initCheckInViewModel()

        showProgress()

        driverModel?.callDriverListMethod()

    }

    private fun initDriverModel() {

        driverModel = DriverListViewModel(this.application)

        driverModel?.changeModel?.observe(this, Observer {

            dismissBar()

            // getting response from api must not be null
            if (it != null) {

                it.add(0, DriverList(name = "select driver"))
                driversList = it

                initSpinner()
            } else {
                showAlertMessage("Something went wrong with server. Please try again", true)
            }
        })

    }


    // initialize check in view model
    private fun initCheckInViewModel() {

        checkInModel = CheckInViewModel(this.application)

        checkInModel?.changeModel?.observe(this, Observer {

            dismissBar()

            if (it != null) {

                myPreferences?.saveDriverDetail(Gson().toJson(it))

                if (it._id.isNotEmpty()) {
                    startActivity(Intent(this, ListActivity::class.java))
                    btnCheckIn.isEnabled = true
                    finish()
                }
            } else {
                btnCheckIn.isEnabled = true
            }
        })

    }


    // ALl click listeners
    private fun clickListeners() {

        etDriverId?.setOnClickListener {
            spinnerDriver.performClick()
        }

        btnCheckIn.setOnClickListener {

            val getDeviceId = HyperTrackInit.getAccess(applicationContext).deviceID

            Log.i("Hyper Device Id ", getDeviceId)

            checkInJson = JSONObject()
            checkInJson.put("platform", "android")
            checkInJson.put("token", getFcmToken)
            checkInJson.put("app_name", packageName)
            checkInJson.put("device_id", getDeviceId)


            val getPermissionStatus = askLocationPermission()

            // if location permission is already granted to app then
            // call get all driver list
            if (getPermissionStatus) {

                btnCheckIn.isEnabled = false

                showProgress()
                checkInModel?.callCheckInMethod(selectedDriverId, checkInJson.toString())
            }
        }
    }

    // initialize spinner with driver list
    private fun initSpinner() {

        customSpinnerAdapter = CustomSpinnerAdapter(
            this@CheckInActivity,
            R.layout.inflate_spinner_item, driversList
        )

        spinnerDriver.adapter = customSpinnerAdapter

        spinnerDriver?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                // this check for first time spinner auto select driver
                if (position != 0) {

                    selectedDriverId = driversList[position].driver_id
                    etDriverId.text = driversList[position].name

                    btnCheckIn.isEnabled = true
                    btnCheckIn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@CheckInActivity,
                            R.color.colorBlack
                        )
                    )
                } else {

                    etDriverId.text = driversList[position].name

                    btnCheckIn.isEnabled = false
                    btnCheckIn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@CheckInActivity,
                            R.color.colorBtnDisable
                        )
                    )
                }


            }
        }
    }


    // get FCM Device token for notification purpose
    private fun getFcmToken() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                getFcmToken = task.result?.token!!

                // Log and toast
                Log.d(TAG.plus(" FCM Token-> "), getFcmToken)

            })

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
                    checkInModel?.callCheckInMethod(selectedDriverId, checkInJson.toString())

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