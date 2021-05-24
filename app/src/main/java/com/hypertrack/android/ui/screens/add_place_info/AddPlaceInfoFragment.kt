package com.hypertrack.android.ui.screens.add_place_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.models.Integration
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_add_integration.*
import kotlinx.android.synthetic.main.fragment_add_place_info.*
import kotlinx.android.synthetic.main.fragment_add_place_info.confirm
import kotlinx.android.synthetic.main.fragment_add_place_info.toolbar
import kotlinx.android.synthetic.main.inflate_integration.*

class AddPlaceInfoFragment : ProgressDialogFragment(R.layout.fragment_add_place_info) {

    private val args: AddPlaceInfoFragmentArgs by navArgs()
    private val vm: AddPlaceInfoViewModel by viewModels {
        MyApplication.injector.provideAddPlaceInfoVmFactory(
            args.latLng,
            address = args.address,
            name = args.name,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Integration>(KEY_INTEGRATION)
            ?.observe(viewLifecycleOwner) { result ->
                result?.let {
                    vm.onIntegrationAdded(it)
                    findNavController().currentBackStackEntry?.savedStateHandle
                        ?.set(KEY_INTEGRATION, null)
                }
            }

        toolbar.title = getString(R.string.add_place)
        mainActivity().setSupportActionBar(toolbar)
        mainActivity().supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mainActivity().supportActionBar!!.setHomeButtonEnabled(true)

        (childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)?.getMapAsync {
            vm.onMapReady(it)
        }

        val listener = object : SimpleTextWatcher() {
            override fun afterChanged(text: String) {
                vm.onAddressChanged(text)
            }
        }
        etAddress.addTextChangedListener(listener)

        vm.address.observe(viewLifecycleOwner, {
            etAddress.silentUpdate(listener, it)
        })

        vm.name.observe(viewLifecycleOwner, {
            etGeofenceName.setText(it)
        })

        vm.integration.observe(viewLifecycleOwner, {
            lIntegration.setGoneState(it == null)
            it?.let {
                it.id.toView(tvIntegrationId)
                it.name?.toView(tvIntegrationName)
                it.type.toView(tvIntegrationType)
            }
        })

        vm.error.observe(viewLifecycleOwner, {
            SnackbarUtil.showErrorSnackbar(view, it)
        })

        vm.loadingState.observe(viewLifecycleOwner, {
            if (it) showProgress() else dismissProgress()
        })

        vm.destination.observe(viewLifecycleOwner, {
            findNavController().navigate(it)
        })

//        vm.showAddIntegrationButton.observe(viewLifecycleOwner, {
//            bAddIntegration.setGoneState(!it)
//        })

        vm.showGeofenceNameField.observe(viewLifecycleOwner, { show ->
            listOf(etGeofenceName, tvGeofenceName).forEach { it.setGoneState(!show) }
        })

        vm.enableConfirmButton.observe(viewLifecycleOwner, { it ->
            confirm.isSelected = it
        })

        etGeofenceName.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                vm.onAddIntegration()
            } else {
                false
            }
        }

        bAddIntegration.setOnClickListener {
            vm.onAddIntegration()
        }

        confirm.setOnClickListener {
            vm.onConfirmClicked(
                name = etGeofenceName.textString(),
                address = etAddress.textString(),
                description = etGeofenceDescription.textString()
            )
        }

        bDeleteIntegration.setOnClickListener {
            vm.onDeleteIntegrationClicked()
        }
    }

    companion object {
        const val KEY_INTEGRATION = "integration"
    }
}