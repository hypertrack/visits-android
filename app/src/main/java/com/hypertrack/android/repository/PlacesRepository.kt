package com.hypertrack.android.repository

import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceProperties
import com.hypertrack.android.models.GeofenceMetadata
import com.hypertrack.android.models.Integration
import com.hypertrack.android.ui.common.nullIfEmpty
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
        address: String? = null,
        description: String? = null,
        integration: Integration? = null
    ): CreateGeofenceResult {
        //todo handle error
        val res = apiClient.createGeofence(
            latitude, longitude, GeofenceMetadata(
                name = name.nullIfEmpty() ?: integration?.name,
                integration = integration,
                description = description.nullIfEmpty(),
                address = address.nullIfEmpty()
            )
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