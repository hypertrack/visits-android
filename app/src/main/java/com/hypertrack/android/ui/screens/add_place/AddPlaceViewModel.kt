package com.hypertrack.android.ui.screens.add_place

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.utils.OsUtilsProvider


class AddPlaceViewModel(
    private val historyRepository: HistoryRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val placesClient: PlacesClient
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
                        map.value!!.moveCamera(
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
        if (setOnMap.value == false) {
            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling selectPlace()).

            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling selectPlace()).
            if (token == null) {
                token = AutocompleteSessionToken.newInstance()
            }

            // Use the builder to create a FindAutocompletePredictionsRequest.

            // Use the builder to create a FindAutocompletePredictionsRequest.
            val request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.GEOCODE)
                .setSessionToken(token)
                .setQuery(query)
                .build()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    Log.d("cutag", "found ${response.autocompletePredictions.firstOrNull()}")
                    places.postValue(PlaceModel.from(response.autocompletePredictions))
                }
                .addOnFailureListener { e ->
                    error.postValue(e.message)
//                if (e is ApiException) {
//                }
                }
        }
    }

    fun onSetOnMapClicked() {
        places.postValue(listOf())
        changeSetOnMapState(true)
        displayAddress()
    }

    fun onConfirmClicked(address: String) {
        proceed(map.value!!.cameraPosition.target, address)
    }

    fun onSearchViewFocus() {
        if (setOnMap.value == true) {
            changeSetOnMapState(false)
        }
    }

    fun onPlaceItemClick(item: PlaceModel) {
        val placeFields: List<Place.Field> =
            listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(item.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                place.latLng?.let {
                    proceed(place.latLng!!, place.address)
                }
            }
            .addOnFailureListener { exception: java.lang.Exception ->
                //todo handle error
            }
    }

    private fun proceed(latLng: LatLng, address: String?) {
        destination.postValue(
            AddPlaceFragmentDirections.actionAddPlaceFragmentToAddPlaceInfoFragment(
                latLng,
                address
            )
        )
    }

    private fun displayAddress() {
        map.value?.cameraPosition?.target?.let {
            searchText.postValue(osUtilsProvider.getAddressFromCoordinates(
                it.latitude,
                it.longitude,
            ).let { "${it.city}, ${it.street}" })
        }

    }

    private fun changeSetOnMapState(state: Boolean) {
        setOnMap.postValue(state)
        showSetOnMapButton.postValue(!state)
    }

}