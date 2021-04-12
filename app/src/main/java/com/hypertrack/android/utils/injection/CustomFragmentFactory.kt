package com.hypertrack.android.utils.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.LiveMapFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.SharedHelper
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TrackingFragment
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.backend.AbstractBackendProvider
import com.hypertrack.sdk.views.HyperTrackViews


class CustomFragmentFactory(
    private val sharedHelper: SharedHelper,
    private val mapStyleOptions: MapStyleOptions,
    private val mapStyleOptionsSilver: MapStyleOptions,
    private val hyperTrackService: HyperTrackService,
    private val backendProvider: AbstractBackendProvider,
    private val realTimeUpdatesService: HyperTrackViews,
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        if (className == LiveMapFragment::class.java.name) {
            return LiveMapFragment(
                sharedHelper,
                mapStyleOptions,
                mapStyleOptionsSilver,
                hyperTrackService
            )
        }
        if (className == TrackingFragment::class.java.name) {
            return TrackingFragment(backendProvider, hyperTrackService, realTimeUpdatesService)
        }
        return super.instantiate(classLoader, className)
    }
}