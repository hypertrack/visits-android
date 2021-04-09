package com.hypertrack.android.ui.screens.visits_management.tabs.map

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.wrappers.InstantApps
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.TrackingError
import com.hypertrack.sdk.TrackingStateObserver
import kotlinx.android.synthetic.main.fragment_tab_map_view.*
import kotlinx.android.synthetic.main.fragment_tab_map_webview.*
import kotlinx.android.synthetic.main.fragment_tab_map_webview.progress
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.*

class LiveMapFragment(
    private val sharedHelper: SharedHelper,
    private val mapStyleOptions: MapStyleOptions,
    private val mapStyleOptionsSilver: MapStyleOptions,
) : Fragment(R.layout.fragment_tab_map_view)  {

    private var state: LoadingProgressState = LoadingProgressState.LOADING
    private var currentMapStyle = mapStyleOptions
    private var map: GoogleMap? = null

    private val trackingStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getIntExtra(TrackingStateObserver.EXTRA_KEY_CODE_, 0)
            when (code) {
                TrackingStateObserver.EXTRA_EVENT_CODE_START -> onTrackingStart()
                TrackingStateObserver.EXTRA_EVENT_CODE_STOP -> onTrackingStop()
                else -> onError(code)
            }
        }
    }

    private val shareBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        trackingStatusText.setOnClickListener { trackingStatusText.visibility = View.GONE }
        trackingStatus.setOnClickListener {
            trackingStatusText.visibility =
                if (trackingStatusText.visibility == View.VISIBLE) View.GONE
                else View.VISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        if (state == LoadingProgressState.LOADING) displayLoadingState(true)
        activity?.apply {
            registerReceiver(
                trackingStateReceiver,
                IntentFilter(TrackingStateObserver.ACTION_TRACKING_STATE)
            )
            registerReceiver(shareBroadcastReceiver, IntentFilter(SHARE_BROADCAST_ACTION))

        }
    }

    override fun onPause() {
        super.onPause()
        if (progress.isVisible) displayLoadingState(false)
    }

    fun onTrackingStart() {
        trackingStatus.isActivated = true
        trackingStatus.setText(R.string.active)
        trackingStatusText.visibility = View.GONE
        trackingStatusText.text =
            String.format(
                getString(R.string.tracking_is), getString(R.string.active).toLowerCase(
                    Locale.getDefault()
                )
            )
    }


    fun onTrackingStop() {
        trackingStatus.isActivated = false
        trackingStatus.setText(R.string.inactive)
        trackingStatusText.text = String.format(
            getString(R.string.tracking_is),
            getString(R.string.disabled).toLowerCase(Locale.ROOT)
        )
    }

    fun onError(errorCode: Int) {
        when (errorCode) {
            TrackingError.INVALID_PUBLISHABLE_KEY_ERROR, TrackingError.AUTHORIZATION_ERROR -> {
                Log.e(TAG, "Need to check publishable key")
                // TODO Denys: device blocked
            }
            TrackingError.PERMISSION_DENIED_ERROR -> {
                // User refused permission or they were not requested.
                // Request permission from the user yourself or leave it to SDK.
                // TODO Denys: Navigate to permissions
            }
            TrackingError.GPS_PROVIDER_DISABLED_ERROR -> {
            // TODO Denys: Update status?
            }
            TrackingError.UNKNOWN_ERROR -> {
            }
        }
        onMapDisabled()
    }

    fun onMapActive() {
        map?.let {
            if (currentMapStyle != mapStyleOptions) {
                it.setMapStyle(mapStyleOptions)
                currentMapStyle = mapStyleOptions
            }

        }
    }

    fun onMapDisabled() {
        map?.let {
            if (currentMapStyle != mapStyleOptionsSilver) {
                it.setMapStyle(mapStyleOptionsSilver)
                currentMapStyle = mapStyleOptionsSilver
            }
        }
    }


    fun getMapAsync(onMapReadyCallback: OnMapReadyCallback) {
        if (map == null) {
            val mapFragment = parentFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                map = googleMap
                val uiSettings = googleMap.uiSettings
                uiSettings.isMapToolbarEnabled = false
                onMapReadyCallback.onMapReady(googleMap)
            }
        } else {
            onMapReadyCallback.onMapReady(map)
        }
    }

    private fun displayLoadingState(isLoading: Boolean) {
        progress.setGoneState(!isLoading)
        progress.background = null
        if (isLoading) loader.playAnimation() else loader.cancelAnimation()
    }

    companion object {
        const val TAG = "MapFragment"
        const val SHARE_BROADCAST_ACTION = "com.hypertrack.visits.SHARE_BROADCAST_ACTION"
    }
}


private enum class LoadingProgressState {
    LOADING,
    DONE
}