package com.hypertrack.android.interactors

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.hypertrack.android.api.BackendException
import com.hypertrack.android.api.LiveAccountApi
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.toBase64
import com.hypertrack.android.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.HttpException
import java.util.*

interface LoginInteractor {
    suspend fun signIn(email: String, password: String): LoginResult
    suspend fun signUp(
        login: String,
        password: String,
        userAttributes: Map<String, String>
    ): RegisterResult

    suspend fun resendEmailConfirmation(email: String)
    suspend fun verifyByOtpCode(email: String, code: String): OtpResult

    companion object UserAttrs {
        const val COMPANY_KEY = "custom:company"

        const val USE_CASE_KEY = "custom:use_case"
        const val USE_CASE_DELIVERIES = "deliveries"
        const val USE_CASE_VISITS = "visits"
        const val USE_CASE_RIDES = "rides"

        const val STATE_KEY = "custom:state"
        const val STATE_MY_FLEET = "my_workforce"
        const val STATE_MY_CUSTOMERS_FLEET = "my_customer"
    }
}

@ExperimentalCoroutinesApi
class LoginInteractorImpl(
    private val cognito: CognitoAccountLoginProvider,
    private val accountRepository: AccountRepository,
    private val driverRepository: DriverRepository,
    private val tokenService: TokenForPublishableKeyExchangeService,
    private val liveAccountUrlService: LiveAccountApi
) : LoginInteractor {

    override suspend fun signIn(email: String, password: String): LoginResult {
        val res = getPublishableKey(email.toLowerCase(Locale.getDefault()), password)
        when (res) {
            is PublishableKey -> {
                return try {
                    val success = loginWithPublishableKey(res.key, email)
                    if (success) {
                        res
                    } else {
                        LoginError(Exception("Invalid publishable key"))
                    }
                } catch (e: Exception) {
                    LoginError(e)
                }
            }
            else -> {
                return res
            }
        }
    }

    private suspend fun loginWithPublishableKey(key: String, email: String): Boolean {
        val pkValid = accountRepository.onKeyReceived(key, "true")
        if (pkValid) {
            driverRepository.driverId = email
            return true
        } else {
            return false
        }
    }

    override suspend fun signUp(
        login: String,
        password: String,
        userAttributes: Map<String, String>
    ): RegisterResult {
        // get Cognito token
        val res = cognito.awsInitCallWrapper()
        if (res is AwsError) {
            return SignUpError(res.exception)
        }

        // Log.v(TAG, "Initialized with user State $userStateDetails")
        val signUpResult =
            cognito.awsSignUpCallWrapper(
                login.toLowerCase(Locale.getDefault()),
                password,
                userAttributes
            )
        when (signUpResult) {
            is AwsSignUpSuccess -> {
                //todo
                return SignUpError(Exception("Confirmation request expected, but got success"))
            }
            is AwsSignUpConfirmationRequired -> {
                return ConfirmationRequired
            }
            is AwsSignUpError -> {
                return SignUpError(signUpResult.exception)
            }
        }
    }

    override suspend fun verifyByOtpCode(email: String, code: String): OtpResult {
        val res = liveAccountUrlService.verifyEmailViaOtpCode(
            "Basic ${MyApplication.SERVICES_API_KEY.toBase64()}",
            LiveAccountApi.OtpBody(
                email = email,
                code = code
            )
        )
        if (res.isSuccessful) {
            return try {
                val pk = res.body()!!.publishableKey
                val success = loginWithPublishableKey(pk, email)
                if (success) {
                    OtpSuccess
                } else {
                    OtpError(Exception("Invalid publishable key"))
                }
            } catch (e: Exception) {
                OtpError(e)
            }
        } else {
            BackendException(res).let {
                return when (it.statusCode) {
                    "CodeMismatchException" -> {
                        OtpWrongCode
                    }
                    "NotAuthorizedException" -> {
                        if (it.message == "User cannot be confirmed. Current status is CONFIRMED") {
                            OtpSignInRequired
                        } else {
                            OtpError(it)
                        }
                    }
                    else -> {
                        OtpError(it)
                    }
                }
            }
        }
    }

    override suspend fun resendEmailConfirmation(email: String) {
        val res = liveAccountUrlService.resendOtpCode(
            "Basic ${MyApplication.SERVICES_API_KEY.toBase64()}",
            LiveAccountApi.ResendBody(email)
        )
        if (!res.isSuccessful) {
            throw HttpException(res)
        }
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
                val tokenRes = cognito.awsTokenCallWrapper()
                return when (tokenRes) {
                    is CognitoTokenError -> {
                        LoginError(Exception("Failed to retrieve Cognito token"))
                    }
                    is CognitoToken -> {
                        val pk = getPublishableKeyFromToken(tokenRes.token)
                        // Log.d(TAG, "Got pk $pk")
                        PublishableKey(pk)
                    }
                }
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

sealed class OtpResult
object OtpSuccess : OtpResult()
object OtpSignInRequired : OtpResult()
class OtpError(val exception: Exception) : OtpResult()
object OtpWrongCode : OtpResult()

