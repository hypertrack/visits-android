package com.hypertrack.android.utils

import android.app.Application
import android.os.Build
import android.os.Debug
import androidx.appcompat.app.AppCompatActivity
import com.hypertrack.android.BASE_URL
import com.hypertrack.android.api_interface.ApiInterface
import com.hypertrack.sdk.HyperTrack
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    private var retrofit: Retrofit? = null

    private var appServices: ApiInterface? = null

    companion object {

        // using this we can check which activity is visible or not
        // so that we can perform particular action
    var activity: AppCompatActivity? = null
}
    override fun onCreate() {
        super.onCreate()

        initRetrofitClient()

        HyperTrack.enableDebugLogging()
    }

    // Get App Client reference
    fun getApiClient(): ApiInterface {
        if (appServices == null) throw Throwable("Some Error. Please restart app")
        return appServices!!
    }

    // create retrofit client for first time when app initialise
    private fun initRetrofitClient() {
        val okHttpClient = OkHttpClient.Builder()
        okHttpClient.addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {

                val request = chain.request()
                val response = chain.proceed(request)
                val body = response.body
                val bodyString = body!!.string()
                val contentType = body.contentType()
                return response.newBuilder().body(bodyString.toResponseBody(contentType)).build()
            }

        })

        // connection timeouts
        okHttpClient.callTimeout(20, TimeUnit.SECONDS)
        okHttpClient.connectTimeout(20, TimeUnit.SECONDS)
        okHttpClient.readTimeout(20, TimeUnit.SECONDS)
        okHttpClient.writeTimeout(20, TimeUnit.SECONDS)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        appServices = retrofit?.create(ApiInterface::class.java)

    }
}