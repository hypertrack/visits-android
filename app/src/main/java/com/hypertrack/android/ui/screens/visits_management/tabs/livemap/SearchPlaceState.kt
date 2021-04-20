package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.util.Log
import com.hypertrack.android.models.GeofenceLocation
import com.hypertrack.android.models.HomeManagementApi
import com.hypertrack.android.models.HomeUpdateResultError
import com.hypertrack.android.models.HomeUpdateResultSuccess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class SearchPlaceState(
    context: Context,
    val mode: String,
    private val backendProvider: HomeManagementApi
) : BaseState(context) {
    var destination: PlaceModel? = null
    val home: PlaceModel? = sharedHelper.homePlace
    var mapDestinationMode = false

    private val _recentPlaces: MutableSet<PlaceModel?> = sharedHelper.recentPlaces?.toMutableSet()?: mutableSetOf()

    fun saveHomePlace(home: PlaceModel?) {
        sharedHelper.homePlace = home
        home?.let {
            GlobalScope.launch {
                createGeofenceOnPlatform(it)
            }
        }
    }

    private suspend fun createGeofenceOnPlatform(home: PlaceModel) {

        val homeLocation = GeofenceLocation(
            home.latLng!!.latitude,
            home.latLng!!.longitude
        )
        when (val res = backendProvider.updateHomeLocation(homeLocation)) {
            HomeUpdateResultSuccess -> Log.d(TAG, "Geofence was created")
            is HomeUpdateResultError -> Log.w(TAG, "Can't update geofence", res.error)
        }

    }

    val recentPlaces: List<PlaceModel> = _recentPlaces.filterNotNull().reversed()

    fun addPlaceToRecent(placeModel: PlaceModel) {
        _recentPlaces.remove(placeModel)
        placeModel.isRecent = true
        _recentPlaces.add(placeModel)
        sharedHelper.recentPlaces = _recentPlaces
    }

    companion object {
        private const val TAG = "SearchPlaceState"
    }
}