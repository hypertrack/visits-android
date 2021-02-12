package com.hypertrack.android.ui.screens.visits_management.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.utils.HistoryRendererFactory
import com.hypertrack.android.view_models.HistoryViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MapWebViewFragment(
    private val historyMapViewModelFactory: ViewModelProvider.Factory,
    private val historyRendererFactory: HistoryRendererFactory
) : Fragment(R.layout.fragment_tab_map_webview) {

    private val historyViewModel: HistoryViewModel by viewModels { historyMapViewModelFactory }
    private var historyRenderer: HistoryMapRenderer? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view1, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.deviceHistoryView) as SupportMapFragment?)?.let {
            historyRenderer = historyRendererFactory.create(it)
        }
        historyViewModel.history.observe(viewLifecycleOwner) { history ->
            historyRenderer?.let {map -> MainScope().launch { map.showHistory(history) } }
        }
    }
}