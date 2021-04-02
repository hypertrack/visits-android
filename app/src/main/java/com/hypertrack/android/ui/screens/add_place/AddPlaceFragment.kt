package com.hypertrack.android.ui.screens.add_place

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.view_models.HistoryViewModel
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_add_place.*

class AddPlaceFragment : ProgressDialogFragment(R.layout.fragment_add_place) {

    private val vm: AddPlaceViewModel by viewModels { MyApplication.injector.provideUserScopeViewModelFactory() }

    private val adapter = PlacesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)?.getMapAsync {
            vm.onMapReady(it)
        }

        toolbar.title = getString(R.string.add_place)

        locations.setLinearLayoutManager(requireContext())
        locations.adapter = adapter

        val watcher = object : DisablableTextWatcher() {
            override fun afterChanged(text: String) {
                vm.onSearchQueryChanged(text)
            }
        }
        search.addTextChangedListener(watcher)
        Utils.showKeyboard(mainActivity(), search)

        vm.places.observe(viewLifecycleOwner, {
            adapter.clear()
            adapter.addAll(it)
        })

        vm.error.observe(viewLifecycleOwner, {
            Utils.hideKeyboard(mainActivity())
            SnackbarUtil.showErrorSnackbar(view, it)
        })

        vm.showMapDestination.observe(viewLifecycleOwner, {
            if (it) {
                Utils.hideKeyboard(mainActivity())
            }
            destination_on_map.setGoneState(!it)
        })

        vm.searchText.observe(viewLifecycleOwner, {
            watcher.disabled = true
            search.setText(it)
            watcher.disabled = false
        })

        set_on_map.hide()
        vm.showSetOnMapButton.observe(viewLifecycleOwner, {
//            set_on_map.setGoneState(!it)
        })

        vm.showPlacesList.observe(viewLifecycleOwner, {
            locations.setGoneState(!it)
        })

        vm.showConfirmButton.observe(viewLifecycleOwner, {
            confirm.setGoneState(!it)
        })

        vm.loadingState.observe(viewLifecycleOwner, {
            if (it) showProgress() else dismissProgress()
        })

        vm.popBackStack.observe(viewLifecycleOwner, {
            findNavController().popBackStack()
        })

        set_on_map.setOnClickListener {
            vm.onSetOnMapClicked()
        }

        confirm.setOnClickListener {
            vm.onConfirmClicked(search.textString())
        }
    }

    abstract class DisablableTextWatcher : SimpleTextWatcher() {
        var disabled = false

        override fun afterTextChanged(s: Editable?) {
            if (!disabled) {
                afterChanged((s ?: "").toString())
            }

        }
    }

}