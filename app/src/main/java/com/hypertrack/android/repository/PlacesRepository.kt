package com.hypertrack.android.repository

import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceProperties
import com.hypertrack.android.api.GeofenceResponse
import com.hypertrack.android.models.GeofenceMetadata
import com.hypertrack.android.models.Integration
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.common.nullIfEmpty
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import retrofit2.HttpException

class PlacesRepository(
    private val apiClient: ApiClient,
    private val integrationsRepository: IntegrationsRepository
) {

    private val geofencesCache = mutableMapOf<String, Geofence>()

    fun refresh() {
        integrationsRepository.invalidateCache()
    }

    suspend fun loadPage(pageToken: String?): GeofenceResponse {
        val res = apiClient.getGeofences(pageToken)
        res.geofences.forEach { geofencesCache.put(it.geofence_id, it) }
        return res
    }

    fun getGeofence(geofenceId: String): Geofence {
        return geofencesCache.getValue(geofenceId)
    }

    suspend fun createGeofence(
        latitude: Double,
        longitude: Double,
        name: String? = null,
        address: String? = null,
        description: String? = null,
        integration: Integration? = null
    ): CreateGeofenceResult {
        try {
            val res = apiClient.createGeofence(
                latitude, longitude, GeofenceMetadata(
                    name = name.nullIfEmpty() ?: integration?.name,
                    integration = integration,
                    description = description.nullIfEmpty(),
                    address = address.nullIfEmpty()
                )
            )
            if (res.isSuccessful) {
                return CreateGeofenceSuccess
            } else {
                return CreateGeofenceError(HttpException(res))
            }
        } catch (e: Exception) {
            return CreateGeofenceError(e)
        }
    }
}

sealed class CreateGeofenceResult
object CreateGeofenceSuccess : CreateGeofenceResult()
class CreateGeofenceError(val e: Exception) : CreateGeofenceResult()