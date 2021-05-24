package com.hypertrack.android.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
class Integration(
    val id: String,
    val name: String?,
    val type: String = "Company",
) : Parcelable {
}
