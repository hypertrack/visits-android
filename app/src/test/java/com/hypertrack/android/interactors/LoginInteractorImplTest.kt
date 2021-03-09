package com.hypertrack.android.interactors

import android.os.Build
import com.amazonaws.mobile.client.UserState
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.utils.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@ExperimentalCoroutinesApi
class LoginInteractorImplTest {

    private var accountLoginProvider: CognitoAccountLoginProviderImpl = mockk {
        coEvery { awsInitCallWrapper() } returns AwsSuccess

        coEvery { awsLoginCallWrapper("login", "password") } returns AwsSignInSuccess
        coEvery {
            awsLoginCallWrapper(
                "login needs confirmation",
                "password"
            )
        } returns AwsSignInConfirmationRequired
        coEvery {
            awsLoginCallWrapper(
                and(not("login"), not("login needs confirmation")),
                any()
            )
        } returns AwsSignInError(
            UserNotFoundException("")
        )
        coEvery { awsLoginCallWrapper("login", not("password")) } returns AwsSignInError(
            NotAuthorizedException("")
        )

        coEvery { awsSignUpCallWrapper("login", "password") } returns AwsSignUpConfirmationRequired

        coEvery { awsTokenCallWrapper() } returns "cognito token"

        every { signOut() } returns Unit
    }
    private var accountRepository: AccountRepository = mockk {
        coEvery { onKeyReceived("pk", any(), any()) } returns true
    }
    private var tokenService = mockk<TokenForPublishableKeyExchangeService> {
        coEvery { getPublishableKey("cognito token") } returns Response.success(
            PublishableKeyContainer("pk")
        )
    }
    private lateinit var loginInteractor: LoginInteractor

    @Before
    fun before() {
        loginInteractor = LoginInteractorImpl(
            accountLoginProvider,
            accountRepository,
            tokenService
        )
    }

    @Test
    fun `sign in success flow`() {
        runBlocking {
            val res = loginInteractor.signIn("login", "password")
            assertEquals("pk", (res as PublishableKey).key)
        }
    }

    @Test
    fun `sign in wrong password`() {
        runBlocking {
            val res = loginInteractor.signIn("login", "wrong password")
            assertTrue(res is InvalidLoginOrPassword)
        }
    }

    @Test
    fun `sign in wrong login`() {
        runBlocking {
            val res = loginInteractor.signIn("wrong login", "password")
            assertTrue(res is NoSuchUser)
        }
    }

    @Test
    fun `sign in invalid data`() {
        throw NotImplementedError()
        runBlocking {
            val res = loginInteractor.signIn("login", "password")
            assertEquals("pk", (res as PublishableKey).key)
        }
    }

    @Test
    fun `sign in needs email confirmation`() {
        runBlocking {
            val res = loginInteractor.signIn("login needs confirmation", "password")
            assertEquals(EmailConfirmationRequired::class.java, res::class.java)
        }
    }

    @Test
    fun `sign up success flow`() {
        runBlocking {
            val res = loginInteractor.signUp("login", "password")
            assertEquals(ConfirmationRequired::class.java, res::class.java)
        }
    }

    @Test
    fun `sign up invalid data`() {
        throw NotImplementedError()
        runBlocking {
            val res = loginInteractor.signIn("login", "password")
            assertEquals("pk", (res as PublishableKey).key)
        }
    }

}