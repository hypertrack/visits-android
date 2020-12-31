package com.hypertrack.android.response

import com.google.gson.annotations.SerializedName

data class AccountData(
    @SerializedName("pub_key") val publishableKey : String? = null,
    @SerializedName("last_token") val lastToken : String? = null,
    @SerializedName("show_manual_visits") var isManualVisitEnabled: Boolean = false,
    @SerializedName("auto_check_in") var autoCheckIn: Boolean = true,
    @SerializedName("pick_up_allowed") private var _pickUpAllowed: Boolean? = true
) {
    var pickUpAllowed: Boolean
        get() = _pickUpAllowed ?: true
        set(value) { _pickUpAllowed = value }
}