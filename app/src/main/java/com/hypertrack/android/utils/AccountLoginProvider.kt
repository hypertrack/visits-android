package com.hypertrack.android.utils

import android.content.Context
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.Tokens
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface AccountLoginProvider {
    suspend fun getPublishableKey(login: String, password: String) : String
}

interface TokenForPublishableKeyExchangeService {
    @GET("api-key")
    suspend fun getPublishableKey(@Header("Authorization") token: String) : Response<PublishableKeyContainer>
}

class CognitoAccountLoginProvider(val ctx: Context) : AccountLoginProvider {
    @ExperimentalCoroutinesApi
    override suspend fun getPublishableKey(login: String, password: String): String {

        // get Cognito token
        val userStateDetails = awsInitCallWrapper() ?: return ""
        Log.d(TAG, "Initialized with user State $userStateDetails")
        val signInResult = awsLoginCallWrapper(login, password) ?: return ""
        Log.d(TAG, "Sign in result $signInResult")
        val idToken = awsTokenCallWrapper() ?: return ""
        Log.d(TAG, "Got id token $idToken")
        val pk = getPublishableKeyFromToken(idToken)
        AWSMobileClient.getInstance().signOut()
        Log.d(TAG, "Got pk $pk")
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
                override fun onResult(result: Tokens?) {
                    it.resume(result?.idToken?.tokenString) {}
                }

                override fun onError(e: Exception?) = it.resume(null) {}

            })
        }
    }

    private suspend fun getPublishableKeyFromToken(token: String) : String {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://live-api.htprod.hypertrack.com/")
            .addConverterFactory(GsonConverterFactory.create(Injector.getGson()))
            .build()
        val service = retrofit.create(TokenForPublishableKeyExchangeService::class.java)
        val response = service.getPublishableKey(token)
        if (response.isSuccessful) return response.body()?.publishableKey?:""
        return ""
    }


    companion object {const val TAG = "CognitoAccountProvider"}
}

data class PublishableKeyContainer(
    @SerializedName("key") val publishableKey: String?
)