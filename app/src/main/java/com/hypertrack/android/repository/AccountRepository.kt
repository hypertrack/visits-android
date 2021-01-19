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

    override var wasWhitelisted: Boolean
        get() = accountData.wasWhitelisted
        set(value) {
            accountData.wasWhitelisted = value
            accountDataStorage.saveAccountData(accountData)
        }
    override var isManualCheckInAllowed: Boolean
        get() = accountData.isManualVisitEnabled
        set(value) { accountData.isManualVisitEnabled = value }
    override var isAutoCheckInEnabled: Boolean
        get() = accountData.autoCheckIn
        set(value)  { accountData.autoCheckIn = value }
    override var isPickUpAllowed: Boolean
        get() = accountData.pickUpAllowed
        set(value) { accountData.pickUpAllowed = value }

    suspend fun onKeyReceived(
        key: String,
        checkInEnabled: String = "false",
        autoCheckIn: String = "true",
        pickUpAllowed: String = "true"
    ) : Boolean {

        val sdk = serviceLocator.getHyperTrackService(key)
        // Log.d(TAG, "HyperTrack deviceId ${sdk.deviceId}")

        val accessTokenRepository = serviceLocator.getAccessTokenRepository(sdk.deviceId, key)
        val accountState = try {
            accessTokenRepository.refreshTokenAsync()
        } catch (ignored: Throwable) {
            Unknown
        }

        var token: String? = null

        when (accountState) {
            Unknown, InvalidCredentials -> return false
            is Active -> token = accountState.token
            Suspended -> {} // Log.d(TAG, "Account is suspended or device was deleted")
        }
        if (checkInEnabled in listOf("true", "True")) {
            isManualCheckInAllowed = true
        }
        if (autoCheckIn in listOf("false", "False")) {
            isAutoCheckInEnabled = false
        }
        if (pickUpAllowed in listOf("false", "False")) {
            isPickUpAllowed = false
        }

        accountDataStorage.saveAccountData(
            AccountData(
                publishableKey = key,
                lastToken = token,
                isManualVisitEnabled = isManualCheckInAllowed,
                autoCheckIn = isAutoCheckInEnabled,
                _pickUpAllowed = isPickUpAllowed
            )
        )
        accountDataStorage.persistRepository(accessTokenRepository)
        return true
    }

    companion object {
        const val TAG = "AccountRepo"
    }
}
