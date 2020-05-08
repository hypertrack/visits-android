package com.hypertrack.android.repository

interface AccountDataStorage {

    fun getAccountData() : AccountData

    fun saveAccountData(accountData: AccountData) : Unit
}
