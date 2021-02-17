package com.hypertrack.android.ui.screens.visits_management.tabs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.utils.HistoryRendererFactory
import com.hypertrack.android.view_models.HistoryViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_map_webview.*
import kotlinx.coroutines.launch

class MapViewFragment(
    private val historyMapViewModelFactory: ViewModelProvider.Factory,
    private val historyRendererFactory: HistoryRendererFactory
) : Fragment(R.layout.fragment_tab_map_webview) {

    private val historyViewModel: HistoryViewModel by viewModels { historyMapViewModelFactory }
    private var historyRenderer: HistoryMapRenderer? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view1: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view1, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.deviceHistoryView) as SupportMapFragment?)?.let {
            Log.d(TAG, "Initializing history Renderer")
            historyRenderer = historyRendererFactory.create(it)
        }

        historyViewModel.history.observe(viewLifecycleOwner) { history ->
            srlHistory.isRefreshing = false
            Log.d(TAG, "Inside history update callback")
            historyRenderer?.let {map -> viewLifecycleOwner.lifecycleScope.launch {
                Log.d(TAG, "Launching history rendering coroutine")
                map.showHistory(history)
            } }
        }

        srlHistory.setOnRefreshListener {
            historyViewModel.getHistory()
        }

//        historyViewModel.getHistory()
    }
    companion object {const val TAG = "MapWebViewFragment"}
}