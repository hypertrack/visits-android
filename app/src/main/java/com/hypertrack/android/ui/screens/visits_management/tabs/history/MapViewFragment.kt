package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.math.MathUtils
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.asHistory
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.utils.Factory
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.HistoryViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_tab_map_webview.*
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
                    map.showHistory(HistoryTile.MOCK_TILES.asHistory())
                }
            }
            setupTimeline(HistoryTile.MOCK_TILES, historyRenderer)
        }

        historyViewModel.error.observe(viewLifecycleOwner, { error ->
            Log.w(TAG, "History error $error")
            SnackbarUtil.showErrorSnackbar(view, error.error?.message)
        })

    }

    private fun setupTimeline(historyTiles: List<HistoryTile>, historyNavigationHandler: HistoryMapRenderer?) {

        val bottomSheetBehavior = BottomSheetBehavior.from(timeLineView)
        bottomSheetBehavior.peekHeight = 48

        val menu = timeLineView.menu
        menu.clear()

        historyTiles.forEach { tile ->
            val item = menu.add(tile.description)
            item.setIcon(tile.icon)
            item.setOnMenuItemClickListener { historyNavigationHandler?.onTileSelected(tile); true }
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        deviceHistoryView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        scrim.setOnClickListener { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val baseColor = Color.BLACK
                val baseAlpha = ResourcesCompat.getFloat(resources, R.dimen.material_emphasis_medium)
                val alpha = MathUtils.lerp(0f, 255f, slideOffset * baseAlpha).toInt()
                val color = Color.argb(alpha, baseColor.red, baseColor.green, baseColor.blue)
                scrim.setBackgroundColor(color)
                scrim.visibility = if (slideOffset > 0) View.VISIBLE else View.GONE
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
    }

    companion object {
        const val TAG = "MapViewFragment"
    }
}

