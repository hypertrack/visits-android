package com.hypertrack.android.utils

import android.content.Context
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.RetryParams
import com.hypertrack.android.api.*
import com.hypertrack.android.interactors.*
import com.hypertrack.android.models.AbstractBackendProvider
import com.hypertrack.android.repository.*
import com.hypertrack.android.ui.common.ParamViewModelFactory
import com.hypertrack.android.ui.common.Tab
import com.hypertrack.android.ui.common.UserScopeViewModelFactory
import com.hypertrack.android.ui.common.ViewModelFactory
import com.hypertrack.android.ui.screens.add_place_info.AddPlaceInfoViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.history.*
import com.hypertrack.android.utils.injection.CustomFragmentFactory
import com.hypertrack.android.view_models.VisitDetailsViewModel
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import com.hypertrack.sdk.ServiceNotificationConfig
import com.hypertrack.sdk.views.HyperTrackViews
import com.squareup.moshi.Moshi
import com.squareup.moshi.recipes.RuntimeJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Provider


class ServiceLocator(val crashReportsProvider: CrashReportsProvider) {

    fun getAccessTokenRepository(deviceId: String, userName: String) =
        BasicAuthAccessTokenRepository(AUTH_URL, deviceId, userName)

    fun getHyperTrackService(publishableKey: String): HyperTrackService {
        val listener = TrackingState(crashReportsProvider)
        val sdkInstance = HyperTrack
            .getInstance(publishableKey)
            .addTrackingListener(listener)
            .backgroundTrackingRequirement(false)
            .setTrackingNotificationConfig(
                ServiceNotificationConfig.Builder()
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .build()
            )

        return HyperTrackService(listener, sdkInstance, crashReportsProvider)
    }

}


object Injector {

    private var userScope: UserScope? = null

    private var visitsRepository: VisitsRepository? = null

    val crashReportsProvider: CrashReportsProvider by lazy { FirebaseCrashReportsProvider() }

    val deeplinkProcessor: DeeplinkProcessor = BranchIoDeepLinkProcessor(crashReportsProvider)

    private val serviceLocator = ServiceLocator(crashReportsProvider)

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

    //todo user scope
    fun <T> provideParamVmFactory(param: T): ParamViewModelFactory<T> {
        return ParamViewModelFactory(
            param,
            getUserScope().tripsInteractor,
            getUserScope().placesRepository,
            getOsUtilsProvider(MyApplication.context),
            placesClient,
            getAccountRepo(MyApplication.context),
            getUserScope().photoUploadQueueInteractor,
            getVisitsApiClient(MyApplication.context),
            getMoshi(),
            crashReportsProvider,
        )
    }

