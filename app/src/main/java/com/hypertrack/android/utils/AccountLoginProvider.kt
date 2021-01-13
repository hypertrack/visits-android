package com.hypertrack.android.utils

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.Tokens
import com.squareup.moshi.Json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface AccountLoginProvider {
    /** @return publishable key for an account or empty string if fails */
    suspend fun getPublishableKey(login: String, password: String) : String
}

class CognitoAccountLoginProvider(private val ctx: Context, private val baseApiUrl: String) : AccountLoginProvider {
    @Suppress("UNUSED_VARIABLE")
    @ExperimentalCoroutinesApi
    override suspend fun getPublishableKey(login: String, password: String): String {

        // get Cognito token
        val userStateDetails = awsInitCallWrapper() ?: return ""
        // Log.v(TAG, "Initialized with user State $userStateDetails")
        val signInResult = awsLoginCallWrapper(login, password) ?: return ""
        // Log.v(TAG, "Sign in result $signInResult")
        val idToken = awsTokenCallWrapper() ?: return ""
        // Log.v(TAG, "Got id token $idToken")
        val pk = getPublishableKeyFromToken(idToken)
        AWSMobileClient.getInstance().signOut()
        // Log.d(TAG, "Got pk $pk")
        return pk
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsInitCallWrapper() : UserStateDetails? {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().initialize(ctx, object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails?) = it.resume(result) {}
                override fun onError(e: Exception?) = it.resume(null) {}
            })
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsLoginCallWrapper(login: String, password: String) : SignInResult? {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().signIn(login, password, emptyMap(), object : Callback<SignInResult> {
                override fun onResult(result: SignInResult?) = it.resume(result) {}
                override fun onError(e: Exception?) = it.resume(null) {}
            })
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsTokenCallWrapper() : String? {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().getTokens(object : Callback<Tokens> {
                override fun onResult(result: Tokens?) = it.resume(result?.idToken?.tokenString) {}
                override fun onError(e: Exception?) = it.resume(null) {}
            })
        }
    }

    private suspend fun getPublishableKeyFromToken(token: String) : String {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseApiUrl)
            .addConverterFactory(MoshiConverterFactory.create(Injector.getMoshi()))
            .build()
        val service = retrofit.create(TokenForPublishableKeyExchangeService::class.java)
        val response = service.getPublishableKey(token)
        if (response.isSuccessful) return response.body()?.publishableKey?:""
        return ""
    }


    companion object {const val TAG = "CognitoAccountProvider"}
}

interface TokenForPublishableKeyExchangeService {
    @GET("api-key")
    suspend fun getPublishableKey(@Header("Authorization") token: String) : Response<PublishableKeyContainer>
}

data class PublishableKeyContainer(@Json(name = "key")val publishableKey: String?)