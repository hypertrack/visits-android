package com.hypertrack.android.ui.screens.add_place

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails
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

    private var firstLaunch: Boolean = true
    private var programmaticCameraMove: Boolean = false
    private val currentLocation = MutableLiveData<Location>()
    val places = MutableLiveData<List<PlaceModel>>()
    val loadingState = MutableLiveData<Boolean>(false)
    val map = MutableLiveData<GoogleMap>()
    val searchText = MutableLiveData<String>()
    val error = SingleLiveEvent<String>()

    private var currentPlace: Place? = null

    //todo persist token?
    private var token: AutocompleteSessionToken? = null
    private var bias: RectangularBounds? = null

    init {
        deviceLocationProvider.getCurrentLocation {
            currentLocation.postValue(it)
        }
    }

    init {
        ZipLiveData(currentLocation, map).apply {
            //todo check leak
            observeForever { pair ->
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
        map.postValue(googleMap)
        try {
            googleMap.isMyLocationEnabled = true
        } catch (_: Exception) {
        }
        googleMap.setOnCameraIdleListener {
            if (/*!firstLaunch &&*/ !programmaticCameraMove) {
                map.value?.cameraPosition?.target?.let {
                    currentPlace = null
                    searchText.postValue(
                        osUtilsProvider.getPlaceFromCoordinates(
                            it.latitude,
                            it.longitude,
                        )?.toAddressString()
                    )
                }
            }
            firstLaunch = false
            programmaticCameraMove = false
        }
        googleMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
        }
    }

    fun onSearchQueryChanged(query: String) {

        currentPlace = null
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling selectPlace()).
        if (token == null) {
            token = AutocompleteSessionToken.newInstance()
        }

        val requestBuilder = FindAutocompletePredictionsRequest.builder()
//                .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .setLocationBias(bias)
        currentLocation.value?.let {
            requestBuilder.setOrigin(LatLng(it.latitude, it.longitude))
        }

        placesClient.findAutocompletePredictions(requestBuilder.build())
            .addOnSuccessListener { response ->
                places.postValue(PlaceModel.from(response.autocompletePredictions))
            }
            .addOnFailureListener { e ->
                places.postValue(listOf())
                error.postValue(e.message)
            }
    }

    fun onConfirmClicked(address: String) {
        proceed(map.value!!.cameraPosition.target, address)
    }

    fun onPlaceItemClick(item: PlaceModel) {
        val placeFields: List<Place.Field> =
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.NAME,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG
            )
        val request = FetchPlaceRequest.newInstance(item.placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                place.latLng?.let { ll ->
                    map.value!!.let {
                        currentPlace = place
                        places.postValue(listOf())
                        searchText.postValue(place.toAddressString())
                        moveMapCamera(ll.latitude, ll.longitude)
                    }
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
                address = address,
                name = currentPlace?.name
            )
        )
    }

    private fun moveMapCamera(latitude: Double, longitude: Double) {
        programmaticCameraMove = true
        map.value!!.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    latitude,
                    longitude
                ), 13f
            )
        )
    }

}