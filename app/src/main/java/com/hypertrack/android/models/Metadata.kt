package com.hypertrack.android.models

import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.repository.AuthCallResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi

@JsonClass(generateAdapter = true)
data class Metadata(
    val otherMetadata: Map<String, String>,
    var visitsAppMetadata: VisitsAppMetadata
) {

    fun toMap(): Map<String, Any> {
        return mutableMapOf<String, Any>().apply {
            otherMetadata.forEach { k, v -> put(k, v) }
            put(VISITS_APP_KEY, visitsAppMetadata)
        }
    }

    override fun toString(): String {
        return "$otherMetadata \n $visitsAppMetadata"
    }

    companion object {
        const val VISITS_APP_KEY = "visits_app"
        const val HT_KEY = "ht_"

        fun empty() = Metadata(mapOf(), VisitsAppMetadata())

        @Suppress("UNCHECKED_CAST")
        fun deserealize(map: Map<String, Any>): Metadata {
            return Metadata(
                visitsAppMetadata = (map.get(VISITS_APP_KEY) as VisitsAppMetadata?)
                    ?: VisitsAppMetadata(),
                otherMetadata = map
                    .filterKeys {
                        it != VISITS_APP_KEY && !it.startsWith(HT_KEY)
                    }
                    .filter { it.value is String } as Map<String, String>
            )
        }
    }

}

@JsonClass(generateAdapter = true)
data class VisitsAppMetadata(
    val appended: Map<String, AppendMetadata>?,
    var note: String?,
    @Json(name = "photos") var _photos: String?,
    @Json(name = "user_location") val userLocation: Location?,
) {

    constructor(
        appended: Map<String, AppendMetadata>? = null,
        note: String? = null,
        photos: Set<String>? = null,
        userLocation: Location? = null
    ) : this(
        appended,
        note,
        photos?.joinToString(","),
        userLocation
    )

    val photos: List<String>
        get() = (_photos?.split(",") ?: listOf())

    fun addPhoto(photoId: String) {
        _photos = photos.toMutableList().apply {
            add(photoId)
        }.toSet().joinToString(",")
    }

    override fun toString(): String {
        return "$appended \n $note \n $_photos \n $userLocation"
    }
}

@JsonClass(generateAdapter = true)
class AppendMetadata(
    val note: String?,
    @Json(name = "photos") val _photos: String?,
    @Json(name = "user_location") val userLocation: Location?
) {
    constructor(
        note: String? = null,
        photos: Set<String>? = null,
        userLocation: Location? = null
    ) : this(
        note,
        photos?.joinToString(","),
        userLocation
    )

    override fun toString(): String {
        return "$note \n $_photos \n $userLocation"
    }
}