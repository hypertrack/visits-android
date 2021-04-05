package com.hypertrack.android.repository

import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceProperties

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

    suspend fun createGeofence(latitude: Double, longitude: Double, name: String?) {
        //todo handle error
        val res = apiClient.createGeofence(
            latitude, longitude, name?.let {
                mapOf(
                    "name" to name
                )
            } ?: mapOf()
        )
        if (res.isSuccessful) {
            geofences.postValue(
                geofences.value!!.toMutableList().apply { add(res.body()!!.first()) })
        }
    }


}