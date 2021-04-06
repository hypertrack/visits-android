package com.hypertrack.android.ui.screens.add_place_info

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.repository.CreateGeofenceError
import com.hypertrack.android.repository.CreateGeofenceSuccess
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.ui.screens.add_place.AddPlaceFragmentDirections
import com.hypertrack.android.ui.screens.add_place.PlaceModel
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.OsUtilsProvider
import kotlinx.coroutines.launch
import java.util.*


class AddPlaceInfoViewModel(
    private val latLng: LatLng,
    private val _address: String?,
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val loadingState = MutableLiveData<Boolean>(false)

    //todo to baseVM
    val error = SingleLiveEvent<String>()
    val address = MutableLiveData<String?>().apply {
        if (_address != null) {
            postValue(_address)
        } else {
            postValue(
                osUtilsProvider.getPlaceFromCoordinates(latLng.latitude, latLng.longitude)?.let {
                    (it.locality?.let { "$it, " } ?: "") + it.thoroughfare
                        ?: "${it.latitude}, ${it.longitude}"
                })
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
                    destination.postValue(AddPlaceFragmentDirections.actionGlobalVisitManagementFragment())
                }
                is CreateGeofenceError -> {
                    error.postValue(res.e.message)
                }
            }
        }
    }

}