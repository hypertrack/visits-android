package com.hypertrack.android.ui.screens.add_place

import android.annotation.SuppressLint
import android.location.Address
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.OsUtilsProvider
import kotlinx.coroutines.launch

class AddPlaceViewModel(
    private val placesRepository: PlacesRepository,
    private val historyRepository: HistoryRepository,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val places = MutableLiveData<List<PlaceModel>>()
    val setOnMap = MutableLiveData<Boolean>(false)
    val loadingState = MutableLiveData<Boolean>(false)
    val showMapDestination = Transformations.map(setOnMap) { it == true }
    val showConfirmButton = Transformations.map(setOnMap) { it == true }
    val showSetOnMapButton = MutableLiveData(false)
    val showPlacesList = Transformations.map(setOnMap) { it == false }
    val map = MutableLiveData<GoogleMap>()
    val searchText = MutableLiveData<String>()
    val error = SingleLiveEvent<String>()

    init {
        ZipLiveData(historyRepository.history, map).apply {
            //todo check leak
            observeForever { pair ->
                if (map.value!!.cameraPosition.target.latitude
                    < 0.1 && map.value!!.cameraPosition.target.longitude < 0.1
                ) {
                    pair.first.locationTimePoints.lastOrNull()?.let {
                        map.value!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    it.first.latitude,
                                    it.first.longitude
                                ), 13f
                            )
                        )
                    }

                }
            }
        }
    }

    //todo persist token?
    private var token: AutocompleteSessionToken? = null

    //todo change context?
    private val placesClient: PlacesClient by lazy { Places.createClient(MyApplication.context) }

    init {
        setOnMap.postValue(true)
        showSetOnMapButton.postValue(false)
    }

    @SuppressLint("MissingPermission")
    fun onMapReady(googleMap: GoogleMap) {
        showSetOnMapButton.postValue(true)
        map.postValue(googleMap)
        try {
            googleMap.isMyLocationEnabled = true
        } catch (e: Exception) {
            //todo
        }
        googleMap.setOnCameraIdleListener {
            if (setOnMap.value!!) {
                displayAddress()
            }
        }
        googleMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
        }
    }

    fun onSearchQueryChanged(query: String) {
//        if (setOnMap.value == false) {
//            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
//            // and once again when the user makes a selection (for example when calling selectPlace()).
//
//            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
//            // and once again when the user makes a selection (for example when calling selectPlace()).
//            if (token == null) {
//                token = AutocompleteSessionToken.newInstance()
//            }
//
//            // Use the builder to create a FindAutocompletePredictionsRequest.
//
//            // Use the builder to create a FindAutocompletePredictionsRequest.
//            val request = FindAutocompletePredictionsRequest.builder()
//                .setTypeFilter(TypeFilter.GEOCODE)
//                .setSessionToken(token)
//                .setQuery(query)
//                .build()
//
//            placesClient.findAutocompletePredictions(request)
//                .addOnSuccessListener { response ->
//                    places.postValue(PlaceModel.from(response.autocompletePredictions))
//                }
//                .addOnFailureListener { e ->
//                    error.postValue(e.message)
////                if (e is ApiException) {
////                }
//                }
//        }
    }

    fun onSetOnMapClicked() {
        showSetOnMapButton.postValue(false)
        setOnMap.postValue(true)
        displayAddress()
    }

    fun onConfirmClicked(address: String) {
        viewModelScope.launch {
            loadingState.postValue(true)
            placesRepository.createGeofence(
                map.value!!.cameraPosition.target.latitude,
                map.value!!.cameraPosition.target.longitude,
                address
            )
            loadingState.postValue(false)
            popBackStack.postValue(true)
        }
    }

    private fun displayAddress() {
        map.value?.cameraPosition?.target?.let {
            searchText.postValue(osUtilsProvider.getAddressFromCoordinates(
                it.latitude,
                it.longitude,
            ).let { "${it.city}, ${it.street}" })
        }

    }

}