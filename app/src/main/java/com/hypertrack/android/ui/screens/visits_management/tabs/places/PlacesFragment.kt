package com.hypertrack.android.ui.screens.visits_management.tabs.places

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.ui.common.show
import com.hypertrack.android.utils.Injector
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment : ProgressDialogFragment(R.layout.fragment_places) {

    private val vm: PlacesViewModel by viewModels { Injector.provideUserScopeViewModelFactory() }

    private lateinit var adapter: PlacesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPlaces.setLinearLayoutManager(requireContext())
        adapter = vm.createPlacesAdapter()
        rvPlaces.adapter = adapter
        adapter.onItemClickListener = {
            vm.onPlaceClick(it)
        }
        rvPlaces.addOnScrollListener(object : EndlessScrollListener(object : OnLoadMoreListener {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
//                Log.v("hypertrack-verbose", "EndlessScrollListener $page $totalItemsCount")
                vm.onLoadMore()
            }
        }) {
            override val visibleThreshold = 1
        })

        vm.placesPage.observe(viewLifecycleOwner, {
            if (it != null) {
                it.consume {
                    Log.v("hypertrack-verbose", "-- page ${it.map { it.geofence.name }}")
                    adapter.addItemsAndUpdate(it)
                    lPlacesPlaceholder.setGoneState(adapter.itemCount != 0)
                    rvPlaces.setGoneState(adapter.itemCount == 0)
                }
            } else {
                adapter.updateItems(listOf())
                lPlacesPlaceholder.hide()
                rvPlaces.show()
            }
        })

        vm.loadingStateBase.observe(viewLifecycleOwner, {
            srlPlaces.isRefreshing = it && adapter.itemCount == 0
            paginationProgressbar.setGoneState(!it || adapter.itemCount == 0)
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        vm.errorBase.observe(viewLifecycleOwner, {
            it.consume {
                SnackbarUtil.showErrorSnackbar(view, it)
            }
        })

        srlPlaces.setOnRefreshListener {
            vm.refresh()
        }

        fbAddPlace.setOnClickListener {
            vm.onAddPlaceClicked()
        }

        vm.refresh()
    }

    companion object {
        fun getInstance() = PlacesFragment()
    }
}