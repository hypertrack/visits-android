package com.hypertrack.android.repository

import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceProperties
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.HttpException

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

    suspend fun createGeofence(
        latitude: Double,
        longitude: Double,
        name: String? = null,
        address: String? = null
    ): CreateGeofenceResult {
        //todo handle error
        val res = apiClient.createGeofence(
            latitude, longitude, mutableMapOf<String, String>().apply {
                name?.let {
                    put("name", it)
                }
                address?.let {
                    put("address", it)
                }
            }
        )
        if (res.isSuccessful) {
            geofences.postValue(
                geofences.value!!.toMutableList().apply { add(res.body()!!.first()) })
            return CreateGeofenceSuccess
        } else {
            return CreateGeofenceError(HttpException(res))
        }
    }
}

sealed class CreateGeofenceResult
object CreateGeofenceSuccess : CreateGeofenceResult()
class CreateGeofenceError(val e: Exception) : CreateGeofenceResult()