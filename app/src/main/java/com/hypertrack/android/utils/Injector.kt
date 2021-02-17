package com.hypertrack.android.utils

import android.content.Context
import androidx.fragment.app.FragmentFactory
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.api.*
import com.hypertrack.android.repository.*
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.ui.common.ViewModelFactory
import com.hypertrack.android.ui.screens.visits_management.tabs.GoogleMapHistoryRenderer
import com.hypertrack.android.ui.screens.visits_management.tabs.HistoryMapRenderer
import com.hypertrack.android.ui.screens.visits_management.tabs.MapViewFragment
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.ServiceNotificationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.recipes.RuntimeJsonAdapterFactory
import javax.inject.Provider


class ServiceLocator {


    fun getAccessTokenRepository(deviceId: String, userName: String) = BasicAuthAccessTokenRepository(
            AUTH_URL, deviceId, userName)

    fun getHyperTrackService(publishableKey: String): HyperTrackService {
        val listener = TrackingState()
        val sdkInstance = HyperTrack
                .getInstance(publishableKey)
                .addTrackingListener(listener)
                .setTrackingNotificationConfig(
                        ServiceNotificationConfig.Builder()
                                .setSmallIcon(R.drawable.ic_notif_logo_small)
                                .build()
                )
                .allowMockLocations()

        return HyperTrackService(listener, sdkInstance)
    }

}


object Injector {

    private var visitsRepository: VisitsRepository? = null

    private val crashReportsProvider: CrashReportsProvider by lazy { FirebaseCrashReportsProvider() }

    val deeplinkProcessor: DeeplinkProcessor = BranchIoDeepLinkProcessor(crashReportsProvider)

    fun getMoshi(): Moshi = Moshi.Builder()
            .add(HistoryCoordinateJsonAdapter())
            .add(GeometryJsonAdapter())
            .add(
                    RuntimeJsonAdapterFactory(HistoryMarker::class.java, "type")
                            .registerSubtype(HistoryStatusMarker::class.java, "device_status")
                            .registerSubtype(HistoryTripMarker::class.java, "trip_marker")
                            .registerSubtype(HistoryGeofenceMarker::class.java, "geofence")
            )
            .build()

    private fun getMyPreferences(context: Context): MyPreferences =
            MyPreferences(context, getMoshi())

    private fun getDriver(context: Context): Driver = getMyPreferences(context).getDriverValue()

    fun getDriverRepo(context: Context) = DriverRepository(
            getDriver(context),
            getMyPreferences(context),
            crashReportsProvider
    )

    private fun getVisitsApiClient(context: Context): ApiClient {
        val accessTokenRepository = accessTokenRepository(context)
        return ApiClient(accessTokenRepository,
                BASE_URL, accessTokenRepository.deviceId)
    }

    private fun accessTokenRepository(context: Context) =
            (getMyPreferences(context).restoreRepository()
                    ?: throw IllegalStateException("No access token repository was saved"))

    fun getAccountRepo(context: Context) =
            AccountRepository(ServiceLocator(), getAccountData(context), getMyPreferences(context))

    private fun getAccountData(context: Context): AccountData = getMyPreferences(context).getAccountData()

    private fun getOsUtilsProvider(context: Context): OsUtilsProvider {
        return OsUtilsProvider(context, crashReportsProvider)

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
                getAccountRepo(context),
                getImageDecoder(),
                crashReportsProvider
        )
        visitsRepository = result

        return result
    }

    fun getHistoryMapRenderer(supportMapFragment: SupportMapFragment): HistoryMapRenderer
        = GoogleMapHistoryRenderer(supportMapFragment)

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(
                getVisitsApiClient(MyApplication.context),
                crashReportsProvider,
                getOsUtilsProvider(MyApplication.context)
        )
    }

    private fun getImageDecoder(): ImageDecoder = SimpleImageDecoder()

    private fun getLoginProvider(context: Context): AccountLoginProvider
            = CognitoAccountLoginProvider(context, LIVE_API_URL_BASE)

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        return ViewModelFactory(
                context,
                accessTokenRepository(context),
                getVisitsRepo(context),
                historyRepository,
                getAccountRepo(context),
                getDriverRepo(context),
                crashReportsProvider,
                getHyperTrackService(context),
                getLoginProvider(context)
        )
    }

    fun provideVisitStatusViewModel(context: Context, visitId: String): VisitDetailsViewModel {
        return VisitDetailsViewModel(getVisitsRepo(context), visitId)
    }

    private fun getMapHistoryFragmentProvider(context: Context) = Provider {
        MapViewFragment(provideViewModelFactory(context), HistoryRendererFactory())
    }

    fun getFragmentFactory(context: Context): FragmentFactory =
            CustomFragmentFactory(getMapHistoryFragmentProvider(context))
}

class HistoryRendererFactory {
    fun create(supportMapFragment: SupportMapFragment) = Injector.getHistoryMapRenderer(supportMapFragment)
}

interface AccountPreferencesProvider {
    var wasWhitelisted: Boolean
    val isManualCheckInAllowed: Boolean
    val isAutoCheckInEnabled: Boolean
    val isPickUpAllowed: Boolean
}

const val BASE_URL = "https://live-app-backend.htprod.hypertrack.com/"
const val LIVE_API_URL_BASE = "https://live-api.htprod.hypertrack.com/"
const val AUTH_URL = LIVE_API_URL_BASE + "authenticate"
const val MAX_IMAGE_SIDE_LENGTH_PX = 1024
