package com.hypertrack.android.ui.screens.add_place_info

import android.annotation.SuppressLint
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.models.Integration
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
    private val initialAddress: String?,
    private val _name: String?,
    private val placesRepository: PlacesRepository,
    private val integrationsRepository: IntegrationsRepository,
    private val osUtilsProvider: OsUtilsProvider,
) : BaseViewModel() {

    private var hasIntegrations = MutableLiveData<Boolean>(false)

    val loadingState = MutableLiveData<Boolean>(true)

    //todo to baseVM
    val error = SingleLiveEvent<String>()

    //todo persist state in create geofence scope
    val address = MutableLiveData<String?>().apply {
        if (initialAddress != null) {
            postValue(initialAddress)
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
    val integration = MutableLiveData<Integration?>(null)

    val showAddIntegrationButton = MediatorLiveData<Boolean>().apply {
        addSource(hasIntegrations) {
            postValue((hasIntegrations.value ?: false) && integration.value == null)
        }
        addSource(integration) {
            postValue((hasIntegrations.value ?: false) && integration.value == null)
        }
    }
    val showGeofenceNameField = MediatorLiveData<Boolean>().apply {
        addSource(hasIntegrations) {
            postValue((hasIntegrations.value ?: false) && integration.value == null)
        }
        addSource(integration) {
            postValue((hasIntegrations.value ?: false) && integration.value == null)
        }
    }

    init {
        viewModelScope.launch {
            loadingState.postValue(true)
            val res = integrationsRepository.hasIntegrations()
            if (res != null) {
                hasIntegrations.postValue(res)
                loadingState.postValue(false)
            } else {
                //todo task
                hasIntegrations.postValue(false)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
        googleMap.addMarker(MarkerOptions().position(latLng))
    }

    fun onConfirmClicked(name: String, address: String, description: String) {
        viewModelScope.launch {
            loadingState.postValue(true)

            val res = placesRepository.createGeofence(
                latLng.latitude,
                latLng.longitude,
                name = name,
                address = address,
                description = description,
                integration = integration.value
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
        if (hasIntegrations.value == true) {
            destination.postValue(
                AddPlaceInfoFragmentDirections.actionAddPlaceInfoFragmentToAddIntegrationFragment()
            )
        }
    }

    fun onIntegrationAdded(integration: Integration) {
        this.integration.postValue(integration)
    }

    fun onDeleteIntegrationClicked() {
        integration.postValue(null)
    }

    fun onAddressChanged(address: String) {
        if (this.address.value != address) {
            this.address.postValue(address)
        }
    }

    companion object {
        const val KEY_ADDRESS = "address"
    }
}