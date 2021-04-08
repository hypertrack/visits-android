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
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.models.Location
import com.hypertrack.android.repository.HistoryRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.ui.common.toAddressString
import com.hypertrack.android.ui.screens.visits_management.tabs.history.DeviceLocationProvider
import com.hypertrack.android.utils.OsUtilsProvider


class AddPlaceViewModel(
    private val osUtilsProvider: OsUtilsProvider,
    private val placesClient: PlacesClient,
    private val deviceLocationProvider: DeviceLocationProvider,
) : BaseViewModel() {

    private val currentLocation = MutableLiveData<Location>()
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

    //todo persist token?
    private var token: AutocompleteSessionToken? = null
    private var bias: RectangularBounds? = null

    init {
        deviceLocationProvider.getCurrentLocation {
            Log.e("cutag", "location")
            currentLocation.postValue(it)
        }
    }

    init {
        ZipLiveData(currentLocation, map).apply {
            //todo check leak
            observeForever { pair ->
                Log.e("cutag", "zip")
                if (map.value!!.cameraPosition.target.latitude
                    < 0.1 && map.value!!.cameraPosition.target.longitude < 0.1
                ) {
                    pair.first.let { location ->
                        map.value!!.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 13f
                            )
                        )

                        bias = RectangularBounds.newInstance(
                            LatLng(location.latitude - 0.1, location.longitude + 0.1),  // SW
                            LatLng(location.latitude + 0.1, location.longitude - 0.1) // NE
                        )
                    }

                }
            }
        }
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
        if (setOnMap.value == false) {
            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling selectPlace()).

            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling selectPlace()).
            if (token == null) {
                token = AutocompleteSessionToken.newInstance()
            }

            // Use the builder to create a FindAutocompletePredictionsRequest.
            Log.e("cutag", bias.toString())
            val request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .setLocationBias(bias)
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
            searchText.postValue(
                osUtilsProvider.getPlaceFromCoordinates(
                    it.latitude,
                    it.longitude,
                )?.toAddressString()
            )
        }

    }

    private fun changeSetOnMapState(state: Boolean) {
        setOnMap.postValue(state)
        showSetOnMapButton.postValue(!state)
    }

}