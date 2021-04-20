package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.hypertrack.logistics.android.github.R

class SharedHelper private constructor(context: Context) {

    private val gson = Gson()
    private val preferences: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    val isHomePlaceSet: Boolean
        get() = preferences.contains(HOME_PLACE_KEY)
    var homePlace: PlaceModel?
        get() {
            try {
                return gson.fromJson(
                    preferences.getString(HOME_PLACE_KEY, null),
                    object : TypeToken<PlaceModel?>() {}.type
                )
            } catch (ignored: JsonSyntaxException) {
            }
            return null
        }
        set(home) {
            val homeJson = gson.toJson(home)
            preferences
                .edit()
                .putString(HOME_PLACE_KEY, homeJson)
                .apply()
        }
    var recentPlaces: Collection<PlaceModel?>?
        get() {
            val recentJson = preferences.getString(RECENT, "[]")
            val listType = object : TypeToken<Set<PlaceModel?>?>() {}.type
            try {
                return gson.fromJson<Set<PlaceModel>>(recentJson, listType)
            } catch (ignored: JsonSyntaxException) {
            }
            return emptySet()
        }
        set(recentPlaces) {
            val recentJson = gson.toJson(recentPlaces ?: emptySet<PlaceModel>())
            preferences.edit().putString(RECENT, recentJson).apply()
        }
    val selectedTripId: String?
        get() = preferences.getString(SELECTED_TRIP_ID, null)

    fun setSelectedTripId(tripId: String) {
        preferences.edit().putString(SELECTED_TRIP_ID, tripId).apply()
    }

    fun clearSelectedTripId() {
        preferences.edit().remove(SELECTED_TRIP_ID).apply()
    }

    companion object {
        private const val HOME_PLACE_KEY = "home_place"
        private const val RECENT = "recent"
        private const val SELECTED_TRIP_ID = "selected_trip_id"
        private var instance: SharedHelper? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): SharedHelper {
            if (instance == null) {
                instance = SharedHelper(context)
            }
            return instance!!
        }
    }

}