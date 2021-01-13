package com.hypertrack.android.response

import com.squareup.moshi.Json

data class AccountData(
    @Json(name = "pub_key") val publishableKey : String? = null,
    @Json(name = "last_token") val lastToken : String? = null,
    @Json(name = "show_manual_visits") var isManualVisitEnabled: Boolean = false,
    @Json(name = "auto_check_in") var autoCheckIn: Boolean = true,
    @Json(name = "pick_up_allowed") private var _pickUpAllowed: Boolean? = true
) {
    var pickUpAllowed: Boolean
        get() = _pickUpAllowed ?: true
        set(value) { _pickUpAllowed = value }
}