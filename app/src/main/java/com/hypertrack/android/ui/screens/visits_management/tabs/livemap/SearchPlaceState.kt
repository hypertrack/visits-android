package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.os.Handler
import android.util.Log
import com.hypertrack.backend.HomeManagementApi
import com.hypertrack.backend.ResultHandler
import com.hypertrack.backend.models.GeofenceLocation
import java.util.concurrent.TimeUnit

internal class SearchPlaceState(
    context: Context,
    val mode: String,
    private val mBackendProvider: HomeManagementApi
) : BaseState(context) {
    var destination: PlaceModel? = null
    val home: PlaceModel? = sharedHelper.homePlace
    var mapDestinationMode = false

    private val _recentPlaces: MutableSet<PlaceModel?> = sharedHelper.recentPlaces?.toMutableSet()?: mutableSetOf()

    fun saveHomePlace(home: PlaceModel?) {
        sharedHelper.homePlace = home
        home?.let { createGeofenceOnPlatform(it) }
    }

    private fun createGeofenceOnPlatform(home: PlaceModel) {
        mBackendProvider.updateHomeGeofence(GeofenceLocation(
            home.latLng!!.latitude,
            home.latLng!!.longitude
        ),
            object : ResultHandler<Void?> {
                override fun onResult(result: Void?) {
                    Log.d(TAG, "Geofence was created")
                }
                override fun onError(error: Exception) {
                    val handler = Handler(mContext.mainLooper)
                    handler.postDelayed(
                        { createGeofenceOnPlatform(home) },
                        TimeUnit.SECONDS.toMillis(5)
                    )
                }
            }
        )
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