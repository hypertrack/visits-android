package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hypertrack.android.models.AbstractBackendProvider
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TripsAdapter.OnItemClickListener
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.Injector
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.views.HyperTrackViews
import com.hypertrack.sdk.views.dao.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TrackingFragment(
    private val mBackendProvider: AbstractBackendProvider,
    private val hyperTrackService: HyperTrackService,
    private val realTimeUpdatesService: HyperTrackViews
) : Fragment(R.layout.fragment_tracking), TrackingPresenter.View {
    private var tripConfirmSnackbar: Snackbar? = null
    private lateinit var blockingView: View
    private var offlineView: View? = null
    private lateinit var locationButton: FloatingActionButton
    private lateinit var bottomHolder: View
    private lateinit var bottomHolderCover: View
    private lateinit var bottomHolderSheetBehavior: BottomSheetBehavior<*>
    private var tripInfo: View? = null
    private var tripSummaryInfo: View? = null
    private lateinit var whereAreYouGoing: View
    private var tripsCount: TextView? = null
    private var tripTo: TextView? = null
    private var destinationIcon: ImageView? = null
    private var destinationAddress: TextView? = null
    private var destinationArrival: TextView? = null
    private var destinationArrivalTitle: TextView? = null
    private var destinationAway: TextView? = null
    private var destinationAwayTitle: TextView? = null
    private var stats: TextView? = null
    private var destination: TextView? = null
    private var loader: LoaderDecorator? = null
    private lateinit var presenter: TrackingPresenter
    private var tripsAdapter = TripsAdapter()

    private val liveMapViewModel: LiveMapViewModel by viewModels({ requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Creating view")
        blockingView = view.findViewById(R.id.blocking_view)
        locationButton = view.findViewById(R.id.location_button)
        locationButton.setOnClickListener {
            presenter.setCameraFixedEnabled(true)
            locationButton.hide()
            blockingView.setOnTouchListener { _, _ ->
                presenter.setCameraFixedEnabled(false)
                locationButton.show()
                blockingView.setOnTouchListener(null)
                false
            }
        }
        offlineView = view.findViewById(R.id.offline)
        whereAreYouGoing = view.findViewById(R.id.where_are_you)
        whereAreYouGoing.setOnClickListener { presenter.openSearch() }
        bottomHolder = view.findViewById(R.id.bottom_holder)
        bottomHolderCover = view.findViewById(R.id.bottom_holder_cover)
        bottomHolderSheetBehavior = BottomSheetBehavior.from(bottomHolder)
        val tripsRecyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        tripInfo = view.findViewById(R.id.trip_info)
        tripsCount = view.findViewById(R.id.trips_count)
        tripTo = view.findViewById(R.id.trip_to)
        destinationIcon = view.findViewById(R.id.destination_icon)
        destinationAddress = view.findViewById(R.id.destination_address)
        destinationArrival = view.findViewById(R.id.destination_arrival)
        destinationArrivalTitle = view.findViewById(R.id.destination_arrival_title)
        destinationAway = view.findViewById(R.id.destination_away)
        destinationAwayTitle = view.findViewById(R.id.destination_away_title)
        tripSummaryInfo = view.findViewById(R.id.trip_summary_info)
        stats = view.findViewById(R.id.stats)
        destination = view.findViewById(R.id.destination)
        val layoutManager = LinearLayoutManager(activity)
        tripsRecyclerView.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(
            view.context,
            layoutManager.orientation
        )
        tripsRecyclerView.addItemDecoration(dividerItemDecoration)
        tripsRecyclerView.layoutManager = layoutManager
        tripsAdapter = TripsAdapter()
        tripsAdapter.setOnItemClickListener( object : OnItemClickListener {

            override fun onItemClick(
                adapter: RecyclerView.Adapter<*>?,
                view: View?,
                position: Int
            ) {
            presenter.selectTrip(tripsAdapter.getItem(position))
        }})
        tripsRecyclerView.adapter = tripsAdapter
        bottomHolder.setOnClickListener {
            if (bottomHolderSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomHolderSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                bottomHolderSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        val shareButton = view.findViewById<Button>(R.id.shareButton)
        shareButton.setOnClickListener { presenter.shareTrackMessage() }
        val endTripButton = view.findViewById<Button>(R.id.endTripButton)
        endTripButton.setOnClickListener { tripConfirmSnackbar!!.show() }
        tripConfirmSnackbar =
            Snackbar.make(view.rootView, R.layout.snackbar_trip_confirm, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.id.resume) { tripConfirmSnackbar!!.dismiss() }
                .setAction(R.id.end_trip) {
                    presenter.endTrip()
                    tripConfirmSnackbar!!.dismiss()
                }
        loader = LoaderDecorator(view.context)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resuming tracking fragment")
        presenter = TrackingPresenter(
            requireContext(),
            this,
            mBackendProvider,
            hyperTrackService,
            realTimeUpdatesService,
            viewLifecycleOwner
        )
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val map = liveMapViewModel.getMap()
            Log.d(TAG, "got google map from VM $map")
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { presenter.subscribeUpdates(map) }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Pausing...")
        presenter.pause()
    }

    override fun updateConnectionStatus(offline: Boolean) {
        if (offline) {
            bottomHolderSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            offlineView!!.visibility = View.VISIBLE
            bottomHolderCover.visibility = View.VISIBLE
        } else {
            offlineView!!.visibility = View.GONE
            bottomHolderCover.visibility = View.GONE
        }
    }

    override fun onStatusUpdateReceived(statusText: String) {}
    override fun showSearch() {
        whereAreYouGoing.visibility = View.VISIBLE
        bottomHolder.visibility = View.INVISIBLE
    }

    override fun updateTripsMenu(trips: List<Trip>, selectedTripIndex: Int) {
        if (activity != null) {
            if (trips.isEmpty()) {
                if (bottomHolder.visibility == View.VISIBLE) {
                    bottomHolder.visibility = View.INVISIBLE
                    whereAreYouGoing.visibility = View.VISIBLE
                }
                presenter.stopTripInfoUpdating()
            } else {
                whereAreYouGoing.visibility = View.INVISIBLE
                bottomHolder.visibility = View.VISIBLE
                val text = getString(R.string.you_have_ongoing_trips)
                val tripValue =
                    if (trips.size == 1) getString(R.string.trip).toLowerCase() else getString(R.string.trips).toLowerCase()
                val tripsCountText = String.format(text, trips.size, tripValue)
                tripsCount!!.text = tripsCountText
                tripsAdapter.update(trips)
                tripsAdapter.setSelection(selectedTripIndex)
            }
        }
    }

    override fun showTripInfo(trip: Trip) {
        if (activity != null) {
            val origin = R.drawable.starting_position
            val destination = R.drawable.destination
            if (trip.destination == null) {
                tripTo!!.setText(R.string.trip_started_from)
                var valueText: String? = getString(R.string.unknown)
                trip.startDate?.let { valueText = DATE_FORMAT.format(it) }

                destinationIcon!!.setImageResource(origin)
                destinationAddress!!.text = valueText
                val arrivalText = if (trip.summary == null) "-" else String.format(
                    getString(R.string._min), TimeUnit.SECONDS.toMinutes(
                        trip.summary!!.duration.toLong()
                    )
                )
                destinationArrival!!.text = arrivalText
                destinationArrivalTitle!!.setText(R.string.tracking)
                destinationAway!!.text = ""
                destinationAwayTitle!!.visibility = View.INVISIBLE
                presenter.stopTripInfoUpdating()
            } else {
                tripTo!!.setText(R.string.trip_to)
                destinationIcon!!.setImageResource(destination)
                if (!TextUtils.isEmpty(trip.destination!!.getAddress())) {
                    destinationAddress!!.text = trip.destination!!.getAddress()
                } else {
                    val latLng = String.format(
                        getString(R.string.lat_lng),
                        trip.destination!!.latitude, trip.destination!!.longitude
                    )
                    destinationAddress!!.text = latLng
                }
                if (trip.destination!!.arrivedDate == null) {
                    if (trip.estimate != null && trip.estimate!!.route != null && trip.estimate!!.route!!.duration != null) {
                        val remainingDuration = trip.estimate!!.route!!
                            .duration!!
                        val arriveDate = Date(
                            System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(remainingDuration.toLong())
                        )
                        destinationArrival!!.text = DATE_FORMAT.format(arriveDate)
                        if (remainingDuration < 120) {
                            destinationAway!!.text = getString(R.string.arriving_now)
                        } else {
                            destinationAway!!.text = String.format(
                                getString(R.string._min),
                                TimeUnit.SECONDS.toMinutes(remainingDuration.toLong())
                            )
                        }
                        presenter.startTripInfoUpdating(trip)
                    } else {
                        destinationArrival!!.text = "-"
                        destinationAway!!.text = "-"
                    }
                    destinationArrivalTitle!!.setText(R.string.arrival)
                } else {
                    destinationArrival!!.text = DATE_FORMAT.format(
                        trip.destination!!.arrivedDate!!
                    )
                    destinationAway!!.text = ""
                    destinationArrivalTitle!!.setText(R.string.arrived)
                }
                destinationAwayTitle!!.visibility = View.VISIBLE
            }
            tripSummaryInfo!!.visibility = View.GONE
            tripInfo!!.visibility = View.VISIBLE
        }
    }

    override fun showTripSummaryInfo(trip: Trip) {
        presenter.stopTripInfoUpdating()
        tripConfirmSnackbar!!.dismiss()
        if (activity != null) {
            if (trip.destination == null && trip.summary == null) {
                stats!!.visibility = View.GONE
                destination!!.visibility = View.GONE
            } else {
                if (trip.summary != null) {
                    val miles = trip.summary!!.distance * 0.000621371
                    val mins = TimeUnit.SECONDS.toMinutes(
                        trip.summary!!.duration.toLong()
                    )
                    val statsText = String.format(getString(R.string.miles_mins), miles, mins)
                    stats!!.text = statsText
                }
                if (trip.destination != null) {
                    destination!!.text = trip.destination!!.getAddress()
                }
                stats!!.visibility = View.VISIBLE
                destination!!.visibility = View.VISIBLE
            }
            tripInfo!!.visibility = View.GONE
            tripSummaryInfo!!.visibility = View.VISIBLE
        }
    }

    override fun showProgressBar() {
        loader!!.start()
    }

    override fun hideProgressBar() {
        loader!!.stop()
    }

    override fun addSearchPlaceFragment(config: SearchPlaceFragment.Config?) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_frame,
                Injector.getCustomFragmentFactory(requireContext()).instantiate(ClassLoader.getSystemClassLoader(), SearchPlaceFragment::class.java.name),
                SearchPlaceFragment::class.java.simpleName
            )
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.destroy()
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
        const val TAG = "TrackingFragment"
    }
}