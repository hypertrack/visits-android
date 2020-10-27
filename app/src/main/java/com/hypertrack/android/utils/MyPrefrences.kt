package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hypertrack.android.response.AccountData
import com.hypertrack.android.repository.*
import com.hypertrack.android.models.Visit


class MyPreferences(context: Context, private val gson: Gson) :
    AccountDataStorage, VisitsStorage {

    private val sharedPreferences : SharedPreferences
            = context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    override fun saveDriver(driverModel: Driver) {
        val serializedModel = gson.toJson(driverModel)
        sharedPreferences.edit()?.putString(DRIVER_KEY, serializedModel)?.apply()
    }

    override fun getDriverValue(): Driver {
        val driverDetails = sharedPreferences.getString(DRIVER_KEY, null)
        driverDetails?.let {
            return gson.fromJson(driverDetails,Driver::class.java)
        }
        return Driver("")

    }

    fun clearPreferences() {
        sharedPreferences.edit()?.clear()?.apply()
    }

    override fun getAccountData() : AccountData {
        return try {
            gson.fromJson(sharedPreferences.getString(ACCOUNT_KEY, "{}"), AccountData::class.java)
        } catch (ignored: Throwable) {
            AccountData()
        }
    }

    override fun saveAccountData(accountData: AccountData) {
        sharedPreferences.edit()?.putString(ACCOUNT_KEY, gson.toJson(accountData))?.apply()
    }

    override fun restoreRepository() : BasicAuthAccessTokenRepository? {
        sharedPreferences.getString(ACCESS_REPO_KEY, null)?.let {
            try {
                val config = gson.fromJson(it, BasicAuthAccessTokenConfig::class.java)
                return BasicAuthAccessTokenRepository(config)
            } catch (ignored: Throwable) {

            }
        }
        return null
    }

    override fun persistRepository(repo: AccessTokenRepository) {
        sharedPreferences.edit()?.putString(ACCESS_REPO_KEY, gson.toJson(repo.getConfig()))?.apply()
    }

    override fun saveVisits(visits: List<Visit>) {
        sharedPreferences.edit().putString(VISITS_KEY, gson.toJson(visits))?.apply()
    }

    override fun restoreVisits(): List<Visit> {
        val typeToken = object : TypeToken<List<Visit>>() {}.type
        try {
            return gson.fromJson(sharedPreferences.getString(VISITS_KEY, "[]"), typeToken)
        } catch (e: Throwable) {
            Log.w(TAG, "Can't deserialize visits ${e.message}")
        }
        return emptyList()
    }

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"
        const val ACCESS_REPO_KEY = "com.hypertrack.android.utils.access_token_repo"
        const val ACCOUNT_KEY = "com.hypertrack.android.utils.accountKey"
        const val VISITS_KEY = "com.hypertrack.android.utils.deliveries"
        const val TAG = "MyPrefs"
    }

}

interface AccountDataStorage {

    fun getAccountData() : AccountData

    fun saveAccountData(accountData: AccountData)

    fun getDriverValue(): Driver

    fun saveDriver(driverModel: Driver)
    fun persistRepository(repo: AccessTokenRepository)
    fun restoreRepository(): BasicAuthAccessTokenRepository?
}

interface VisitsStorage {
    fun saveVisits(visits: List<Visit>)
    fun restoreVisits() : List<Visit>
}