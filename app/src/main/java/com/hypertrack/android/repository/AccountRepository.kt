package com.hypertrack.android.repository

import android.util.Log
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.utils.AccountDataStorage
import com.hypertrack.android.utils.AccountPreferencesProvider
import com.hypertrack.android.utils.ServiceLocator

class AccountRepository(
    private val serviceLocator: ServiceLocator,
    private val accountData: AccountData,
    private val accountDataStorage: AccountDataStorage
) : AccountPreferencesProvider {

    val isVerifiedAccount : Boolean
        get() = accountData.lastToken != null

    override var isManualCheckInAllowed: Boolean
        get() = accountData.isManualVisitEnabled
        set(value) {
            accountData.isManualVisitEnabled = value
        }
    override var isAutoCheckInEnabled: Boolean
        get() = accountData.autoCheckIn
    set(value)  {
        accountData.autoCheckIn = value
    }

    suspend fun onKeyReceived(key: String, checkInEnabled: String = "false", autoCheckIn: String = "true") : Boolean {

        val sdk = serviceLocator.getHyperTrackService(key)
        Log.d(TAG, "HyperTrack deviceId ${sdk.deviceId}")

        val accessTokenRepository = serviceLocator.getAccessTokenRepository(sdk.deviceId, key)
        val token = try {
            accessTokenRepository.refreshTokenAsync()
        } catch (ignored: Throwable) {
            ""
        }

        if (token.isEmpty()) return false
        if (checkInEnabled in listOf("true", "True")) {
            isManualCheckInAllowed = true
        }
        if (autoCheckIn in listOf("false", "False")) {
            isAutoCheckInEnabled = false
        }

        accountDataStorage.saveAccountData(
            AccountData(
                publishableKey = key,
                lastToken = token,
                isManualVisitEnabled = isManualCheckInAllowed,
                autoCheckIn = isAutoCheckInEnabled
            )
        )
        accountDataStorage.persistRepository(accessTokenRepository)
        return true
    }

    companion object {
        const val TAG = "AccountRepo"
    }
}
