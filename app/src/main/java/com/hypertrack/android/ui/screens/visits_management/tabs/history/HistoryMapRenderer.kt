package com.hypertrack.android.ui.screens.visits_management.tabs.history

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Marker
import com.hypertrack.android.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/** Maps imports swimline */
interface HistoryMapRenderer {
    suspend fun showHistory(history: History): Boolean
    fun onTileSelected(tile: HistoryTile)
}

interface HistoryStyle {
    val activeColor: Int
    val driveSelectionColor: Int
    val walkSelectionColor: Int
    val stopSelectionColor: Int
    val outageSelectionColor: Int
    val mapPadding: Int
    fun colorForStatus(status: Status): Int
    fun markerForStatus(status: Status): Bitmap
}

class GoogleMapHistoryRenderer(
    private val mapFragment: SupportMapFragment,
    private val style: HistoryStyle,
    ) : HistoryMapRenderer{

    private var map: GoogleMap? = null
    private var polyLine: Polyline? = null
    private var selectedSegment: Polyline? = null
    private var viewBounds: LatLngBounds? = null
    private val activeMarkers = mutableListOf<Marker>()


    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    override suspend fun showHistory(history: History) = suspendCancellableCoroutine<Boolean> { continuation ->
        Log.d(TAG, "Showing history $history")
        if (map == null) {
            Log.d(TAG, "Map haven't been yet initialized")
            mapFragment.getMapAsync { googleMap ->
                Log.d(TAG,  "google map async callback")
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isZoomControlsEnabled = true
                map = googleMap
                polyLine = googleMap?.addPolyline(history.asPolylineOptions().color(style.activeColor))

                if (history.locationTimePoints.isEmpty()) {
                    map?.animateCamera(CameraUpdateFactory.zoomTo(13.0f)) // City level
                } else {
                    viewBounds = history.locationTimePoints.map { it.first }.boundRect()
                    map?.animateCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, style.mapPadding))

                }
                continuation.resume(true, null)
            }
        } else {
            Log.d(TAG, "Adding polyline to existing map")
            polyLine?.remove()
            polyLine = map?.addPolyline(history.asPolylineOptions().color(style.activeColor))
            map?.let { map ->
                history.locationTimePoints.firstOrNull()?.let { point ->
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(point.first.asLatLng(), 13.0f))
                }
            }

            continuation.resume(true, null)
        }
    }

    override fun onTileSelected(tile: HistoryTile) {
        Log.d(TAG, "onTileSelected $tile")
        if (tile.tileType == HistoryTileType.SUMMARY) return

        selectedSegment?.remove()
        activeMarkers.forEach { it.remove() }
        map?.let { googleMap ->
            selectedSegment = googleMap.addPolyline(
                tile.locations
                    .map { LatLng(it.latitude, it.longitude) }
                    .fold(PolylineOptions()) { options, loc -> options.add(loc) }
                    .color(style.colorForStatus(tile.status))
                    .clickable(true)
            )
            tile.locations.firstOrNull()?.let {
                activeMarkers.add(addMarker(it, googleMap, tile.address, tile.status))
            }
            tile.locations.lastOrNull()?.let {
                activeMarkers.add(addMarker(it, googleMap, tile.address, tile.status))
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(tile.locations.boundRect(), style.mapPadding))
            googleMap.setOnMapClickListener {
                Log.d(TAG, "onMapClicked")
                selectedSegment?.remove()
                activeMarkers.forEach { it.remove() }
                activeMarkers.clear()
                selectedSegment = null
                viewBounds?.let { bounds ->
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, style.mapPadding))
                }

            }

        }
    }

    private fun addMarker(
        location: Location,
        map: GoogleMap,
        address: CharSequence?,
        status: Status
    ): Marker {
        val markerOptions = MarkerOptions().position(LatLng(location.latitude, location.longitude))
            .icon(BitmapDescriptorFactory.fromBitmap(style.markerForStatus(status)))
        address?.let { markerOptions.title(it.toString()) }
        return map.addMarker(markerOptions)
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
