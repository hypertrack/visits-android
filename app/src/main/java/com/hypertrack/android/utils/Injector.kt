package com.hypertrack.android.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.hypertrack.android.api.ApiClient
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

    private var deliveriesRepository: DeliveriesRepository? = null

    private fun getGson() = Gson()

    private fun getMyPreferences(context: Context): MyPreferences =
        MyPreferences(context, getGson())

    private fun getDriver(context: Context): Driver = getMyPreferences(context).getDriverValue()

    private fun getDriverRepo(context: Context) = DriverRepo(getDriver(context),getMyPreferences(context))

    private fun getDeliveriesApiClient(context: Context): ApiClient {
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

    private fun getDeliveriesRepo(context: Context): DeliveriesRepository {
        deliveriesRepository?.let { return it }

        getMyPreferences(context).getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        val result = DeliveriesRepository(
            getOsUtilsProvider(context),
            getDeliveriesApiClient(context),
            getMyPreferences(context),
            getHyperTrackService(context)
        )
        deliveriesRepository = result

        return result
    }

    fun provideListActivityViewModelFactory(context: Context): ListActivityViewModelFactory {
        val repository = getDeliveriesRepo(context)
        return ListActivityViewModelFactory(repository)
    }

    fun provideDeliveryStatusViewModel(context: Context, deliveryId:String): DeliveryStatusViewModel {
        return DeliveryStatusViewModel(getDeliveriesRepo(context), deliveryId)
    }

    fun provideCheckinViewModelFactory(context: Context) : CheckinViewModelFactory {
        return CheckinViewModelFactory(getDriverRepo(context), getHyperTrackService(context), getDeliveriesApiClient(context))
    }

    fun provideSplashScreenViewModelFactory(context: Context): SplashScreenViewModelFactory {
        return SplashScreenViewModelFactory(getDriverRepo(context), getAccountRepo(context))

    }
}

class ListActivityViewModelFactory(
    private val deliveriesRepository: DeliveriesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            DeliveryListViewModel::class.java -> return DeliveryListViewModel(deliveriesRepository) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}

class CheckinViewModelFactory(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService,
    private val deliveriesApiClient: ApiClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        when (modelClass) {
            CheckInViewModel::class.java -> return CheckInViewModel(driverRepo, hyperTrackService, deliveriesApiClient) as T
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