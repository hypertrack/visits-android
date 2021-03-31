package com.hypertrack.android.utils

import android.content.Context
import com.google.android.gms.maps.SupportMapFragment
import com.hypertrack.android.RetryParams
import com.hypertrack.android.api.*
import com.hypertrack.android.interactors.*
import com.hypertrack.android.repository.*
import com.hypertrack.android.ui.common.ParamViewModelFactory
import com.hypertrack.android.ui.common.UserScopeViewModelFactory
import com.hypertrack.android.ui.common.ViewModelFactory
import com.hypertrack.android.ui.screens.visits_management.tabs.history.BaseHistoryStyle
import com.hypertrack.android.ui.screens.visits_management.tabs.history.GoogleMapHistoryRenderer
import com.hypertrack.android.ui.screens.visits_management.tabs.history.HistoryMapRenderer
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.ServiceNotificationConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.recipes.RuntimeJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object ServiceLocator {

    fun getAccessTokenRepository(deviceId: String, userName: String) =
        BasicAuthAccessTokenRepository(
            AUTH_URL, deviceId, userName
        )

    fun getHyperTrackService(publishableKey: String): HyperTrackService {
        val listener = TrackingState()
        val sdkInstance = HyperTrack
            .getInstance(publishableKey)
            .addTrackingListener(listener)
            .backgroundTrackingRequirement(false)
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

    private var userScope: UserScope? = null

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

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        return ViewModelFactory(
            getAccountRepo(context),
            getDriverRepo(context),
            crashReportsProvider,
            getPermissionInteractor(),
            getLoginInteractor(),
            getOsUtilsProvider(MyApplication.context),
        )
    }

    fun <T> provideParamVmFactory(param: T): ParamViewModelFactory<T> {
        return ParamViewModelFactory(
            param,
            getUserScope().placesRepository,
            getOsUtilsProvider(MyApplication.context)
        )
    }

    fun provideUserScopeViewModelFactory(): UserScopeViewModelFactory {
        return getUserScope().userScopeViewModelFactory
    }

    fun provideVisitStatusViewModel(context: Context, visitId: String): VisitDetailsViewModel {
        return VisitDetailsViewModel(getVisitsRepo(context), getVisitsInteractor(), visitId)
    }

    private fun getUserScope(): UserScope {
        if (userScope == null) {
            val context = MyApplication.context
            val historyRepository = HistoryRepository(
                getVisitsApiClient(MyApplication.context),
                crashReportsProvider,
                getOsUtilsProvider(MyApplication.context)
            )
            val scope = CoroutineScope(Dispatchers.IO)
            val placesRepository = getPlacesRepository()
            val hyperTrackService = getHyperTrackService(context)
            userScope = UserScope(
                historyRepository,
                placesRepository,
                UserScopeViewModelFactory(
                    getVisitsRepo(context),
                    placesRepository,
                    historyRepository,
                    getDriverRepo(context),
                    getAccountRepo(context),
                    crashReportsProvider,
                    hyperTrackService,
                    getPermissionInteractor(),
                    accessTokenRepository(MyApplication.context),
                    getTimeLengthFormatter(),
                    getVisitsApiClient(MyApplication.context),
                    getOsUtilsProvider(MyApplication.context)
                ),
                PhotoUploadInteractorImpl(
                    getVisitsRepo(context),
                    getFileRepository(),
                    crashReportsProvider,
                    getImageDecoder(),
                    getVisitsApiClient(MyApplication.context),
                    scope,
                    RetryParams(
                        retryTimes = 3,
                        initialDelay = 1000,
                        factor = 10.0,
                        maxDelay = 30 * 1000
                    )
                ),
                hyperTrackService
            )
        }
        return userScope!!
    }

    private fun getFileRepository(): FileRepository {
        return FileRepositoryImpl()
    }

    private fun getPlacesRepository(): PlacesRepository {
        return PlacesRepository(getVisitsApiClient(MyApplication.context))
    }

    private fun getPermissionInteractor(): PermissionsInteractor {
        return PermissionsInteractorImpl(
            getAccountRepo(MyApplication.context)
        )
    }

    private val tokenForPublishableKeyExchangeService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(LIVE_API_URL_BASE)
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .build()
        return@lazy retrofit.create(TokenForPublishableKeyExchangeService::class.java)
    }

    private val liveAccountUrlService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(LIVE_ACCOUNT_URL_BASE)
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .build()
        return@lazy retrofit.create(LiveAccountApi::class.java)
    }

    @ExperimentalCoroutinesApi
    private fun getLoginInteractor(): LoginInteractor {
        return LoginInteractorImpl(
            getCognitoLoginProvider(MyApplication.context),
            getAccountRepo(MyApplication.context),
            getDriverRepo(MyApplication.context),
            tokenForPublishableKeyExchangeService,
            liveAccountUrlService,
            MyApplication.SERVICES_API_KEY
        )
    }

    private fun getMyPreferences(context: Context): MyPreferences =
        MyPreferences(context, getMoshi())

    private fun getDriver(context: Context): Driver = getMyPreferences(context).getDriverValue()

    private fun getDriverRepo(context: Context) = DriverRepository(
        getDriver(context),
        getAccountRepo(MyApplication.context),
        getServiceLocator(),
        getMyPreferences(context),
        crashReportsProvider,
    )

    private fun getServiceLocator(): ServiceLocator {
        return ServiceLocator
    }

    private fun getVisitsApiClient(context: Context): ApiClient {
        val accessTokenRepository = accessTokenRepository(context)
        return ApiClient(
            accessTokenRepository,
            BASE_URL, accessTokenRepository.deviceId
        )
    }

    private fun getVisitsInteractor(): VisitsInteractor {
        return VisitsInteractorImpl(
            getVisitsRepo(MyApplication.context),
            getImageDecoder(),
            getUserScope().photoUploadInteractor
        )
    }

    private fun accessTokenRepository(context: Context) =
        (getMyPreferences(context).restoreRepository()
            ?: throw IllegalStateException("No access token repository was saved"))

    private fun getAccountRepo(context: Context) =
        AccountRepository(getServiceLocator(), getAccountData(context), getMyPreferences(context))
        { userScope = null }

    private fun getAccountData(context: Context): AccountData =
        getMyPreferences(context).getAccountData()

    private fun getOsUtilsProvider(context: Context): OsUtilsProvider {
        return OsUtilsProvider(context, crashReportsProvider)
    }

    private fun getHyperTrackService(context: Context): HyperTrackService {
        val myPreferences = getMyPreferences(context)
        val publishableKey = myPreferences.getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        return getServiceLocator().getHyperTrackService(publishableKey)
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
        )
        visitsRepository = result

        return result
    }

    private fun getImageDecoder(): ImageDecoder = SimpleImageDecoder()

    private fun getCognitoLoginProvider(context: Context): CognitoAccountLoginProvider =
        CognitoAccountLoginProviderImpl(context, LIVE_API_URL_BASE)

    private fun getHistoryMapRenderer(supportMapFragment: SupportMapFragment): HistoryMapRenderer =
        GoogleMapHistoryRenderer(supportMapFragment, BaseHistoryStyle(MyApplication.context), FusedDeviceLocationProvider(MyApplication.context))

    fun getHistoryRendererFactory(): Factory<SupportMapFragment, HistoryMapRenderer> =
        Factory { a -> getHistoryMapRenderer(a) }

    private fun getTimeLengthFormatter() = SimpleTimeDistanceFormatter()

}

private class UserScope(
    val historyRepository: HistoryRepository,
    val placesRepository: PlacesRepository,
    val userScopeViewModelFactory: UserScopeViewModelFactory,
    val photoUploadInteractor: PhotoUploadInteractor,
    val hyperTrackService: HyperTrackService
)

fun interface Factory<A, T> {
    fun create(a: A): T
}

interface AccountPreferencesProvider {
    var wasWhitelisted: Boolean
    val isManualCheckInAllowed: Boolean
    val isPickUpAllowed: Boolean
    var shouldStartTracking: Boolean
}

const val BASE_URL = "https://live-app-backend.htprod.hypertrack.com/"
const val LIVE_API_URL_BASE = "https://live-api.htprod.hypertrack.com/"
const val AUTH_URL = LIVE_API_URL_BASE + "authenticate"
const val MAX_IMAGE_SIDE_LENGTH_PX = 1024

const val LIVE_ACCOUNT_URL_BASE = "https://live-account.htprod.hypertrack.com"
