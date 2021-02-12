package com.hypertrack.android.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.hypertrack.android.ui.screens.visits_management.tabs.MapWebViewFragment
import javax.inject.Provider

class CustomFragmentFactory(
    private val mapHistoryFragmentProvider: Provider<MapWebViewFragment>
) : FragmentFactory() {


    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            MapWebViewFragment::class.java -> {mapHistoryFragmentProvider.get()}
            else -> super.instantiate(classLoader, className)
        }
    }
}