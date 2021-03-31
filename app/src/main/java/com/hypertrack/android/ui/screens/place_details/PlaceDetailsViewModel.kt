package com.hypertrack.android.ui.screens.place_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.utils.OsUtilsProvider

class PlaceDetailsViewModel(
    val geofenceId: String,
    val placesRepository: PlacesRepository,
    val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    private val map = MutableLiveData<GoogleMap>()

    private val geofence = MutableLiveData<Geofence>().apply {
        try {
            postValue(placesRepository.getGeofence(geofenceId))
        } catch (e: Exception) {
            //todo handle
        }
    }

    val address = Transformations.map(geofence) { geofence ->
        geofence.fullAddress ?: osUtilsProvider
            .getPlaceFromCoordinates(geofence.latitude, geofence.longitude)
            ?.let { "${it.locality}, ${it.thoroughfare ?: "${it.latitude}, ${it.longitude}"}" }
    }
    val metadata: LiveData<List<KeyValueItem>> = Transformations.map(geofence) { geofence ->
        geofence.metadata?.filter { it.value is String }
            ?.map { KeyValueItem(it.key, it.value as String) }?.toList() ?: listOf()
    }

    init {
        ZipLiveData(geofence, map).apply {
            observeForever {
                displayGeofenceLocation(it.first, it.second)
            }
        }
    }

    fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.apply {
            isScrollGesturesEnabled = false
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
        }
        map.postValue(googleMap)

    }

    fun displayGeofenceLocation(geofence: Geofence, googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions().position(geofence.latLng).title(geofence.name)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofence.latLng, 13.0f))
    }

}