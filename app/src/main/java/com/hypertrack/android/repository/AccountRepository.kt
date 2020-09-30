package com.hypertrack.android.repository

import android.util.Log
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.utils.AccountDataStorage
import com.hypertrack.android.utils.ServiceLocator

class AccountRepository(
    private val serviceLocator: ServiceLocator,
    private val accountData: AccountData,
    private val accountDataStorage: AccountDataStorage
) {

    val isVerifiedAccount : Boolean
        get() = accountData.lastToken != null

    suspend fun onKeyReceived(key: String) : Boolean {

        val sdk = serviceLocator.getHyperTrackService(key)
        Log.d(TAG, "HyperTrack deviceId ${sdk.deviceId}")

        val accessTokenRepository = serviceLocator.getAccessTokenRepository(sdk.deviceId, key)
        val token = try {
            accessTokenRepository.refreshTokenAsync()
        } catch (ignored: Throwable) {
            ""
        }

        if (token.isEmpty()) return false

        accountDataStorage.saveAccountData(
            AccountData(
                key,
                token
            )
        )
        accountDataStorage.persistRepository(accessTokenRepository)
        return true
    }

    companion object {
        const val TAG = "AccountRepo"
    }
}
