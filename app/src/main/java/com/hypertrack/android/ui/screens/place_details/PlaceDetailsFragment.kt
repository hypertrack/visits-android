package com.hypertrack.android.ui.screens.place_details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.ui.common.KeyValueAdapter
import com.hypertrack.android.ui.common.setLinearLayoutManager
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.fragment_place_details.*

class PlaceDetailsFragment : ProgressDialogFragment(R.layout.fragment_place_details) {

    private val args: PlaceDetailsFragmentArgs by navArgs()
    private val vm: PlaceDetailsViewModel by viewModels {
        MyApplication.injector.provideParamVmFactory(
            args.geofenceId
        )
    }
    private lateinit var map: GoogleMap

    private val metadataAdapter = KeyValueAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)?.getMapAsync {
            vm.onMapReady(it)
        }

        rvMetadata.setLinearLayoutManager(requireContext())
        rvMetadata.adapter = metadataAdapter

        vm.address.observe(viewLifecycleOwner, {
            tvAddress.text = it
        })

        vm.metadata.observe(viewLifecycleOwner, {
            metadataAdapter.updateItems(it)
        })

        ivBack.setOnClickListener {
            mainActivity().onBackPressed()
        }
    }

}

