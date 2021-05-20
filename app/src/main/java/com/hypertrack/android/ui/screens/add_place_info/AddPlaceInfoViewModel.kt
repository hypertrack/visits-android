package com.hypertrack.android.ui.screens.add_place_info

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.repository.CreateGeofenceError
import com.hypertrack.android.repository.CreateGeofenceSuccess
import com.hypertrack.android.repository.IntegrationsRepository
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.common.toAddressString
import com.hypertrack.android.ui.screens.add_place.AddPlaceFragmentDirections
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragment
import com.hypertrack.android.utils.OsUtilsProvider
import kotlinx.coroutines.launch


class AddPlaceInfoViewModel(
    private val latLng: LatLng,
    private val _address: String?,
    private val _name: String?,
    private val placesRepository: PlacesRepository,
    private val integrationsRepository: IntegrationsRepository,
    private val osUtilsProvider: OsUtilsProvider,
) : BaseViewModel() {

    private var hasIntegrations: Boolean = false

    val loadingState = MutableLiveData<Boolean>(true)

    //todo to baseVM
    val error = SingleLiveEvent<String>()
    val address = MutableLiveData<String?>().apply {
        if (_address != null) {
            postValue(_address)
        } else {
            osUtilsProvider.getPlaceFromCoordinates(latLng.latitude, latLng.longitude)?.let {
                postValue(it.toAddressString())
            }
        }
    }
    val name = MutableLiveData<String>().apply {
        _name?.let {
            postValue(_name)
        }
    }

    //todo test
    val showAddIntegrationButton = MutableLiveData<Boolean>(true)

    init {
        viewModelScope.launch {
            loadingState.postValue(true)
            val res = integrationsRepository.hasIntegrations()
            if (res != null) {
                hasIntegrations = res
                loadingState.postValue(false)
            } else {
                //todo task
                hasIntegrations = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
        googleMap.addMarker(MarkerOptions().position(latLng))
    }

    fun onConfirmClicked(name: String, address: String) {
        viewModelScope.launch {
            loadingState.postValue(true)

            val res = placesRepository.createGeofence(
                latLng.latitude,
                latLng.longitude,
                name = name,
                address = address
            )
            loadingState.postValue(false)
            when (res) {
                CreateGeofenceSuccess -> {
                    destination.postValue(
                        AddPlaceFragmentDirections.actionGlobalVisitManagementFragment(
                            VisitsManagementFragment.Tab.PLACES.ordinal
                        )
                    )
                }
                is CreateGeofenceError -> {
                    error.postValue(res.e.message)
                }
            }
        }
    }

    fun onAddIntegration() {
        if (hasIntegrations) {
            destination.postValue(
                AddPlaceInfoFragmentDirections.actionAddPlaceInfoFragmentToAddIntegrationFragment()
            )
        }
    }

}