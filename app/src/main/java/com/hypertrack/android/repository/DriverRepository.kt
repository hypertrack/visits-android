package com.hypertrack.android.repository

import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.FirebaseCrashReportsProvider
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.ServiceLocator
import com.squareup.moshi.JsonClass

class DriverRepository(
    private var _driver: Driver,
    private val accountRepository: AccountRepository,
    private val serviceLocator: ServiceLocator,
    private val accountDataStorage: AccountDataStorage,
    private val crashReportsProvider: CrashReportsProvider,
) {

    var driverId: String
        get() = _driver.driverId
        set(value) {
            _driver = Driver(value)
            serviceLocator.getHyperTrackService(accountRepository.publishableKey).let {
                it.driverId = value
            }
            accountDataStorage.saveDriver(_driver)
        }

    val hasDriverId: Boolean
        get() = _driver.driverId.isNotEmpty()

    companion object {
        const val TAG = "DriverRepo"
    }
}

@JsonClass(generateAdapter = true)
data class Driver(val driverId: String)
