package com.hypertrack.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hypertrack.android.repository.*
import com.hypertrack.android.response.DriverModel


class MyPreferences(context: Context, private val gson: Gson) : AccountDataStorage {

    private val getPreferences : SharedPreferences
            = context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    override fun saveDriver(driverModel: DriverModel) {
        val serializedModel = gson.toJson(driverModel)
        getPreferences.edit()?.putString(DRIVER_KEY, serializedModel)?.apply()
    }

    override fun getDriverValue(): DriverModel? {
        val driverDetails = getPreferences.getString(DRIVER_KEY, null)
        driverDetails?.let {
            return gson.fromJson(driverDetails,DriverModel::class.java)
        }
        return null

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

    fun restoreRepository() : AccessTokenRepository? {
        getPreferences.getString(ACCESS_REPO_KEY, null)?.let {
            try {
                return gson.fromJson(it, BasicAuthAccessTokenRepository::class.java)
            } catch (ignored: Throwable) {

            }
        }
        return null
    }

    fun persistRepository(repo: AccessTokenRepository) {
        getPreferences.edit()?.putString(ACCESS_REPO_KEY, gson.toJson(repo))?.apply()
    }

    fun getDriver(): Driver {
        TODO("Denys")
    }

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"
        val ACCESS_REPO_KEY = "com.hypertrack.android.utils.access_token_repo"
        val ACCOUNT_KEY = "com.hypertrack.android.utils.accountKey"

    }

}