package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.hypertrack.android.models.History
import com.hypertrack.android.models.HistoryTile
import com.hypertrack.android.models.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/** Maps imports swimline */
interface HistoryMapRenderer {
    suspend fun showHistory(history: History): Boolean
    fun onTileSelected(tile: HistoryTile)
}

class GoogleMapHistoryRenderer(private val mapFragment: SupportMapFragment) : HistoryMapRenderer{

    var map: GoogleMap? = null
    var polyLine: Polyline? = null
    var selectedSegment: Polyline? = null
    var viewBounds: LatLngBounds? = null


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
                polyLine = googleMap?.addPolyline(history.asPolylineOptions().color(HISTORY_COLOR))

                if (history.locationTimePoints.isEmpty()) {
                    map?.moveCamera(CameraUpdateFactory.zoomTo(13.0f)) // City level
                } else {
                    viewBounds = history.locationTimePoints.map { it.first }.boundRect()
                    map?.moveCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, VIEW_PADDING))

                }
                continuation.resume(true, null)
            }
        } else {
            Log.d(TAG, "Adding polyline to existing map")
            polyLine?.remove()
            polyLine = map?.addPolyline(history.asPolylineOptions().color(HISTORY_COLOR))
            map?.let { map ->
                history.locationTimePoints.firstOrNull()?.let { point ->
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(point.first.asLatLng(), 13.0f))
                }
            }

            continuation.resume(true, null)
        }
    }

    override fun onTileSelected(tile: HistoryTile) {
        Log.d(TAG, "onTileSelected $tile")
        selectedSegment?.remove()
        map?.let { googleMap ->
            selectedSegment = googleMap.addPolyline(
                tile.locations
                    .map { LatLng(it.latitude, it.longitude) }
                    .fold(PolylineOptions()) { options, loc -> options.add(loc) }
                    .color(SELECTED_SEGMENT_COLOR)
                    .clickable(true)
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(tile.locations.boundRect(), VIEW_PADDING))
            googleMap.setOnMapClickListener {
                Log.d(TAG, "onMapClicked")
                selectedSegment?.remove()
                selectedSegment = null
                viewBounds?.let { bounds ->
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, VIEW_PADDING))
                }

            }

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

private fun Iterable<Location>.boundRect() : LatLngBounds {
    val northEast = LatLng(this.map {it.latitude}.maxOrNull()!!, this.map {it.longitude}.maxOrNull()!!)
    val southWest = LatLng(this.map {it.latitude}.minOrNull()!!, this.map {it.longitude}.minOrNull()!!)
    return LatLngBounds(southWest, northEast)
}

private const val SELECTED_SEGMENT_COLOR = 0xffff0000.toInt()
private const val HISTORY_COLOR = 0xff00ce5b.toInt()

private const val VIEW_PADDING = 32
