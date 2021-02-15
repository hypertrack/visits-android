package com.hypertrack.android.ui.screens.visits_management.tabs

import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.hypertrack.android.models.History
import com.hypertrack.android.models.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/** Maps imports swimline */
interface HistoryMapRenderer { suspend fun showHistory(history: History): Boolean }



class GoogleMapHistoryRenderer(private val mapFragment: SupportMapFragment): HistoryMapRenderer {

    var map: GoogleMap? = null
    var polyLine: Polyline? = null


    @ExperimentalCoroutinesApi
    override suspend fun showHistory(history: History) = suspendCancellableCoroutine<Boolean> { continuation ->
        Log.d(TAG, "Showing history $history")
        if (map == null) {
            Log.d(TAG, "Map haven't been yet initialized")
            mapFragment.getMapAsync { googleMap ->
                Log.d(TAG,  "google map async callback")
                googleMap.uiSettings.isMyLocationButtonEnabled = true
                googleMap.uiSettings.isZoomControlsEnabled = true
                map = googleMap
                polyLine = googleMap?.addPolyline(history.asPolylineOptions())

                if (history.locationTimePoints.isEmpty()) {
                    map?.moveCamera(CameraUpdateFactory.zoomTo(13.0f)) // City level
                } else {
                    val locations = history.locationTimePoints.map { it.first }
                    val northEast = LatLng(locations.map {it.latitude}.maxOrNull()!!, locations.map {it.longitude}.maxOrNull()!!)
                    val southWest = LatLng(locations.map {it.latitude}.minOrNull()!!, locations.map {it.longitude}.minOrNull()!!)
                    val bounds = LatLngBounds(southWest, northEast)
                    map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0))

                }
                continuation.resume(true, null)
            }
        } else {
            Log.d(TAG, "Adding polyline to existing map")
            polyLine?.remove()
            polyLine = map?.addPolyline(history.asPolylineOptions())
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(history.locationTimePoints.first().first.asLatLng(), 13.0f))
            continuation.resume(true, null)
        }
    }

    companion object { const val TAG = "HistoryMapRenderer" }
}

private fun Location.asLatLng(): LatLng = LatLng(latitude, longitude)

private fun History.asPolylineOptions(): PolylineOptions = this
    .locationTimePoints
    .map { it.first }
    .fold(PolylineOptions()) {
            options, point ->  options.add(LatLng(point.latitude, point.longitude))
    }