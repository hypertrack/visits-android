package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hypertrack.android.repository.*
import com.hypertrack.android.view_models.Delivery


class MyPreferences(context: Context, private val gson: Gson) :
    AccountDataStorage, DeliveriesStorage {

    private val getPreferences : SharedPreferences
            = context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    override fun saveDriver(driverModel: Driver) {
        val serializedModel = gson.toJson(driverModel)
        getPreferences.edit()?.putString(DRIVER_KEY, serializedModel)?.apply()
    }

    override fun getDriverValue(): Driver {
        val driverDetails = getPreferences.getString(DRIVER_KEY, null)
        driverDetails?.let {
            return gson.fromJson(driverDetails,Driver::class.java)
        }
        return Driver("")

    }

    fun clearPreferences() {
        getPreferences.edit()?.clear()?.apply()
    }

    override fun getAccountData() : AccountData {
        return try {
            gson.fromJson(getPreferences.getString(ACCOUNT_KEY, "{}"), AccountData::class.java)
        } catch (ignored: Throwable) {
            AccountData()
        }
    }

    override fun saveAccountData(accountData: AccountData) {
        getPreferences.edit()?.putString(ACCOUNT_KEY, gson.toJson(accountData))?.apply()
    }

    override fun restoreRepository() : BasicAuthAccessTokenRepository? {
        getPreferences.getString(ACCESS_REPO_KEY, null)?.let {
            try {
                val config = gson.fromJson(it, BasicAuthAccessTokenConfig::class.java)
                return BasicAuthAccessTokenRepository(config)
            } catch (ignored: Throwable) {

            }
        }
        return null
    }

    override fun persistRepository(repo: AccessTokenRepository) {
        getPreferences.edit()?.putString(ACCESS_REPO_KEY, gson.toJson(repo.getConfig()))?.apply()
    }

    override fun saveDeliveries(deliveries: List<Delivery>) {
        getPreferences.edit().putString(DELIVERIES_KEY, gson.toJson(deliveries))?.apply()
    }

    override fun restoreDeliveries(): List<Delivery> {
        val typeToken = object : TypeToken<List<Delivery>>() {}.type
        try {
            return gson.fromJson(getPreferences.getString(DELIVERIES_KEY, null), typeToken)
        } catch (e: Throwable) {
            Log.w(TAG, "Can't deserialize deliveries ${e.message}")
        }
        return emptyList()
    }

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"
        const val ACCESS_REPO_KEY = "com.hypertrack.android.utils.access_token_repo"
        const val ACCOUNT_KEY = "com.hypertrack.android.utils.accountKey"
        const val DELIVERIES_KEY = "com.hypertrack.android.utils.deliveries"
        const val TAG = "MyPrefs"
    }

}

interface AccountDataStorage {

    fun getAccountData() : AccountData

    fun saveAccountData(accountData: AccountData) : Unit

    fun getDriverValue(): Driver

    fun saveDriver(driverModel: Driver)
    fun persistRepository(repo: AccessTokenRepository)
    fun restoreRepository(): BasicAuthAccessTokenRepository?
}

interface DeliveriesStorage {
    fun saveDeliveries(deliveries: List<Delivery>)
    fun restoreDeliveries() : List<Delivery>
}