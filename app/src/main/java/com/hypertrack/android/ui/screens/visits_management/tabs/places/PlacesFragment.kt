package com.hypertrack.android.ui.screens.visits_management.tabs.places

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.common.setLinearLayoutManager
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

        vm.places.observe(viewLifecycleOwner, {
            lPlacesPlaceholder.setGoneState(it.isNotEmpty())
            rvPlaces.setGoneState(it.isEmpty())
            adapter.updateItems(it)
        })

        vm.loadingState.observe(viewLifecycleOwner, {
            srlPlaces.isRefreshing = it
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        srlPlaces.setOnRefreshListener {
            vm.refresh()
        }
    }

    companion object {
        fun getInstance() = PlacesFragment()
    }
}