package com.hypertrack.android.ui.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hypertrack.android.repository.*
import com.hypertrack.android.ui.screens.visits_management.tabs.summary.SummaryViewModel
import com.hypertrack.android.utils.AccountLoginProvider
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.view_models.*

class ViewModelFactory(
        private val context: Context,
        private val accessTokenRepository: AccessTokenRepository,
        private val visitsRepository: VisitsRepository,
        private val historyRepository: HistoryRepository,
        private val accountRepository: AccountRepository,
        private val driverRepository: DriverRepository,
        private val crashReportsProvider: CrashReportsProvider,
        private val hyperTrackService: HyperTrackService,
        private val accountLoginProvider: AccountLoginProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            VisitsManagementViewModel::class.java -> return VisitsManagementViewModel(
                    visitsRepository,
                    historyRepository,
                    accountRepository,
                    accessTokenRepository,
                    crashReportsProvider
            ) as T
            SummaryViewModel::class.java -> SummaryViewModel(historyRepository) as T
            PermissionRequestViewModel::class.java -> PermissionRequestViewModel(
                    accountRepository,
                    context
            ) as T
            DriverLoginViewModel::class.java -> return DriverLoginViewModel(
                    driverRepository,
                    hyperTrackService
            ) as T
            AccountLoginViewModel::class.java -> return AccountLoginViewModel(
                    accountLoginProvider,
                    accountRepository
            ) as T
            SplashScreenViewModel::class.java -> return SplashScreenViewModel(
                    driverRepository,
                    accountRepository,
                    crashReportsProvider
            ) as T
            HistoryViewModel::class.java -> HistoryViewModel(historyRepository) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}