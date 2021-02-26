package com.hypertrack.android.ui.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.utils.AccountLoginProvider
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.view_models.AccountLoginViewModel
import com.hypertrack.android.view_models.PermissionRequestViewModel
import com.hypertrack.android.view_models.SplashScreenViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val accountRepository: AccountRepository,
    private val driverRepository: DriverRepository,
    private val crashReportsProvider: CrashReportsProvider,
    private val accountLoginProvider: AccountLoginProvider,
    private val permissionsInteractor: PermissionsInteractor,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            PermissionRequestViewModel::class.java -> PermissionRequestViewModel(
                permissionsInteractor,
            ) as T
            AccountLoginViewModel::class.java -> AccountLoginViewModel(
                accountLoginProvider,
                accountRepository
            ) as T
            SplashScreenViewModel::class.java -> SplashScreenViewModel(
                driverRepository,
                accountRepository,
                crashReportsProvider,
                permissionsInteractor
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}