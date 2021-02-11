package com.hypertrack.android.ui.screens.splash_screen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.JustLoading
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.android.utils.PermissionsUtils
import com.hypertrack.android.view_models.SplashScreenViewModel
import com.hypertrack.logistics.android.github.R

class SplashScreenFragment: ProgressDialogFragment(R.layout.splash_screen_layout) {

    private lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splashScreenViewModel = mainActivity().splashScreenViewModel

        splashScreenViewModel.state.observe(viewLifecycleOwner, { state ->
            if(state !is JustLoading) {
                dismissProgress()
            }
            when(state) {
                is JustLoading -> {
                    showProgress()
                }
                is SplashScreenViewModel.KeyIsCorrect -> {
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
                }
                is SplashScreenViewModel.LoggedIn -> {
                    if(PermissionsUtils.hasRequiredPermissions()) {
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToVisitManagementFragment())
                    } else {
                        findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToPermissionRequestFragment())
                    }
                }
                is SplashScreenViewModel.AccountVerified -> {
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToDriverIdInputFragment())
                }
                is SplashScreenViewModel.NoPublishableKey -> {
                    findNavController().navigate(SplashScreenFragmentDirections.actionSplashScreenFragmentToLoginFragment())
                }
            }
        })
    }
}