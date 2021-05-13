package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.api.TripParams
import com.hypertrack.android.models.AbstractBackendProvider
import com.hypertrack.android.models.CreateTripError
import com.hypertrack.android.models.ShareableTripSuccess
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.logistics.android.github.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class SearchPlacePresenter @SuppressLint("MissingPermission") constructor(
    private val context: Context,
    private val view: View,
    private val backendProvider: AbstractBackendProvider,
    private val deviceId: String,
    private val viewLifecycleOwner: LifecycleOwner,
    private val state: SearchPlaceState,
    private val visitsRepository: VisitsRepository
) {
    private val placesClient: PlacesClient  = Places.createClient(context)
    private var bias: RectangularBounds? = null
    private val handler = Handler()
    private var googleMap: GoogleMap? = null
    private var token: AutocompleteSessionToken? = null

    init {
        LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                bias = RectangularBounds.newInstance(
                    LatLng(location.latitude - 0.1, location.longitude + 0.1),  // SW
                    LatLng(location.latitude + 0.1, location.longitude - 0.1) // NE
                )
            }
        }
    }

    fun initMap(googleMap: GoogleMap) {
        this.googleMap = googleMap

    }

    fun applyMode() = view.updateHomeAddress(state.home)

    fun setMapDestinationModeEnable(enable: Boolean) {
        if (state.mapDestinationMode != enable) {
            state.mapDestinationMode = enable
            if (enable) {
                val runnable = Runnable {
                    googleMap?.let { map ->
                        state.destination = null
                        view.updateAddress(context.getString(R.string.searching_))
                        val target = map.cameraPosition.target
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            val detectedAddress = MapUtils.getLocationAddress(context, target)
                            val destination = PlaceModel().apply {
                                address = detectedAddress
                                latLng = target
                            }
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main){
                                state.destination = destination
                                view.updateAddress(detectedAddress)
                            }
                        }
                    }
                }
                googleMap?.setOnCameraMoveListener {
                    handler.removeCallbacks(runnable)
                    handler.postDelayed(runnable, 500)
                }
                runnable.run()
                view.updateList(emptyList())
                view.showSetOnMap()
            } else {
                googleMap?.setOnCameraMoveListener(null)
                state.destination = null
                view.updateAddress("")
                view.hideSetOnMap()
            }
        }
    }

    fun search(query: String?) {
        if (!state.mapDestinationMode) {
            if (TextUtils.isEmpty(query)) {
                view.updateList(state.recentPlaces)
            } else {
                // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
                // and once again when the user makes a selection (for example when calling selectPlace()).
                token = AutocompleteSessionToken.newInstance()

                // Use the builder to create a FindAutocompletePredictionsRequest.
                val request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.GEOCODE)
                    .setSessionToken(token)
                    .setQuery(query)
                if (bias != null) {
                    request.locationBias = bias
                }
                placesClient.findAutocompletePredictions(request.build())
                    .addOnSuccessListener { response ->
                        view.updateList(
                            PlaceModel.from(response.autocompletePredictions)
                        )
                    }
                    .addOnFailureListener { e ->
                        if (e is ApiException) {
                            Log.e(TAG, "Place not found: " + e.statusCode)
                        }
                    }
            }
        }
    }

    fun confirm() { state.destination?.let { providePlace(it) } }

    fun selectHome() = providePlace(state.home)

    fun selectItem(placeModel: PlaceModel) {
        view.showProgressBar()
        if (!placeModel.isRecent) {
            state.addPlaceToRecent(placeModel)
        }
        val fields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        )
        val request = FetchPlaceRequest.builder(placeModel.placeId, fields)
            .setSessionToken(token)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { fetchPlaceResponse ->
                view.hideProgressBar()
                val destination = PlaceModel()
                destination.address = fetchPlaceResponse.place.address
                destination.latLng = fetchPlaceResponse.place.latLng
                state.destination = destination
                providePlace(destination)
            }.addOnFailureListener { e ->
                view.hideProgressBar()
                if (e is ApiException) {
                    Log.e(TAG, "Place not found: " + e.statusCode)
                }
            }
    }

    fun skip() {
        state.saveHomePlace(null)
        view.finish()
    }

    fun providePlace(placeModel: PlaceModel?) {
        startTrip(placeModel)
    }

    private fun startTrip(destination: PlaceModel?) {
        view.showProgressBar()
        val tripRequest: TripParams = destination?.let {
            destination.latLng?.let {
                TripParams(deviceId, it.latitude, it.longitude)
            }
        } ?: TripParams(deviceId)
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = backendProvider.createTrip(tripRequest)) {
                is ShareableTripSuccess -> {
                    Log.d(TAG, "trip is created: $result")
                    visitsRepository.refreshVisits()
                    view.hideProgressBar()
                    view.finish()
                }
                is CreateTripError -> {
                    Log.e(TAG, "Trip start failure", result.error)
                    view.hideProgressBar()
                    Toast.makeText(context, "Trip start failure", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    fun destroy() {
        googleMap?.setOnCameraMoveListener(null)
        googleMap = null
    }

    interface View {
        fun updateConnectionStatus(offline: Boolean)
        fun updateAddress(address: String?)
        fun updateHomeAddress(home: PlaceModel?)
        fun updateList(list: List<PlaceModel>)
        fun showHomeAddress()
        fun hideHomeAddress()
        fun showSetOnMap()
        fun hideSetOnMap()
        fun showProgressBar()
        fun hideProgressBar()
        fun finish()
    }

    companion object {
        private const val TAG = "SPlacePresenter"
    }

}