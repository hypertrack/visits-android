package com.hypertrack.android.ui.screens.splash_screen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import com.hypertrack.android.ui.base.ProgressDialogFragment
import com.hypertrack.logistics.android.github.R

class SplashScreenFragment : ProgressDialogFragment(R.layout.fragment_splash_screen) {

    private lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        splashScreenViewModel = mainActivity().splashScreenViewModel

        splashScreenViewModel.loadingState.observe(viewLifecycleOwner, {
            if(it) showProgress() else dismissProgress()
        })

    }
}