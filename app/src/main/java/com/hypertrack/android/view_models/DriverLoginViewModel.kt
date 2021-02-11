package com.hypertrack.android.view_models

import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.ui.base.BaseStateViewModel
import com.hypertrack.android.ui.base.JustLoading
import com.hypertrack.android.ui.base.JustSuccess
import com.hypertrack.android.utils.HyperTrackService
import kotlinx.coroutines.launch

class DriverLoginViewModel(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService
) : BaseStateViewModel() {

    fun onLoginClick(driverId: String?) {
        driverId?.let {
            state.postValue(JustLoading)
            // Log.d(TAG, "Proceeding with Driver Id $driverId")
            hyperTrackService.driverId = driverId
            driverRepo.driverId = driverId
            viewModelScope.launch {
                state.postValue(JustSuccess)
            }
        }
    }

    fun checkAutoLogin() {
        // Log.v(TAG, "checkAutoLogin")
        if (driverRepo.hasDriverId) {
            onLoginClick(driverRepo.driverId)
        }
    }

    companion object {
        const val TAG = "LoginVM"
    }
}