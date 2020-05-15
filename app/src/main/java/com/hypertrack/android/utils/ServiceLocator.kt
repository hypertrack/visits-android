package com.hypertrack.android.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.hypertrack.android.AUTH_URL
import com.hypertrack.android.BASE_URL
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.repository.*
import com.hypertrack.android.view_models.DeliveryListViewModel
import com.hypertrack.sdk.HyperTrack

class ServiceLocator(private val application: Application) {

    fun getDriverRepo() = DriverRepo(getDriver(),getMyPreferences(application))

    fun getAccountRepo() = AccountRepository(getAccountData(), getMyPreferences(application))

    private fun getAccountData(): AccountData = getMyPreferences(application).getAccountData()

    fun getAccessTokenRepository(deviceId : String, userName : String) = BasicAuthAccessTokenRepository(AUTH_URL, deviceId, userName)

    private fun getDriver(): Driver = getMyPreferences(application).getDriverValue()

    private fun getMyPreferences(application: Application): MyPreferences =
        MyPreferences(application.applicationContext, getGson())

    private fun getGson() = Gson()

    fun getHyperTrack(): HyperTrack {
        return HyperTrack.getInstance(application.applicationContext, getAccountRepo().publishableKey)
    }

    fun getDeliveriesApiClient(): ApiClient {
        val accessTokenRepository = getMyPreferences(application).restoreRepository() ?: throw IllegalStateException("No access token repository was saved")
        return ApiClient(accessTokenRepository, BASE_URL, accessTokenRepository.deviceId)
    }

}

fun Application.getServiceLocator() = ServiceLocator(this)

object Injector {
    private fun getGson() = Gson()

    private fun getMyPreferences(context: Context): MyPreferences =
        MyPreferences(context, getGson())

    private fun getDeliveriesApiClient(context: Context): ApiClient {
        val accessTokenRepository = getMyPreferences(context).restoreRepository() ?: throw IllegalStateException("No access token repository was saved")
        return ApiClient(accessTokenRepository, BASE_URL, accessTokenRepository.deviceId)
    }
    private fun getDeliveriesRepo(context: Context): DeliveriesRepository {
        return DeliveriesRepository(
            context,
            getDeliveriesApiClient(context),
            getMyPreferences(context)
        )
    }
    fun provideListActivityViewModelFactory(context: Context): ListActivityViewModelFactory {
        val repository = getDeliveriesRepo(context)
        return ListActivityViewModelFactory(repository)
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
