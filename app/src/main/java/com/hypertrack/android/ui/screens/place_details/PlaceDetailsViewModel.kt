package com.hypertrack.android.ui.screens.place_details

import android.content.Intent
import android.graphics.Color
import android.hardware.camera2.params.ColorSpaceTransform
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceMarker
import com.hypertrack.android.models.Integration
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.ZipLiveData
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.ui.common.toAddressString
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.R
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val geofenceId: String,
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val moshi: Moshi
) : BaseViewModel() {

    private val map = MutableLiveData<GoogleMap>()

    val loadingState = MutableLiveData<Boolean>(false)

    private val geofence = MutableLiveData<Geofence>().apply {
        try {
            postValue(placesRepository.getGeofence(geofenceId))
        } catch (e: Exception) {
            //todo handle
        }
    }

    val address = Transformations.map(geofence) { geofence ->
        geofence.fullAddress ?: osUtilsProvider.getPlaceFromCoordinates(
            geofence.latitude,
            geofence.longitude
        )?.toAddressString()
    }

    val metadata: LiveData<List<KeyValueItem>> = Transformations.map(geofence) { geofence ->
        (geofence.metadata?.filter { it.value is String } ?: mapOf())
            .toMutableMap().apply {
                put(
                    osUtilsProvider.stringFromResource(R.string.place_visits_count),
                    geofence.visitsCount.toString()
                )
//                put("created_at", geofence.created_at.toString())
            }
            .map { KeyValueItem(it.key, it.value as String) }.toList()
    }

    val integration: LiveData<Integration?> = Transformations.map(geofence) {
        it.getIntegration(moshi)
    }

    val visits: LiveData<List<GeofenceMarker>> = Transformations.map(geofence) { geofence ->
        geofence.marker?.markers?.sortedByDescending { it.arrival!!.recordedAt } ?: listOf()
    }

    val externalMapsIntent = MutableLiveData<Intent>()

    init {
        //todo check leak
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

    private fun displayGeofenceLocation(geofence: Geofence, googleMap: GoogleMap) {
        geofence.radius?.let { radius ->
            googleMap.addCircle(
                CircleOptions()
                    .center(geofence.latLng)
                    .fillColor(osUtilsProvider.colorFromResource(R.color.colorGeofenceFill))
                    .strokeColor(osUtilsProvider.colorFromResource(R.color.colorGeofence))
                    .strokeWidth(3f)
                    .radius(radius.toDouble())
                    .visible(true)
            )
        }
        googleMap.addCircle(
            CircleOptions()
                .center(geofence.latLng)
                .fillColor(osUtilsProvider.colorFromResource(R.color.colorGeofence))
                .strokeColor(Color.TRANSPARENT)
                .radius(30.0)
                .visible(true)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geofence.latLng, 15.0f))
    }

    fun onDirectionsClick() {
//        val gmmIntentUri = Uri.parse("google.navigation:q=${geofence.value!!.latitude},${geofence.value!!.longitude}")

        val googleMapsUrl = "https://www.google.com/maps/dir/?api=1&" +
                "destination=${geofence.value!!.latitude},${geofence.value!!.longitude}"

        val gmmIntentUri = Uri.parse(googleMapsUrl)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        externalMapsIntent.postValue(mapIntent)
//        mapIntent.setPackage("com.google.android.apps.maps")
    }

    fun onAddressClick() {
        if (!address.value.isNullOrEmpty()) {
            osUtilsProvider.copyToClipboard(address.value!!)
        }
    }

    fun onCopyValue(value: String) {
        if (value.isNotEmpty()) {
            osUtilsProvider.copyToClipboard(value)
        }
    }

    fun onCopyVisitIdClick(str: String) {
        osUtilsProvider.copyToClipboard(str)
    }

    fun onIntegrationCopy() {
        integration.value?.let {
            osUtilsProvider.copyToClipboard(it.id)
        }
    }
}