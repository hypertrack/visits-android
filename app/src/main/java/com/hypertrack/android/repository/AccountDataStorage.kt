package com.hypertrack.android.repository


interface AccountDataStorage {

    fun getAccountData() : AccountData

    fun saveAccountData(accountData: AccountData) : Unit

    fun getDriverValue(): Driver

    fun saveDriver(driverModel: Driver)
}