    fun provideAddPlaceInfoVmFactory(
        latLng: LatLng,
        address: String?,
        name: String?,
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddPlaceInfoViewModel(
                    latLng,
                    initialAddress = address,
                    _name = name,
                    getUserScope().placesRepository,
                    getUserScope().integrationsRepository,
                    getOsUtilsProvider(MyApplication.context),
                ) as T
            }
        }
    }

    fun provideUserScopeViewModelFactory(): UserScopeViewModelFactory {
        return getUserScope().userScopeViewModelFactory
    }

    fun provideVisitStatusViewModel(context: Context, visitId: String): VisitDetailsViewModel {
        return VisitDetailsViewModel(
            getVisitsRepo(context),
            getVisitsInteractor(),
            visitId,
            getOsUtilsProvider(MyApplication.context)
        )
    }

    private fun isTwmoEnabled(): Boolean {
        return MyApplication.TWMO_ENABLED
    }

    fun provideTabs(): List<Tab> = mutableListOf<Tab>().apply {
        addAll(
            listOf(
                Tab.MAP,
                Tab.HISTORY,
            )
        )
        add(
            if (isTwmoEnabled()) {
                Tab.ORDERS
            } else {
                Tab.VISITS
            }
        )
        addAll(
            listOf(
                Tab.PLACES,
                Tab.SUMMARY,
                Tab.PROFILE,
            )
        )
    }

    private fun getUserScope(): UserScope {
        if (userScope == null) {
            val accessTokenRepository = accessTokenRepository(MyApplication.context)
            val context = MyApplication.context
            val historyRepository = HistoryRepository(
                getVisitsApiClient(MyApplication.context),
                crashReportsProvider,
                getOsUtilsProvider(MyApplication.context)
            )
            val scope = CoroutineScope(Dispatchers.IO)
            val placesRepository = getPlacesRepository()
            val integrationsRepository = getIntegrationsRepository()
            val hyperTrackService = getHyperTrackService(context)
            val photoUploadInteractor = PhotoUploadInteractorImpl(
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
            )

            val photoUploadQueueInteractor = PhotoUploadQueueInteractorImpl(
                getMyPreferences(MyApplication.context),
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
            )

            val tripsInteractor = TripsInteractorImpl(
                getVisitsApiClient(MyApplication.context),
                getMyPreferences(MyApplication.context),
                getAccountRepo(MyApplication.context),
                getMoshi(),
                getHyperTrackService(MyApplication.context),
                GlobalScope,
                photoUploadQueueInteractor,
                getImageDecoder(),
                getOsUtilsProvider(MyApplication.context),
                Dispatchers.IO
            )

            val driverRepository = getDriverRepo(context)

            val accountRepository = getAccountRepo(context)

            userScope = UserScope(
                historyRepository,
                tripsInteractor,
                placesRepository,
                integrationsRepository,
                UserScopeViewModelFactory(
                    getVisitsRepo(context),
                    tripsInteractor,
                    placesRepository,
                    integrationsRepository,
                    historyRepository,
                    driverRepository,
                    accountRepository,
                    crashReportsProvider,
                    hyperTrackService,
                    getPermissionInteractor(),
                    accessTokenRepository,
                    getTimeDistanceFormatter(),
                    getVisitsApiClient(MyApplication.context),
                    getOsUtilsProvider(MyApplication.context),
                    placesClient,
                    getDeviceLocationProvider()
                ),
                photoUploadInteractor,
                hyperTrackService,
                photoUploadQueueInteractor
            )

            crashReportsProvider.setUserIdentifier(
                getMoshi().adapter(UserIdentifier::class.java).toJson(
                    UserIdentifier(
                        deviceId = accessTokenRepository.deviceId,
                        driverId = driverRepository.driverId,
                        pubKey = accountRepository.publishableKey,
                    )
                )
            )
        }
        return userScope!!
    }

    private val placesClient: PlacesClient by lazy {
        Places.createClient(MyApplication.context)
    }

    private fun getFileRepository(): FileRepository {
        return FileRepositoryImpl()
    }

    private fun getPlacesRepository(): PlacesRepository {
        return PlacesRepository(getVisitsApiClient(MyApplication.context))
    }

    private fun getIntegrationsRepository(): IntegrationsRepository {
        return IntegrationsRepositoryImpl(getVisitsApiClient(MyApplication.context))
    }

    private fun getPermissionInteractor() = PermissionsInteractorImpl { getUserScope().hyperTrackService }

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
        serviceLocator,
        getMyPreferences(context),
        crashReportsProvider,
    )

    private fun getVisitsApiClient(context: Context): ApiClient {
        val accessTokenRepository = accessTokenRepository(context)
        return ApiClient(
            accessTokenRepository,
            BASE_URL,
            accessTokenRepository.deviceId,
            getMoshi(),
            crashReportsProvider
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
        AccountRepository(serviceLocator, getAccountData(context), getMyPreferences(context))
        { userScope = null }

    private fun getAccountData(context: Context): AccountData =
        getMyPreferences(context).getAccountData()

    fun getOsUtilsProvider(context: Context): OsUtilsProvider {
        return OsUtilsProvider(context, crashReportsProvider)
    }

    private fun getHyperTrackService(context: Context): HyperTrackService {
        val myPreferences = getMyPreferences(context)
        val publishableKey = myPreferences.getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        return serviceLocator.getHyperTrackService(publishableKey)
    }

    fun getVisitsRepo(context: Context): VisitsRepository {
        visitsRepository?.let { return it }

        getMyPreferences(context).getAccountData().publishableKey
            ?: throw IllegalStateException("No publishableKey saved")
        val result = VisitsRepository(
            getOsUtilsProvider(context),
            getVisitsApiClient(context),
            getMyPreferences(context),
            getHyperTrackService(context),
            getAccountRepo(context),
            getDeviceLocationProvider()
        )
        visitsRepository = result

        return result
    }

    private fun getImageDecoder(): ImageDecoder = SimpleImageDecoder()

    private fun getCognitoLoginProvider(context: Context): CognitoAccountLoginProvider =
        CognitoAccountLoginProviderImpl(context)

    private fun getHistoryMapRenderer(supportMapFragment: SupportMapFragment): HistoryMapRenderer =
        GoogleMapHistoryRenderer(
            supportMapFragment,
            BaseHistoryStyle(MyApplication.context),
            getDeviceLocationProvider(),
            crashReportsProvider
        )

    private fun getDeviceLocationProvider(): DeviceLocationProvider {
        return FusedDeviceLocationProvider(MyApplication.context)
    }

    fun getHistoryRendererFactory(): Factory<SupportMapFragment, HistoryMapRenderer> =
        Factory { a -> getHistoryMapRenderer(a) }

    fun getBackendProvider(ctx: Context): Provider<AbstractBackendProvider> =
        Provider { getVisitsApiClient(ctx) }

    fun getRealTimeUpdatesService(ctx: Context): Provider<HyperTrackViews> =
        Provider { HyperTrackViews.getInstance(ctx, getAccountRepo(ctx).publishableKey) }

    val hyperTrackServiceProvider = Provider { getUserScope().hyperTrackService }

    fun getTimeDistanceFormatter() =
        LocalizedTimeDistanceFormatter(getOsUtilsProvider(MyApplication.context))

    fun getCustomFragmentFactory(applicationContext: Context): FragmentFactory {
        val publishableKeyProvider: Provider<String> =
            Provider<String> { getAccountRepo(applicationContext).publishableKey }
        val hyperTrackServiceProvider = Provider { getUserScope().hyperTrackService }
        val apiClientProvider: Provider<AbstractBackendProvider> =
            Provider { getVisitsApiClient(applicationContext) }

        return CustomFragmentFactory(
            MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.style_map),
            MapStyleOptions.loadRawResourceStyle(applicationContext, R.raw.style_map_silver),
            hyperTrackServiceProvider,
            { HyperTrackViews.getInstance(applicationContext, publishableKeyProvider.get()) },
            apiClientProvider
        )
    }

}

private class UserScope(
    val historyRepository: HistoryRepository,
    val tripsInteractor: TripsInteractor,
    val placesRepository: PlacesRepository,
    val integrationsRepository: IntegrationsRepository,
    val userScopeViewModelFactory: UserScopeViewModelFactory,
    val photoUploadInteractor: PhotoUploadInteractor,
    val hyperTrackService: HyperTrackService,
    val photoUploadQueueInteractor: PhotoUploadQueueInteractor
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
