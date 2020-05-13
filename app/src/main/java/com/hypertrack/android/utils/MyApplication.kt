package com.hypertrack.android.utils

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.hypertrack.android.BASE_URL
import com.hypertrack.android.api.ApiInterface
import com.hypertrack.sdk.HyperTrack
import io.branch.referral.Branch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MyApplication : Application() {

    private var retrofit: Retrofit? = null

    private var appServices: ApiInterface? = null

    companion object {
        const val TAG = "MyApplication"

        // using this we can check which activity is visible or not
        // so that we can perform particular action
        var activity: AppCompatActivity? = null
    }
    override fun onCreate() {
        super.onCreate()

        when {
            MyPreferences(this, Gson()).getAccountData().publishableKey == null -> {
                Log.i(TAG, "No pk found, initializing Branch IO")
                // First run - get the pk
                Branch.enableLogging()
                Branch.getAutoInstance(this)
            }

        }

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
//            .addInterceptor()
//        okHttpClient.addInterceptor(object : Interceptor {
//            override fun intercept(chain: Interceptor.Chain): Response {
//
//                val request = chain.request()
//                val response = chain.proceed(request)
//                val body = response.body
//                val bodyString = body!!.string()
//                val contentType = body.contentType()
//                return response.newBuilder().body(bodyString.toResponseBody(contentType)).build()
//            }
//
//        })

        // connection timeouts
        okHttpClient.callTimeout(20, TimeUnit.SECONDS)
        okHttpClient.connectTimeout(20, TimeUnit.SECONDS)
        okHttpClient.readTimeout(20, TimeUnit.SECONDS)
        okHttpClient.writeTimeout(20, TimeUnit.SECONDS)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        appServices = retrofit?.create(ApiInterface::class.java)

    }
}