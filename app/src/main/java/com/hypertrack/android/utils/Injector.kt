package com.hypertrack.android.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.hypertrack.android.api.*
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.repository.*
import com.hypertrack.android.view_models.*

class ServiceLocator(private val context: Context) {


    fun getAccessTokenRepository(deviceId : String, userName : String) = BasicAuthAccessTokenRepository(
        AUTH_URL, deviceId, userName)

    fun getHyperTrackService(publishableKey: String): HyperTrackService {
        return HyperTrackService(
            publishableKey,
            context
        )
    }

}


object Injector {

    private var visitsRepository: VisitsRepository? = null

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
        val accessTokenRepository =
            getMyPreferences(context).restoreRepository()
                ?: throw IllegalStateException("No access token repository was saved")
        return ApiClient(accessTokenRepository,
            BASE_URL, accessTokenRepository.deviceId)
    }

    private fun getAccountRepo(context: Context) =
        AccountRepository(ServiceLocator(context), getAccountData(context), getMyPreferences(context))

    private fun getAccountData(context: Context): AccountData = getMyPreferences(context).getAccountData()

    private fun getOsUtilsProvider(context: Context) : OsUtilsProvider {
        return OsUtilsProvider(context)

    }

    private fun getHyperTrackService(context: Context): HyperTrackService {
        val myPreferences = getMyPreferences(context)
        val publishableKey = myPreferences.getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        return HyperTrackService(
            publishableKey,
            context
        )
    }

    private fun getVisitsRepo(context: Context): VisitsRepository {
        visitsRepository?.let { return it }

        getMyPreferences(context).getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        val result = VisitsRepository(
            getOsUtilsProvider(context),
            getVisitsApiClient(context),
            getMyPreferences(context),
            getHyperTrackService(context)
        )
        visitsRepository = result

        return result
    }

    fun provideListActivityViewModelFactory(context: Context): ListActivityViewModelFactory {
        val repository = getVisitsRepo(context)
        return ListActivityViewModelFactory(repository)
    }

    fun provideVisitStatusViewModel(context: Context, visitId:String): VisitDetailsViewModel {
        return VisitDetailsViewModel(getVisitsRepo(context), visitId)
    }

    fun provideCheckinViewModelFactory(context: Context) : CheckinViewModelFactory {
        return CheckinViewModelFactory(getDriverRepo(context), getHyperTrackService(context))
    }

    fun provideSplashScreenViewModelFactory(context: Context): SplashScreenViewModelFactory {
        return SplashScreenViewModelFactory(getDriverRepo(context), getAccountRepo(context))

    }
}

class ListActivityViewModelFactory(
    private val visitsRepository: VisitsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            VisitsManagementViewModel::class.java -> return VisitsManagementViewModel(visitsRepository) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

class CheckinViewModelFactory(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            CheckInViewModel::class.java -> return CheckInViewModel(driverRepo, hyperTrackService) as T
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

const val BASE_URL = "https://live-app-backend.htprod.hypertrack.com/"
const val AUTH_URL = "https://live-api.htprod.hypertrack.com/authenticate"