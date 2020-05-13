package com.hypertrack.android.repository

import com.hypertrack.android.response.DriverModel

interface AccountDataStorage {

    fun getAccountData() : AccountData

    fun saveAccountData(accountData: AccountData) : Unit

    fun getDriverValue(): DriverModel?

    fun saveDriver(driverModel: DriverModel)
}
