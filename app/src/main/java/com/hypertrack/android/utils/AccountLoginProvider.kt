package com.hypertrack.android.utils

import android.content.Context
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.client.results.SignInResult
import com.amazonaws.mobile.client.results.SignInState
import com.amazonaws.mobile.client.results.Tokens
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface AccountLoginProvider {
    /** @return publishable key for an account or empty string if fails */
    //todo fix return
    suspend fun getPublishableKey(login: String, password: String): LoginResult
}

sealed class LoginResult
class PublishableKey(val key: String) : LoginResult()
object NoSuchUser : LoginResult()
object InvalidLoginOrPassword : LoginResult()
class LoginError(val exception: Exception) : LoginResult()

sealed class AwsSignInResult
object AwsSignInSuccess : AwsSignInResult()
class AwsSignInError(val exception: Exception) : AwsSignInResult()

class CognitoAccountLoginProvider(private val ctx: Context, private val baseApiUrl: String) :
    AccountLoginProvider {

    @Suppress("UNUSED_VARIABLE")
    @ExperimentalCoroutinesApi
    override suspend fun getPublishableKey(login: String, password: String): LoginResult {

        // get Cognito token
        val userStateDetails = awsInitCallWrapper() ?: return LoginError(Exception("Unknown error"))

        // Log.v(TAG, "Initialized with user State $userStateDetails")
        val signInResult = awsLoginCallWrapper(login, password)
        when (signInResult) {
            is AwsSignInSuccess -> {
                // Log.v(TAG, "Sign in result $signInResult")
                val idToken = awsTokenCallWrapper() ?: return LoginError(Exception("Unknown error"))
                // Log.v(TAG, "Got id token $idToken")
                val pk = getPublishableKeyFromToken(idToken)
                AWSMobileClient.getInstance().signOut()
                // Log.d(TAG, "Got pk $pk")
                return PublishableKey(pk)
            }
            is AwsSignInError -> {
                signInResult.exception.let {
                    return when (it) {
                        is UserNotFoundException -> {
                            NoSuchUser
                        }
                        is NotAuthorizedException -> {
                            InvalidLoginOrPassword
                        }
                        else -> {
                            LoginError(it)
                        }
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsInitCallWrapper(): UserStateDetails? {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().initialize(ctx, object : Callback<UserStateDetails> {
                override fun onResult(result: UserStateDetails?) = it.resume(result) {}
                override fun onError(e: Exception?) = it.resume(null) {}
            })
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsLoginCallWrapper(login: String, password: String): AwsSignInResult {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().signIn(
                login,
                password,
                emptyMap(),
                object : Callback<SignInResult> {
                    override fun onResult(result: SignInResult) {
                        when (result.signInState) {
                            SignInState.DONE -> {
                                it.resume(AwsSignInSuccess) {}
                            }
                            else -> {
                                it.resume(AwsSignInError(Exception(result.signInState.toString()))) {}
                            }
                        }

                    }

                    override fun onError(e: Exception) {
                        it.resume(AwsSignInError(e)) {}
                    }
                })
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun awsTokenCallWrapper(): String? {
        return suspendCancellableCoroutine {
            AWSMobileClient.getInstance().getTokens(object : Callback<Tokens> {
                override fun onResult(result: Tokens?) = it.resume(result?.idToken?.tokenString) {}
                override fun onError(e: Exception?) = it.resume(null) {}
            })
        }
    }

    private suspend fun getPublishableKeyFromToken(token: String): String {
        val retrofit = Retrofit.Builder()
                .baseUrl(baseApiUrl)
                .addConverterFactory(MoshiConverterFactory.create(Injector.getMoshi()))
                .build()
        val service = retrofit.create(TokenForPublishableKeyExchangeService::class.java)
        val response = service.getPublishableKey(token)
        if (response.isSuccessful) return response.body()?.publishableKey ?: ""
        return ""
    }


    companion object {
        const val TAG = "CognitoAccountProvider"
    }
}

interface TokenForPublishableKeyExchangeService {
    @GET("api-key")
    suspend fun getPublishableKey(@Header("Authorization") token: String): Response<PublishableKeyContainer>
}

@JsonClass(generateAdapter = true)
data class PublishableKeyContainer(@field:Json(name = "key") val publishableKey: String?)