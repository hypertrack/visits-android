package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.utils.Factory
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.HistoryViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.launch

class MapViewFragment : Fragment(R.layout.fragment_tab_map_webview) {

    private val historyViewModel: HistoryViewModel by viewModels {
        MyApplication.injector.provideUserScopeViewModelFactory()
    }
    private var historyRenderer: HistoryMapRenderer? = null

    private val rendererFactory: Factory<SupportMapFragment, HistoryMapRenderer> =
            MyApplication.injector.getHistoryRendererFactory()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.deviceHistoryView) as SupportMapFragment?)?.let {
            historyRenderer = rendererFactory.create(it)
        }

        historyViewModel.history.observe(viewLifecycleOwner) { history ->
            Log.d(TAG, "Updating history $history")
            historyRenderer?.let { map ->
                viewLifecycleOwner.lifecycleScope.launch {
                    map.showHistory(history)
                }
            }
        }

        historyViewModel.error.observe(viewLifecycleOwner, { error ->
            Log.w(TAG, "History error $error")
            SnackbarUtil.showErrorSnackbar(view, error.error?.message)
        })

    }

    companion object {
        const val TAG = "MapViewFragment"
    }
}
