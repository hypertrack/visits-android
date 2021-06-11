package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.utils.*
import com.hypertrack.logistics.android.github.BuildConfig
import com.hypertrack.logistics.android.github.R

class ProfileViewModel(
    driverRepository: DriverRepository,
    hyperTrackService: HyperTrackService,
    accountRepository: AccountRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val crashReportsProvider: CrashReportsProvider,
) : BaseViewModel() {

    val profile = MutableLiveData<List<KeyValueItem>>(mutableListOf<KeyValueItem>().apply {
        add(
            KeyValueItem(
                osUtilsProvider.stringFromResource(R.string.driver_id),
                driverRepository.driverId
            )
        )
        add(
            KeyValueItem(
                osUtilsProvider.stringFromResource(R.string.device_id),
                hyperTrackService.deviceId ?: ""
            )
        )
        if (BuildConfig.DEBUG) {
            add(
                KeyValueItem(
                    "Publishable key (debug)",
                    accountRepository.publishableKey
                )
            )
        }
        getBuildVersion()?.let {
            add(
                KeyValueItem(
                    osUtilsProvider.stringFromResource(R.string.app_version),
                    it
                )
            )
        }
    })

    init {
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                profile.postValue(profile.value!!.toMutableList().apply {
                    add(
                        KeyValueItem(
                            "Firebase token (debug)",
                            it
                        )
                    )
                })
            }.addOnFailureListener {
//                Log.v("hypertrack-verbose", "firebase token retrieval failed ${it}")
            }
        }
    }

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

    fun onReportAnIssueClick() {
        crashReportsProvider.logException(ManuallyTriggeredException)
        osUtilsProvider.makeToast(R.string.profile_report_sent)
    }

}