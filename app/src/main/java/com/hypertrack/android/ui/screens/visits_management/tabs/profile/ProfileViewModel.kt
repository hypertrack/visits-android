package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.ui.common.stringFromResource
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.R

class ProfileViewModel(
    driverRepository: DriverRepository,
    hyperTrackService: HyperTrackService,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val profile = MutableLiveData<List<KeyValueItem>>(mutableListOf<KeyValueItem>().apply {
        add(
            KeyValueItem(
                R.string.driver_id.stringFromResource(),
                driverRepository.driverId
            )
        )
        add(
            KeyValueItem(
                R.string.device_id.stringFromResource(),
                hyperTrackService.deviceId ?: ""
            )
        )
        getBuildVersion()?.let {
            add(
                KeyValueItem(
                    R.string.app_version.stringFromResource(),
                    it
                )
            )
        }
    })

    private fun getBuildVersion(): String? {
        try {
            val pInfo = MyApplication.context.packageManager.getPackageInfo(
                MyApplication.context.packageName,
                0
            )
            return pInfo.versionName
        } catch (e: Exception) {
            return null
        }
    }

    fun onCopyItemClick(txt: String) {
        osUtilsProvider.copyToClipboard(txt)
    }

}