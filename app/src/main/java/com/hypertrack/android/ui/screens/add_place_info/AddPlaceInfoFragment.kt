package com.hypertrack.android.ui.screens.add_place_info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.SnackbarUtil
import com.hypertrack.android.ui.common.textString
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_add_place.*
import kotlinx.android.synthetic.main.fragment_add_place_info.*
import kotlinx.android.synthetic.main.fragment_add_place_info.confirm
import kotlinx.android.synthetic.main.fragment_add_place_info.toolbar

class AddPlaceInfoFragment : ProgressDialogFragment(R.layout.fragment_add_place_info) {

    private val args: AddPlaceInfoFragmentArgs by navArgs()
    private val vm: AddPlaceInfoViewModel by viewModels {
        MyApplication.injector.provideAddPlaceInfoVmFactory(
            args.latLng,
            address = args.address,
            name = args.name,
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = getString(R.string.add_place)
        mainActivity().setSupportActionBar(toolbar)
        mainActivity().supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mainActivity().supportActionBar!!.setHomeButtonEnabled(true)

        (childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?)?.getMapAsync {
            vm.onMapReady(it)
        }

        vm.address.observe(viewLifecycleOwner, {
            etAddress.setText(it)
        })

        vm.name.observe(viewLifecycleOwner, {
            etGeofenceName.setText(it)
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

        confirm.setOnClickListener {
            vm.onConfirmClicked(
                name = etGeofenceName.textString(),
                address = etAddress.textString()
            )
        }
    }
}