package com.hypertrack.android.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Integration(
    val id: String,
    val name: String?,
    val type: String = "Hubspot company",
) {
}
