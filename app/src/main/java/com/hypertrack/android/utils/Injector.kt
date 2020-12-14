package com.hypertrack.android.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geometry
import com.hypertrack.android.api.Point
import com.hypertrack.android.api.Polygon
import com.hypertrack.android.repository.*
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.view_models.*
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.ServiceNotificationConfig


class ServiceLocator {


    fun getAccessTokenRepository(deviceId : String, userName : String) = BasicAuthAccessTokenRepository(
        AUTH_URL, deviceId, userName)

    fun getHyperTrackService(publishableKey: String): HyperTrackService {
        val listener = TrackingState()
        val sdkInstance = HyperTrack
            .getInstance(publishableKey)
            .addTrackingListener(listener)
            .setTrackingNotificationConfig(
                ServiceNotificationConfig.Builder()
                    .setSmallIcon(R.drawable.ic_logo_small)
                    .build()
            )
            .allowMockLocations()

        return HyperTrackService(listener, sdkInstance)
    }

}


object Injector {

    private var visitsRepository: VisitsRepository? = null

    val deeplinkProcessor: DeeplinkProcessor = BranchIoDeepLinkProcessor()

    fun getGson() : Gson = GsonBuilder()
        .registerTypeAdapterFactory(RuntimeTypeAdapterFactory
            .of(Geometry::class.java)
            .registerSubtype(Point::class.java, "Point")
            .registerSubtype(Polygon::class.java, "Polygon"))
        .create()

    private fun getMyPreferences(context: Context): MyPreferences =
        MyPreferences(context, getGson())

    private fun getDriver(context: Context): Driver = getMyPreferences(context).getDriverValue()

    private fun getDriverRepo(context: Context) = DriverRepo(getDriver(context),getMyPreferences(context))

    private fun getVisitsApiClient(context: Context): ApiClient {
        val accessTokenRepository = accessTokenRepository(context)
        return ApiClient(accessTokenRepository,
            BASE_URL, accessTokenRepository.deviceId)
    }

    private fun accessTokenRepository(context: Context) =
        (getMyPreferences(context).restoreRepository()
            ?: throw IllegalStateException("No access token repository was saved"))

    private fun getAccountRepo(context: Context) =
        AccountRepository(ServiceLocator(), getAccountData(context), getMyPreferences(context))

    private fun getAccountData(context: Context): AccountData = getMyPreferences(context).getAccountData()

    private fun getOsUtilsProvider(context: Context) : OsUtilsProvider {
        return OsUtilsProvider(context)

    }

    private fun getHyperTrackService(context: Context): HyperTrackService {
        val myPreferences = getMyPreferences(context)
        val publishableKey = myPreferences.getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        return ServiceLocator().getHyperTrackService(publishableKey)
    }

    private fun getVisitsRepo(context: Context): VisitsRepository {
        visitsRepository?.let { return it }

        getMyPreferences(context).getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        val result = VisitsRepository(
            getOsUtilsProvider(context),
            getVisitsApiClient(context),
            getMyPreferences(context),
            getHyperTrackService(context),
            getAccountRepo(context)
        )
        visitsRepository = result

        return result
    }

    private fun getLoginProvider(context: Context): AccountLoginProvider
            = CognitoAccountLoginProvider(context, LIVE_API_URL_BASE)

    fun provideVisitsManagementViewModelFactory(context: Context): VisitsManagementViewModelFactory {
        val repository = getVisitsRepo(context)
        val accountRepository = getAccountRepo(context)
        return VisitsManagementViewModelFactory(repository, accountRepository, accessTokenRepository(context))
    }

    fun provideVisitStatusViewModel(context: Context, visitId:String): VisitDetailsViewModel {
        return VisitDetailsViewModel(getVisitsRepo(context), visitId)
    }

    fun provideDriverLoginViewModelFactory(context: Context) : DriverLoginViewModelFactory {
        return DriverLoginViewModelFactory(getDriverRepo(context), getHyperTrackService(context))
    }

    fun provideAccountLoginViewModelFactory(context: Context) : AccountLoginViewModelFactory {
        return AccountLoginViewModelFactory(getLoginProvider(context), getAccountRepo(context))
    }


    fun provideSplashScreenViewModelFactory(context: Context): SplashScreenViewModelFactory {
        return SplashScreenViewModelFactory(getDriverRepo(context), getAccountRepo(context))

    }
}

class VisitsManagementViewModelFactory(
    private val visitsRepository: VisitsRepository,
    val accountRepository: AccountRepository,
    val accessTokenRepository: AccessTokenRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            VisitsManagementViewModel::class.java -> return VisitsManagementViewModel(
                visitsRepository,
                accountRepository,
                accessTokenRepository
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

class DriverLoginViewModelFactory(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            DriverLoginViewModel::class.java -> return DriverLoginViewModel(driverRepo, hyperTrackService) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

class AccountLoginViewModelFactory(
    private val accountLoginProvider: AccountLoginProvider,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            AccountLoginViewModel::class.java -> return AccountLoginViewModel(accountLoginProvider, accountRepository) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

class SplashScreenViewModelFactory(
    private val driverRepo: DriverRepo,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            SplashScreenViewModel::class.java -> return SplashScreenViewModel(
                driverRepo,
                accountRepository
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

interface AccountPreferencesProvider {
    val isManualCheckInAllowed: Boolean
    val isAutoCheckInEnabled: Boolean
}

const val BASE_URL = "https://live-app-backend.htprod.hypertrack.com/"
const val LIVE_API_URL_BASE  = "https://live-api.htprod.hypertrack.com/"
const val AUTH_URL = LIVE_API_URL_BASE + "authenticate"
