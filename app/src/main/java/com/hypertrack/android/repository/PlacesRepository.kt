package com.hypertrack.android.repository

import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence

class PlacesRepository(
    private val apiClient: ApiClient
) {

    val geofences = MutableLiveData<List<Geofence>>()

    suspend fun refreshGeofences() {
        //todo handle error
        val res = apiClient.getGeofences()
        geofences.postValue(res)
    }

    fun getGeofence(geofenceId: String): Geofence {
        //todo handle null
        return geofences.value?.filter { it._id == geofenceId }?.firstOrNull()!!
    }
}