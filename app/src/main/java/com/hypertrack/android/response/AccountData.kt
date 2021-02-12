package com.hypertrack.android.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountData(
    @field:Json(name = "pub_key") val publishableKey : String? = null,
    @field:Json(name = "last_token") val lastToken : String? = null,
    @field:Json(name = "show_manual_visits") var isManualVisitEnabled: Boolean = false,
    @field:Json(name = "auto_check_in") var autoCheckIn: Boolean = true,
    @field:Json(name = "pick_up_allowed") var _pickUpAllowed: Boolean? = true,
    @field:Json(name = "was_whitelisted") var _wasWhitelisted: Boolean? = false
) {
    var wasWhitelisted: Boolean
        get() = _wasWhitelisted ?: false
        set(value) { _wasWhitelisted = value }
    var pickUpAllowed: Boolean
        get() = _pickUpAllowed ?: true
        set(value) { _pickUpAllowed = value }
}