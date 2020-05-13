package com.hypertrack.android.api

import com.hypertrack.android.repository.AccessTokenRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class ApiClient(
    accessTokenRepository: AccessTokenRepository,
    baseUrl:String,
    private val deviceId: String
) {

    val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                .build()
        )
        .build().create(ApiInterface::class.java)

    suspend fun checkinCall() = api.makeDriverCheckIn(deviceId)

    suspend fun checkoutCall() = api.makeDriverCheckOut(deviceId)

    suspend fun getGeofences() = api.getDeliveries(deviceId)

}

