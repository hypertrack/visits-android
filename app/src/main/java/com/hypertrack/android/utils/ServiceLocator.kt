package com.hypertrack.android.utils

import android.app.Application
import com.google.gson.Gson
import com.hypertrack.android.repository.AccountData
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.response.DriverModel

class ServiceLocator(private val application: Application) {

    fun getDriverRepo() = DriverRepo(getDriverModel())

    fun getAccountRepo() = AccountRepository(getAccountData())

    private fun getAccountData(): AccountData = getMyPreferences(application).getAccountData()

    private fun getDriverModel(): DriverModel? = getMyPreferences(application).getDriverValue()

    private fun getMyPreferences(application: Application): MyPreferences =
        MyPreferences(application.applicationContext, getGson())

    private fun getGson() = Gson()
}

fun Application.getServiceLocator() = ServiceLocator(this)