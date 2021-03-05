package com.hypertrack.android.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hypertrack.android.interactors.LoginInteractor
import com.hypertrack.android.interactors.PermissionsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.screens.background_permissions.BackgroundPermissionsViewModel
import com.hypertrack.android.utils.AccountLoginProvider
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.view_models.AccountLoginViewModel
import com.hypertrack.android.ui.screens.permission_request.PermissionRequestViewModel
import com.hypertrack.android.ui.screens.sign_in.SignInViewModel
import com.hypertrack.android.ui.screens.splash_screen.SplashScreenViewModel
import com.hypertrack.android.utils.HyperTrackService

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val accountRepository: AccountRepository,
    private val driverRepository: DriverRepository,
    private val crashReportsProvider: CrashReportsProvider,
    private val accountLoginProvider: AccountLoginProvider,
    private val permissionsInteractor: PermissionsInteractor,
    private val loginInteractor: LoginInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            SignInViewModel::class.java -> SignInViewModel(loginInteractor) as T
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
            BackgroundPermissionsViewModel::class.java -> BackgroundPermissionsViewModel(
                permissionsInteractor
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}