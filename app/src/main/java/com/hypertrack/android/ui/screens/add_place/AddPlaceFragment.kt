package com.hypertrack.android.ui.screens.add_place

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.ui.common.Utils.isDoneAction
import com.hypertrack.android.utils.MyApplication
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
        mainActivity().setSupportActionBar(toolbar)
        mainActivity().supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mainActivity().supportActionBar!!.setHomeButtonEnabled(true)

        locations.setLinearLayoutManager(requireContext())
        locations.adapter = adapter
        adapter.setOnItemClickListener { adapter, view, position ->
            vm.onPlaceItemClick(this.adapter.getItem(position))
        }

        val watcher = object : DisablableTextWatcher() {
            override fun afterChanged(text: String) {
                vm.onSearchQueryChanged(text)
            }
        }
        search.addTextChangedListener(watcher)
        search.setOnClickListener {
            vm.onSearchQueryChanged(search.textString())
        }
        search.setOnEditorActionListener { v, actionId, event ->
            if (isDoneAction(actionId, event)) {
                Utils.hideKeyboard(requireActivity())
                true
            } else false
        }

        vm.places.observe(viewLifecycleOwner, {
            adapter.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        })

        vm.error.observe(viewLifecycleOwner, {
            Utils.hideKeyboard(mainActivity())
            SnackbarUtil.showErrorSnackbar(view, it)
        })

        vm.searchText.observe(viewLifecycleOwner, {
            watcher.disabled = true
            search.setText(it)
            search.setSelection(search.textString().length)
            watcher.disabled = false
            Utils.hideKeyboard(mainActivity())
        })

        vm.loadingState.observe(viewLifecycleOwner, {
            if (it) showProgress() else dismissProgress()
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

        vm.closeKeyboard.observe(viewLifecycleOwner, {
            Utils.hideKeyboard(requireActivity())
        })

        set_on_map.hide()
        destination_on_map.show()
        confirm.show()

        confirm.setOnClickListener {
            vm.onConfirmClicked(search.textString())
        }
    }

    //todo change to custom edittext and silent update
    abstract class DisablableTextWatcher : SimpleTextWatcher() {
        var disabled = false

        override fun afterTextChanged(s: Editable?) {
            if (!disabled) {
                afterChanged((s ?: "").toString())
            }

        }
    }

}