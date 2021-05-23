package com.hypertrack.android.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.interactors.TripsInteractor
import com.hypertrack.android.repository.*
import com.hypertrack.android.ui.screens.add_integration.AddIntegrationViewModel
import com.hypertrack.android.ui.screens.add_place.AddPlaceViewModel
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.profile.ProfileViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.summary.SummaryViewModel
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.ui.screens.driver_id_input.DriverLoginViewModel
import com.hypertrack.android.ui.screens.permission_request.PermissionRequestViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.history.DeviceLocationProvider
import com.hypertrack.android.ui.screens.visits_management.tabs.orders.OrdersListViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.places.PlacesViewModel
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TimeDistanceFormatter
import com.hypertrack.android.view_models.HistoryViewModel

@Suppress("UNCHECKED_CAST")
class UserScopeViewModelFactory(
    private val visitsRepository: VisitsRepository,
    private val tripsInteractor: TripsInteractor,
    private val placesRepository: PlacesRepository,
    private val integrationsRepository: IntegrationsRepository,
    private val historyRepository: HistoryRepository,
    private val driverRepository: DriverRepository,
    private val accountRepository: AccountRepository,
    private val crashReportsProvider: CrashReportsProvider,
    private val hyperTrackService: HyperTrackService,
    private val permissionsInteractor: PermissionsInteractor,
    private val accessTokenRepository: AccessTokenRepository,
    private val timeLengthFormatter: TimeDistanceFormatter,
    private val apiClient: ApiClient,
    private val osUtilsProvider: OsUtilsProvider,
    private val placesClient: PlacesClient,
    private val deviceLocationProvider: DeviceLocationProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            OrdersListViewModel::class.java -> OrdersListViewModel(
                tripsInteractor,
                osUtilsProvider
            ) as T
            AddIntegrationViewModel::class.java -> AddIntegrationViewModel(
                integrationsRepository
            ) as T
            AddPlaceViewModel::class.java -> AddPlaceViewModel(
                osUtilsProvider,
                placesClient,
                deviceLocationProvider
            ) as T
            PlacesViewModel::class.java -> PlacesViewModel(
                placesRepository,
                osUtilsProvider
            ) as T
            PermissionRequestViewModel::class.java -> PermissionRequestViewModel(
                permissionsInteractor,
                hyperTrackService
            ) as T
            SummaryViewModel::class.java -> SummaryViewModel(
                historyRepository,
                osUtilsProvider,
                timeLengthFormatter
            ) as T
            HistoryViewModel::class.java -> HistoryViewModel(
                historyRepository,
                timeLengthFormatter,
                osUtilsProvider
            ) as T
            DriverLoginViewModel::class.java -> DriverLoginViewModel(
                driverRepository,
                hyperTrackService,
                permissionsInteractor
            ) as T
            VisitsManagementViewModel::class.java -> VisitsManagementViewModel(
                visitsRepository,
                historyRepository,
                accountRepository,
                crashReportsProvider,
                accessTokenRepository
            ) as T
            ProfileViewModel::class.java -> ProfileViewModel(
                driverRepository,
                hyperTrackService,
                accountRepository,
                osUtilsProvider
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}