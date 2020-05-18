package com.hypertrack.android.response

import com.google.gson.annotations.SerializedName

data class AccountData(
    @SerializedName("pub_key") val publishableKey : String? = null,
    @SerializedName("last_token") val lastToken : String? = null
)