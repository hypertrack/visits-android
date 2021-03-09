package com.hypertrack.android.interactors

import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

interface LoginInteractor {
    suspend fun signIn(login: String, password: String): LoginResult
    suspend fun signUp(login: String, password: String): RegisterResult
    suspend fun resendEmailConfirmation()
}

@ExperimentalCoroutinesApi
class LoginInteractorImpl(
    private val cognito: CognitoAccountLoginProvider,
    private val accountRepository: AccountRepository,
    private val tokenService: TokenForPublishableKeyExchangeService
) : LoginInteractor {

    override suspend fun signIn(login: String, password: String): LoginResult {
        val res = getPublishableKey(login, password)
        return when (res) {
            is PublishableKey -> {
                val pkValid = accountRepository.onKeyReceived(res.key, "true")
                if (pkValid) {
                    res
                } else {
                    //todo task
                    LoginError(Exception("Invalid Publishable Key"))
                }
            }
            else -> {
                res
            }
        }
    }

    override suspend fun signUp(login: String, password: String): RegisterResult {
        // get Cognito token
        val res = cognito.awsInitCallWrapper()
        if (res is AwsError) {
            return SignUpError(res.exception)
        }

        // Log.v(TAG, "Initialized with user State $userStateDetails")
        val signUpResult = cognito.awsSignUpCallWrapper(login, password)
        when (signUpResult) {
            is AwsSignUpSuccess -> {
                return SignUpError(Exception("Confirmation request expected, but got success"))
                //todo
//                // Log.v(TAG, "Sign in result $signInResult")
//                val idToken = awsTokenCallWrapper() ?: return LoginError(Exception("Unknown error"))
//                // Log.v(TAG, "Got id token $idToken")
//                val pk = getPublishableKeyFromToken(idToken)
//                AWSMobileClient.getInstance().signOut()
//                // Log.d(TAG, "Got pk $pk")
//                return PublishableKey(pk)
            }
            is AwsSignUpConfirmationRequired -> {
                return ConfirmationRequired
            }
            is AwsSignUpError -> {
                return SignUpError(signUpResult.exception)
            }
        }
    }

    override suspend fun resendEmailConfirmation() {
        TODO("Not yet implemented")
    }

    private suspend fun getPublishableKey(login: String, password: String): LoginResult {

        // get Cognito token
        val res = cognito.awsInitCallWrapper()
        if (res is AwsError) {
            return LoginError(res.exception)
        }

        // Log.v(TAG, "Initialized with user State $userStateDetails")
        val signInResult = cognito.awsLoginCallWrapper(login, password)
        when (signInResult) {
            is AwsSignInSuccess -> {
                // Log.v(TAG, "Sign in result $signInResult")
                val idToken =
                    cognito.awsTokenCallWrapper() ?: return LoginError(Exception("Unknown error"))
                // Log.v(TAG, "Got id token $idToken")
                val pk = getPublishableKeyFromToken(idToken)
                cognito.signOut()
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
            is AwsSignInConfirmationRequired -> {
                return EmailConfirmationRequired
            }
        }
    }

    private suspend fun getPublishableKeyFromToken(token: String): String {
        val response = tokenService.getPublishableKey(token)
        if (response.isSuccessful) return response.body()?.publishableKey ?: ""
        return ""
    }
}

sealed class LoginResult
class PublishableKey(val key: String) : LoginResult()
object NoSuchUser : LoginResult()
object EmailConfirmationRequired : LoginResult()
object InvalidLoginOrPassword : LoginResult()
class LoginError(val exception: Exception) : LoginResult()

sealed class RegisterResult
object ConfirmationRequired : RegisterResult()
class SignUpError(val exception: Exception) : RegisterResult()

