package com.hypertrack.android.repository

import com.google.gson.annotations.SerializedName

class AccountRepository(private val accountDataStorage: AccountDataStorage) {

    private val accountData = accountDataStorage.getAccountData()


    val isVerifiedAccount : Boolean
      get() = accountData.lastToken != null

    val hasNoKey : Boolean
      get() = accountData.publishableKey == null
}


data class AccountData(
    @SerializedName("pub_key") val publishableKey : String? = null,
    @SerializedName("last_token") val lastToken : String? = null
)