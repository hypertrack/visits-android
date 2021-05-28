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
        return HistoryCoordinate(
            latitude = json[1] as Double,
            longitude = json[0] as Double,
            altitude = json[2] as Double?,
            timestamp = json[3] as String
        )
    }
}

internal class GeometryJsonAdapter {
    @ToJson
    fun geometryToJson(geometry: Geometry): Map<String, Any> =
            mapOf("type" to geometry.type, "coordinates" to geometry.coordinates)

    @Suppress("UNCHECKED_CAST")
    @FromJson
    fun jsonToGeometry(json: Map<String, Any>): Geometry {
        return when (json["type"]) {
            "Point" -> Point(json["coordinates"] as List<Double>)
            "Polygon" -> Polygon(json["coordinates"] as List<List<List<Double>>>)
            else -> throw JsonDataException("Unknown geometry type ${json["type"]}")
        }
    }
}