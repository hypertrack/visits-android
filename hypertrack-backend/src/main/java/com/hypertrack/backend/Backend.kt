package com.hypertrack.backend

import androidx.annotation.FloatRange
import com.hypertrack.backend.models.Geofence
import com.hypertrack.backend.models.GeofenceProperties

interface AsyncTokenProvider {
    fun getAuthenticationToken(resultHandler: ResultHandler<String>)
}

interface GeofencesApiProvider {
    fun getDeviceGeofences(callback: ResultHandler<Set<Geofence>>)
    fun createGeofences(geofencesProperties: Set<GeofenceProperties>, callback: ResultHandler<Set<Geofence>>)
    fun deleteGeofence(geofence_id: String)
}

class TripConfig internal constructor(
        val latitude: Double?,
        val longitude: Double?,
        val deviceId: String
) {

    fun getRequestBody(): String {
        return if (latitude == null || longitude == null) {
            "{\"device_id\":\"${deviceId}\"}"
        } else {
            "{\"destination\":{\"geometry\":{\"coordinates\":[${longitude},${latitude}],\"type\":\"Point\"}},\"device_id\":\"${deviceId}\"}"
        }
    }

    class Builder {
        private var destinationLatitude: Double? = null
        private var destinationLongitude: Double? = null
        private var deviceId : String? = null

        /**
         * @param latitude of trip's destination. Negative values are for southern hemisphere.
         * Unless both latitude and longitude are set values are ignored.
         * @see .setDestinationLongitude
         */
        fun setDestinationLatitude(@FloatRange(from = -90.0, to = 90.0) latitude: Double): Builder {
            destinationLatitude = latitude
            return this
        }

        /**
         * @param longitude of trip's destination. Negative values are for western hemisphere.
         * Unless both latitude and longitude are set values are ignored.
         * @see .setDestinationLatitude
         */
        fun setDestinationLongitude(@FloatRange(from = -180.0, to = 180.0) longitude: Double): Builder {
            destinationLongitude = longitude
            return this
        }

        fun setDeviceId(deviceId: String): Builder {
            this.deviceId = deviceId
            return this
        }

        fun build(): TripConfig {
            requireNotNull(deviceId)
            return TripConfig(
                destinationLatitude,
                destinationLongitude,
                deviceId ?: throw NullPointerException("device id can't be null")
            )
        }
    }

}

interface ResultHandler<T> {
    fun onResult(result: T)
    fun onError(error: Exception)
}