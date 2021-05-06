package com.hypertrack.android.interactors

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException
import com.hypertrack.android.api.LiveAccountApi
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.utils.*
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

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

        coEvery {
            awsSignUpCallWrapper(
                "login",
                "password",
                mapOf()
            )
        } returns AwsSignUpConfirmationRequired

        coEvery { awsTokenCallWrapper() } returns CognitoToken("cognito token")

        every { signOut() } returns Unit
    }
    private var accountRepository: AccountRepository = mockk {
        coEvery { onKeyReceived("pk", any(), any()) } returns true
    }
    private var driverRepository: DriverRepository = mockk {
        coEvery { driverId = any() } returns Unit
    }
    private var tokenService = mockk<TokenForPublishableKeyExchangeService> {
        coEvery { getPublishableKey("cognito token") } returns Response.success(
            PublishableKeyContainer("pk")
        )
    }
    private var liveAccountUrlService = mockk<LiveAccountApi> {
        coEvery {
            verifyEmailViaOtpCode(
                any(),
                respMatch(LiveAccountApi.OtpBody("email", "123456"))
            )
        } returns Response.success(LiveAccountApi.PublishableKeyResponse("pk"))
    }

    private fun MockKMatcherScope.respMatch(e: LiveAccountApi.OtpBody) =
        match<LiveAccountApi.OtpBody> { e1 ->
            e.email == e1.email && e.code == e1.code
        }

    private lateinit var loginInteractor: LoginInteractor

    @Before
    fun before() {
        loginInteractor = LoginInteractorImpl(
            accountLoginProvider,
            accountRepository,
            driverRepository,
            tokenService,
            liveAccountUrlService,
            "services api key"
        )
    }

    @Test
    fun `sign in success flow`() {
        runBlocking {
            val res = loginInteractor.signIn("login", "password")
            assertEquals("pk", (res as PublishableKey).key)
            verify { driverRepository.driverId = "login" }
        }
    }

    @Test
    fun `sign in wrong password`() {
        runBlocking {
            val res = loginInteractor.signIn("login", "wrong password")
            assertTrue(res is InvalidLoginOrPassword)
            verify(exactly = 0) { driverRepository.driverId = any() }
        }
    }

    @Test
    fun `sign in wrong login`() {
        runBlocking {
            val res = loginInteractor.signIn("wrong login", "password")
            assertTrue(res is NoSuchUser)
            verify(exactly = 0) { driverRepository.driverId = any() }
        }
    }

    @Test
    fun `sign in needs email confirmation`() {
        runBlocking {
            val res = loginInteractor.signIn("login needs confirmation", "password")
            assertEquals(EmailConfirmationRequired::class.java, res::class.java)
            verify(exactly = 0) { driverRepository.driverId = any() }
        }
    }

    @Test
    fun `sign up success flow`() {
        runBlocking {
            val res = loginInteractor.signUp("login", "password", mapOf())
            assertEquals(ConfirmationRequired::class.java, res::class.java)
            verify(exactly = 0) { driverRepository.driverId = any() }
        }
    }

    @Test
    fun `login after email confirmation`() {
        runBlocking {
            val res = loginInteractor.verifyByOtpCode("email", "123456")
            assertEquals(OtpSuccess::class.java, res::class.java)
            verify { driverRepository.driverId = "email" }
        }
    }

}