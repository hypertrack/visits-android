package com.hypertrack.android.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

internal class HistoryCoordinateJsonAdapter {
    @ToJson
    fun historyCoordinateToJson(historyCoordinate: HistoryCoordinate): List<Any?> =
        listOf(
                historyCoordinate.longitude,
                historyCoordinate.latitude,
                historyCoordinate.altitude,
                historyCoordinate.timestamp
            )

    @FromJson
    fun jsonToHistoryCoordinate(json: List<Any?>): HistoryCoordinate {
        if (json.size != 4) throw JsonDataException("history coordinate should consist of four elements")
        if (json[0] !is Double || json[1] !is Double) throw JsonDataException("longitude and latitude should be of double type")
        if (json[3] !is String) throw JsonDataException("Timestamp should be String in ISO format")
        return HistoryCoordinate(json[0] as Double, json[1] as Double, json[2] as Double?, json[3] as String)
    }
}