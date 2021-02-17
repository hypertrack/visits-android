package com.hypertrack.android.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragment
import com.hypertrack.android.ui.screens.visits_management.tabs.MapViewFragment
import javax.inject.Provider

class CustomFragmentFactory(
    private val mapHistoryFragmentProvider: Provider<MapViewFragment>
) : FragmentFactory() {


    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            MapViewFragment::class.java -> {mapHistoryFragmentProvider.get()}
            VisitsManagementFragment::class.java -> VisitsManagementFragment(mapHistoryFragmentProvider.get())
            else -> super.instantiate(classLoader, className)
        }
    }
}