package com.hypertrack.android.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.hypertrack.android.*
import com.hypertrack.android.adapters.JobListAdapters
import com.hypertrack.android.response.Deliveries
import com.hypertrack.android.response.HeaderItem
import com.hypertrack.android.utils.HyperTrackInit
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.MyPreferences
import com.hypertrack.android.view_models.CheckOutViewModel
import com.hypertrack.android.view_models.DeliveryStatusViewModel
import com.hypertrack.android.view_models.SingleDriverViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver
import kotlinx.android.synthetic.main.activity_job_listing.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ListActivity : AppCompatActivity(), TrackingStateObserver.OnTrackingStateChangeListener {

    private var onAdapterItemClick: JobListAdapters.OnListAdapterClick? = null

    private var jobListAdapters: JobListAdapters? = null

    private var linearLayoutManager: LinearLayoutManager? = null

    private lateinit var hyperTrackSdk: HyperTrack

    private lateinit var singleDriverViewModel: SingleDriverViewModel

    private lateinit var deliveryStatusViewModel: DeliveryStatusViewModel

    private lateinit var checkOutViewModel: CheckOutViewModel

    private lateinit var getDriverIdFromIntent: String

    private lateinit var driverDeliveriesList: ArrayList<Any>

    private var myPreferences: MyPreferences? = null

    private var getPendingItem = listOf<Deliveries>()
    private var getVisitedItem = listOf<Deliveries>()
    private var getCompletedItem = listOf<Deliveries>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_listing)

        init()
    }


    // initialize all variable here
    private fun init() {

        myPreferences = MyPreferences(this@ListActivity, Gson())

        val getDriver = myPreferences?.getDriverValue()

//        if (getDriver?.driver_id.isNullOrEmpty()) {
//
//            println("Landed in List Activity Class")
//            finish()
//        }

//        getDriverIdFromIntent = getDriver?.driver_id!!
//
//        tvUserName.text = "Welcome, ".plus(getDriver.name)

//        if (intent != null && intent.hasExtra(KEY_EXTRA_DRIVER_ID))
//            getDriverIdFromIntent = intent.getStringExtra(KEY_EXTRA_DRIVER_ID)!!
//        else
//            finish()

        onItemClick()

        initRecyclerView()

        clickListeners()

        initObservable()

        initDeliveryStatusObserver()

        initCheckOutObservable()

        startSdk()


    }

    private fun clickListeners() {

        ivBack.setOnClickListener {

            showProgressBar()
            checkOutViewModel.callCheckOutMethod(getDriverIdFromIntent)
        }

        ivRefresh.setOnClickListener {

            showProgressBar()

            singleDriverViewModel.callFetchDeliveries(getDriverIdFromIntent)
        }
    }

    private fun initRecyclerView() {
        linearLayoutManager =
            LinearLayoutManager(this@ListActivity, LinearLayoutManager.VERTICAL, false)

        jobListAdapters = JobListAdapters(this@ListActivity, onAdapterItemClick!!)

        recyclerView.layoutManager = linearLayoutManager

        recyclerView.adapter = jobListAdapters


    }

    // init single driver response and observables
    private fun initObservable() {

        singleDriverViewModel = SingleDriverViewModel(this.application)

        showProgressBar()

        singleDriverViewModel.callFetchDeliveries(getDriverIdFromIntent)

        singleDriverViewModel.driverModel?.observe(this, Observer { it ->

            dismissProgressBar()

            if (it != null) {

                driverDeliveriesList = arrayListOf()

                getPendingItem = it.deliveries.filter { it.status == "pending" }
                getVisitedItem = it.deliveries.filter { it.status == "visited" }
                getCompletedItem = it.deliveries.filter { it.status == "completed" }
                if (getPendingItem.isNotEmpty()) {

                    driverDeliveriesList.add(HeaderItem("Pending deliveries"))
                    driverDeliveriesList.addAll(getPendingItem)
                }

                if (getVisitedItem.isNotEmpty()) {

                    driverDeliveriesList.add(HeaderItem("Visited deliveries"))
                    driverDeliveriesList.addAll(getVisitedItem)
                }

                if (getCompletedItem.isNotEmpty()) {

                    driverDeliveriesList.add(HeaderItem("Completed deliveries"))
                    driverDeliveriesList.addAll(getCompletedItem)
                }

                jobListAdapters?.updateList(driverDeliveriesList)

                makeTrackerStatus()

            }

        })
    }

    private fun initDeliveryStatusObserver() {

        deliveryStatusViewModel = DeliveryStatusViewModel(this.application)

        deliveryStatusViewModel.deliveryStatus?.observe(this, Observer {

            if (it != null) {

                showProgressBar()

                singleDriverViewModel.callFetchDeliveries(getDriverIdFromIntent)

            }

        })
    }

    // init single driver response and observables
    private fun initCheckOutObservable() {

        checkOutViewModel = CheckOutViewModel(this.application)

        checkOutViewModel.changeModel?.observe(this, Observer {

            dismissProgressBar()

            if (it != null) {

                myPreferences?.clearPreferences()

                val intent = Intent(this@ListActivity, CheckInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                finish()

            }

        })
    }

    // start hyper track sdk for for tracking
    private fun startSdk() {

        hyperTrackSdk = HyperTrackInit.getAccess(applicationContext)

        hyperTrackSdk.allowMockLocations()

        hyperTrackSdk.addTrackingListener(this)

        //hyperTrackSdk.start()

    }

    // Implement click from adapter class
    private fun onItemClick() {

        onAdapterItemClick = object : JobListAdapters.OnListAdapterClick {
            override fun onJobItemClick(position: Int) {

                val getItem = driverDeliveriesList[position] as Deliveries

                startActivityForResult(
                    Intent(this@ListActivity, JobDetailActivity::class.java)
                        .putExtra(KEY_EXTRA_DELIVERY_ID, getItem.delivery_id),
                    DELIVERY_UPDATE_RESULT_CODE
                )
            }

        }

    }

    // Hyper track Sdk all implemented methods
    override fun onTrackingStart() {
        makeTrackerStatus()
        println("Android-> Hyper Track OnTrackingStart Method")
    }

    override fun onError(p0: TrackingError?) {
        println("Android-> Hyper Track OnError Method ${p0?.message}")
    }

    override fun onTrackingStop() {
        makeTrackerStatus()
        println("Android-> Hyper Track OnTrackingStop Method")
    }

    // Activity Lifecycle methods
    override fun onResume() {
        super.onResume()
        MyApplication.activity = this
        if (hyperTrackSdk.isRunning) {
            //onTrackingStart()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hyperTrackSdk.removeTrackingListener(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun getStatusFromFCM(updateItem: UpdateStatusModel) {

        if (updateItem != null) {

            val getStatus = updateItem.deliveryStatus
            val getDeliveryId = updateItem.deliveryId

            if (getStatus == "geofence_enter") {

                for ((index, item) in driverDeliveriesList.withIndex()) {

                    if (item is Deliveries) {

                        if (item.delivery_id == getDeliveryId) {
                            if (item.status == "pending") {

                                item.enteredAt = getCurrentTime()

                                println("The Current Time is ${item.enteredAt}")

                                jobListAdapters?.notifyItemChanged(index)

                            }
                            break
                        }
                    }
                }
            } else if (getStatus == "geofence_exit") {

                singleDriverViewModel.callFetchDeliveries(getDriverIdFromIntent)

                // deliveryStatusViewModel.callStatusMethod(getDeliveryId, "visit")
            }
        }
    }

    private fun makeTrackerStatus() {

        val getTrackerStatus = hyperTrackSdk.isRunning

        var createStatusText = ""

        getTrackerStatus.let {

            if (it) {
                createStatusText = "Location tracking is active"

                tvTrackerStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorListStatus
                    )
                )

            } else {
                createStatusText = "Location tracking is inactive"

                tvTrackerStatus.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_red_dark
                    )
                )
            }

        }

        tvTrackerStatus.text = createStatusText.plus("\n")
            .plus(
                "You visited ${getVisitedItem.size.plus(getCompletedItem.size)} out of ${getVisitedItem.size.plus(
                    getCompletedItem.size
                ).plus(getPendingItem.size)}"
            )


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        MyApplication.activity = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == DELIVERY_UPDATE_RESULT_CODE) {
            showProgressBar()
            singleDriverViewModel.callFetchDeliveries(getDriverIdFromIntent)
        }
    }

}