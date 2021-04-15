package com.hypertrack.backend

import com.hypertrack.backend.models.Geofence
import com.hypertrack.backend.models.GeofenceProperties

interface AsyncTokenProvider {
    fun getAuthenticationToken(resultHandler: com.hypertrack.android.models.ResultHandler<String>)
}

interface GeofencesApiProvider {
    fun getDeviceGeofences(callback: com.hypertrack.android.models.ResultHandler<Set<Geofence>>)
    fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: com.hypertrack.android.models.ResultHandler<Set<Geofence>>)
    fun deleteGeofence(geofence_id: String)
}

