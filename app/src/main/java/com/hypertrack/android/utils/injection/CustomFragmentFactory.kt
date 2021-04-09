package com.hypertrack.android.utils.injection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.hypertrack.android.ui.screens.visits_management.tabs.map.LiveMapFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.map.SharedHelper
import com.hypertrack.android.utils.HyperTrackService

class CustomFragmentFactory(
    private val sharedHelper: SharedHelper,
    private val mapStyleOptions: MapStyleOptions,
    private val mapStyleOptionsSilver: MapStyleOptions,
    private val hyperTrackService: HyperTrackService,
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
        return super.instantiate(classLoader, className)
    }
}