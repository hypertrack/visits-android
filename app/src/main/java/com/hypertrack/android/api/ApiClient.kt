package com.hypertrack.android.api

import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.utils.Injector
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
        .addConverterFactory(GsonConverterFactory.create(Injector.getGson()))
        .addConverterFactory(ScalarsConverterFactory.create())
        .client(
            OkHttpClient.Builder()
                .authenticator(AccessTokenAuthenticator(accessTokenRepository))
                .addInterceptor(AccessTokenInterceptor(accessTokenRepository))
                .addInterceptor(UserAgentInterceptor())
                .build()
        )
        .build().create(ApiInterface::class.java)

    suspend fun clockIn() = api.clockIn(deviceId)

    suspend fun clockOut() = api.clockOut(deviceId)

    suspend fun getVisits() = api.getVisits(deviceId)

}

