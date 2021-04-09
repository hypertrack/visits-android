package com.hypertrack.android.ui.screens.visits_management.tabs.map

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_map_webview.*
import kotlinx.android.synthetic.main.progress_bar.*

class LiveMapFragment : Fragment(R.layout.fragment_tab_map_view)  {

    private var state: LoadingProgressState = LoadingProgressState.LOADING

    private lateinit var sharedHelper: SharedHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        sharedHelper = SharedHelper.getInstance(this)

        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map)
        mapStyleOptionsSilver = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_silver)

    }

    override fun onResume() {
        super.onResume()
        if (state == LoadingProgressState.LOADING) displayLoadingState(true)
    }

    override fun onPause() {
        super.onPause()
        if (progress.isVisible) displayLoadingState(false)
    }

    private fun displayLoadingState(isLoading: Boolean) {
        progress.setGoneState(!isLoading)
        progress.background = null
        if (isLoading) loader.playAnimation() else loader.cancelAnimation()
    }

    companion object {const val TAG = "MapFragment"}
}


private enum class LoadingProgressState {
    LOADING,
    DONE
}