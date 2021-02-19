package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.logistics.android.github.R

class ProfileViewModel(
        val deviceId: String,
        val driverRepository: DriverRepository,
): ViewModel() {

    val profile = MutableLiveData<List<ProfileItem>>(listOf(
            ProfileItem(
                    R.string.driver_id,
                    driverRepository.driverId
            ),
            ProfileItem(
                    R.string.device_id,
                    deviceId ?: ""
            )
    ))

}