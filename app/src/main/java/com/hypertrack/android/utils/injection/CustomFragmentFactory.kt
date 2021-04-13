package com.hypertrack.android.utils.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.LiveMapFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.SearchPlaceFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.livemap.TrackingFragment
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.backend.AbstractBackendProvider
import com.hypertrack.sdk.views.HyperTrackViews


class CustomFragmentFactory(
    private val mapStyleOptions: MapStyleOptions,
    private val mapStyleOptionsSilver: MapStyleOptions,
    private val hyperTrackService: HyperTrackService,
    private val backendProvider: AbstractBackendProvider,
    private val realTimeUpdatesService: HyperTrackViews,
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            LiveMapFragment::class.java.name -> LiveMapFragment(
                mapStyleOptions,
                mapStyleOptionsSilver,
                hyperTrackService
            )
            TrackingFragment::class.java.name ->
                TrackingFragment(backendProvider, hyperTrackService, realTimeUpdatesService)
            SearchPlaceFragment::class.java.name ->
                SearchPlaceFragment(
                    backendProvider,
                    hyperTrackService.deviceId,
                    realTimeUpdatesService
                )
            else -> super.instantiate(classLoader, className)
        }
    }
}