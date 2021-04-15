package com.hypertrack.backend

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.hypertrack.backend.deprecated.InternalApiTokenProvider
import com.hypertrack.backend.deprecated.VolleyBasedProvider
import com.hypertrack.backend.models.*
import java.net.HttpURLConnection

private const val TAG = "HybridBackendProvider"

class HybridBackendProvider(
        context: Context,
        publishableKey: String,
        private val deviceID: String,
        baseUrl: String,
        authUrl: String,
        homeManagementApiProvider: com.hypertrack.android.models.HomeManagementApi
) : com.hypertrack.android.models.AbstractBackendProvider, com.hypertrack.android.models.HomeManagementApi by homeManagementApiProvider {
    init { Log.d(TAG, "Initializing with deviceId $deviceID and publishableKey $publishableKey") }

    private val queue = Volley.newRequestQueue(context)
    private val gson = Injector.gson
    private val internalApiIssuesTokenProvider = InternalApiTokenProvider(queue, deviceID, publishableKey, gson, authUrl)
    val backendProvider = VolleyBasedProvider(gson, queue, internalApiIssuesTokenProvider, baseUrl)

    fun start(callback: com.hypertrack.android.models.ResultHandler<String>) {
        Log.i(TAG, "start $deviceID")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.start(deviceID, callback) }
        )
        backendProvider.start(deviceID, retryCallback)
    }

    fun stop() {
        Log.i(TAG, "stop $deviceID")
        val retryCallback = wrapCallback<String>(
                null,
                Runnable { backendProvider.stop(deviceID, null) }
        )
        backendProvider.stop(deviceID, retryCallback)

    }

    override fun createTrip(tripConfig: com.hypertrack.android.models.TripConfig, callback: com.hypertrack.android.models.ResultHandler<com.hypertrack.android.models.ShareableTrip>) {
        Log.i(TAG, "Creating trip with config $tripConfig")
        val retryCallback = wrapCallback<com.hypertrack.android.models.ShareableTrip>(
                callback,
                Runnable { backendProvider.createTrip(tripConfig, callback) }
        )
        backendProvider.createTrip(tripConfig, retryCallback)
    }

    override fun completeTrip(tripId: String, callback: com.hypertrack.android.models.ResultHandler<String>) {
        Log.i(TAG, "Complete trip $tripId")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.completeTrip(tripId, callback) }
        )
        backendProvider.completeTrip(tripId, retryCallback)

    }

    fun getInviteLink(callback: com.hypertrack.android.models.ResultHandler<String>) {
        Log.i(TAG, "getInviteLink")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.getInviteLink(callback) }
        )
        backendProvider.getInviteLink(retryCallback)
    }

    fun getAccountName(callback: com.hypertrack.android.models.ResultHandler<String>) {
        Log.d(TAG, "Requesting account email")
        val retryCallback = wrapCallback<String>(
                callback,
                Runnable { backendProvider.getAccountEmail(callback) }
        )
        backendProvider.getAccountEmail(retryCallback)
    }

    private fun <T> wrapCallback(callback: com.hypertrack.android.models.ResultHandler<T>?, actualCall: Runnable): com.hypertrack.android.models.ResultHandler<T> {
        return object : com.hypertrack.android.models.ResultHandler<T> {
            override fun onResult(result: T) {
                callback?.onResult(result)
            }

            override fun onError(error: Exception) =
                    getErrorHandlerWithTokenAutoRefresh<T>(error, callback) { actualCall.run() }
        }
    }

    private fun <T> getErrorHandlerWithTokenAutoRefresh(
        error: Exception,
        callback: com.hypertrack.android.models.ResultHandler<T>?,
        retryCall: () -> Unit
    ) {
        when (error) {
            is VolleyError -> {
                if (error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    internalApiIssuesTokenProvider.refreshToken(queue) { retryCall() }
                    return
                }
            }
        }
        callback?.onError(error)
    }

    companion object {
        const val TAG = "HybridBackendProvider"

        @JvmStatic
        fun getInstance(context: Context, publishableKey: String, deviceID: String): HybridBackendProvider {
            return HybridBackendProvider(
                    context = context, publishableKey = publishableKey, deviceID = deviceID,
                    baseUrl = Injector.baseUrl, authUrl = Injector.authUrl,
                    homeManagementApiProvider = Injector.getHomeManagementApiProvider(deviceID, publishableKey))
        }
    }
}

internal class GeofenceApiAdapter(private val geofenceApiProvider: GeofencesApiProvider) :
    com.hypertrack.android.models.HomeManagementApi {

    override fun getHomeGeofenceLocation(resultHandler: com.hypertrack.android.models.ResultHandler<com.hypertrack.android.models.GeofenceLocation?>) {

        // Get all geofences
        geofenceApiProvider.getDeviceGeofences(object :
            com.hypertrack.android.models.ResultHandler<Set<Geofence>> {
            override fun onResult(result: Set<Geofence>) {
                Log.d(TAG, "Got geofences $result")
                val homes = result
                        .filter { it.archived != true }
                        .filter { it.metadata?.get("name") == "Home" }
                        .sortedByDescending { it.created_at }
                when {
                    homes.isEmpty() -> {
                        resultHandler.onResult(null)
                    }
                    else -> {
                        val home = homes.first()
                        // return coordinates for latest
                        resultHandler.onResult(
                            com.hypertrack.android.models.GeofenceLocation(
                                home.latitude,
                                home.longitude
                            )
                        )
                    }
                }
                if (homes.size > 1) {
                    homes.subList(1, homes.size).forEach { geofenceApiProvider.deleteGeofence(it.geofence_id) }
                }
            }

            override fun onError(error: Exception) = resultHandler.onError(error)
        })

    }

    override fun updateHomeGeofence(homeLocation: com.hypertrack.android.models.GeofenceLocation, resultHandler: com.hypertrack.android.models.ResultHandler<Void?>) {
        // Get existing home geofences
        geofenceApiProvider.getDeviceGeofences(object :
            com.hypertrack.android.models.ResultHandler<Set<Geofence>> {
            override fun onResult(result: Set<Geofence>) {
                // delete all if present
                result
                        .filter { it.metadata?.get("name") == "Home" }
                        .forEach { geofenceApiProvider.deleteGeofence(it.geofence_id) }
                // create new geofence
                geofenceApiProvider.createGeofences(setOf(GeofenceProperties(
                        Point(listOf(homeLocation.longitude, homeLocation.latitude)),
                        mapOf("name" to "Home"), 100
                )), object : com.hypertrack.android.models.ResultHandler<Set<Geofence>> {
                    override fun onResult(result: Set<Geofence>) {
                        if (result.size == 1) resultHandler.onResult(null)
                        else resultHandler.onError(Exception("No geofence was received in server response"))
                    }

                    override fun onError(error: Exception) = resultHandler.onError(error)
                })
            }

            override fun onError(error: Exception) = resultHandler.onError(error)
        })
    }
}