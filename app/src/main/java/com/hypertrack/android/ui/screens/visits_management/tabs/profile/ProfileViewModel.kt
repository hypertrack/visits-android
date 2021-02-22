package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R

class ProfileViewModel(
        driverRepository: DriverRepository,
        hyperTrackService: HyperTrackService,
) : ViewModel() {

    val profile = MutableLiveData<List<ProfileItem>>(mutableListOf<ProfileItem>().apply {
        add(ProfileItem(
                R.string.driver_id,
                driverRepository.driverId
        ))
        add(ProfileItem(
                R.string.device_id,
                hyperTrackService.deviceId ?: ""
        ))
        getBuildVersion()?.let {
            add(ProfileItem(
                    R.string.app_version,
                    it
            ))
        }
    })

    private fun getBuildVersion(): String? {
        try {
            val pInfo = MyApplication.context.packageManager.getPackageInfo(MyApplication.context.packageName, 0)
            return pInfo.versionName
        } catch (e: Exception) {
            return null
        }
    }

}