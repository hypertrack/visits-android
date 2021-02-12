package com.hypertrack.android.ui.screens.visits_management.tabs

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.hypertrack.android.models.History
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/** Maps imports swimline */
interface HistoryMapRenderer { suspend fun showHistory(history: History): Boolean }



class GoogleMapHistoryRenderer(private val mapFragment: SupportMapFragment): HistoryMapRenderer {

    var map: GoogleMap? = null
    var polyLine: Polyline? = null


    @ExperimentalCoroutinesApi
    override suspend fun showHistory(history: History) = suspendCancellableCoroutine<Boolean> {
        if (map == null) {
            mapFragment.getMapAsync { googleMap ->
                map = googleMap
                polyLine = googleMap?.addPolyline(history.asPolylineOptions())
                it.resume(true, null)
            }
        } else {
            polyLine?.remove()
            polyLine = map?.addPolyline(history.asPolylineOptions())
            it.resume(true, null)
        }
    }
}

private fun History.asPolylineOptions(): PolylineOptions = this
    .locationTimePoints
    .map { it.first }
    .fold(PolylineOptions()) {
            options, point ->  options.add(LatLng(point.latitude, point.longitude))
    }