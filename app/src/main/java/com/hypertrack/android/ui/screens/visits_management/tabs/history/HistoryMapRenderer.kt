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
    val summaryPeekHeight: Int
    fun colorForStatus(status: Status): Int
    fun markerForStatus(status: Status): Bitmap
}

interface DeviceLocationProvider {
    fun getCurrentLocation(block: (l: Location?) -> Unit)
}

class GoogleMapHistoryRenderer(
    private val mapFragment: SupportMapFragment,
    private val style: HistoryStyle,
    private val locationProvider: DeviceLocationProvider,
    ) : HistoryMapRenderer{

    private var map: GoogleMap? = null
    private var polyLine: Polyline? = null
    private var selectedSegment: Polyline? = null
    private var viewBounds: LatLngBounds? = null
    private val activeMarkers = mutableListOf<Marker>()


    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    override suspend fun showHistory(history: History) = suspendCancellableCoroutine<Boolean> { continuation ->
        if (map == null) {
            mapFragment.getMapAsync { googleMap ->
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.setPadding(0, 0, 0, style.summaryPeekHeight )
                map = googleMap

                if (history.locationTimePoints.isEmpty()) {
                    locationProvider.getCurrentLocation {
                        if (it != null) {
                            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), CITY_LEVEL_ZOOM))
                        } else {
//                            map?.animateCamera(CameraUpdateFactory.zoomBy(CITY_LEVEL_ZOOM))
                        }
                    }
                } else {
                    polyLine = googleMap?.addPolyline(history.asPolylineOptions().color(style.activeColor))
                    viewBounds = history.locationTimePoints.map { it.first }.boundRect()
                    map?.animateCamera(CameraUpdateFactory.newLatLngBounds(viewBounds, style.mapPadding))

                }
                continuation.resume(true, null)
            }
        } else {
            polyLine?.remove()
            polyLine = map?.addPolyline(history.asPolylineOptions().color(style.activeColor))
            map?.let { map ->
                history.locationTimePoints.sortedByDescending { it.second }.firstOrNull()
                    ?.let { point ->
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                point.first.asLatLng(),
                                CITY_LEVEL_ZOOM
                            )
                        )
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

    companion object {
        const val TAG = "HistoryMapRenderer"
        const val CITY_LEVEL_ZOOM = 13.0f
    }
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
