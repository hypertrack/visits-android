package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.TrackingStateValue
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_map_view.*
import kotlinx.android.synthetic.main.fragment_tab_map_webview.progress
import kotlinx.android.synthetic.main.progress_bar.*
import java.util.*

class LiveMapFragment(
    private val mapStyleOptions: MapStyleOptions,
    private val mapStyleOptionsSilver: MapStyleOptions,
    private val hyperTrackService: HyperTrackService,
) : Fragment(R.layout.fragment_tab_map_view)  {

    private var state: LoadingProgressState = LoadingProgressState.LOADING
    private var currentMapStyle = mapStyleOptions
    private var gMap: GoogleMap? = null

    private val liveMapViewModel: LiveMapViewModel by viewModels()

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
        hyperTrackService.state.observe(viewLifecycleOwner) {
            when(it) {
                TrackingStateValue.TRACKING -> onTrackingStart()
                TrackingStateValue.STOP -> onTrackingStop()
                else -> onError()
            }
        }
        (childFragmentManager.findFragmentById(R.id.liveMap) as SupportMapFragment)
            .getMapAsync {
                gMap = it
                liveMapViewModel.googleMap = it
                state = LoadingProgressState.DONE
                displayLoadingState(false)
                hyperTrackService.state.value?.let { state ->
                    when (state) {
                        TrackingStateValue.TRACKING -> onMapActive()
                        else -> onMapDisabled()
                    }
                }
            }

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_frame,
                parentFragmentManager.fragmentFactory.instantiate(
                    ClassLoader.getSystemClassLoader(), TrackingFragment::class.java.name
                ),
                TrackingFragment::class.java.name
            )
            .commitAllowingStateLoss()

    }

    override fun onResume() {
        super.onResume()
        if (state == LoadingProgressState.LOADING) displayLoadingState(true)
        activity?.apply {
            registerReceiver(shareBroadcastReceiver, IntentFilter(SHARE_BROADCAST_ACTION))

        }

    }

    override fun onPause() {
        super.onPause()
        if (progress.isVisible) displayLoadingState(false)
    }

    private fun onTrackingStart() {
        trackingStatus.isActivated = true
        trackingStatus.setText(R.string.active)
        trackingStatusText.visibility = View.GONE
        trackingStatusText.text =
            String.format(
                getString(R.string.tracking_is), getString(R.string.active).toLowerCase(
                    Locale.getDefault()
                )
            )
        onMapActive()
    }


    private fun onTrackingStop() {
        trackingStatus.isActivated = false
        trackingStatus.setText(R.string.inactive)
        trackingStatusText.text = String.format(
            getString(R.string.tracking_is),
            getString(R.string.disabled).toLowerCase(Locale.ROOT)
        )
        onMapDisabled()
    }

    private fun onError() = onMapDisabled()

    private fun onMapActive() {
        Log.d(TAG, "onMapActive")
        gMap?.let {
            if (currentMapStyle != mapStyleOptions) {
                Log.d(TAG, "applying active style")
                it.setMapStyle(mapStyleOptions)
                currentMapStyle = mapStyleOptions
            }
        }
    }

    private fun onMapDisabled() {
        Log.d(TAG, "onMapDisabled")
        gMap?.let {
            if (currentMapStyle != mapStyleOptionsSilver) {
                Log.d(TAG, "applying active style")
                it.setMapStyle(mapStyleOptionsSilver)
                currentMapStyle = mapStyleOptionsSilver
            }
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


private enum class LoadingProgressState { LOADING, DONE }