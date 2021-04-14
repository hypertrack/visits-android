package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringDef
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.hypertrack.logistics.android.github.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

class SharedHelper private constructor(context: Context) {
    @StringDef(LOGIN_TYPE_COGNITO, LOGIN_TYPE_DEEPLINK)
    @Retention(RetentionPolicy.SOURCE)
    annotation class LoginType

    private val gson = Gson()
    private val preferences: SharedPreferences
    var accountEmail: String
        get() = preferences.getString(USER_EMAIL_KEY, "")!!
        set(email) {
            preferences.edit().putString(USER_EMAIL_KEY, email).apply()
        }
    var inviteLink: String
        get() = preferences.getString(INVITE_LINK_KEY, "")!!
        set(inviteLink) {
            preferences.edit().putString(INVITE_LINK_KEY, inviteLink).apply()
        }
    var hyperTrackPubKey: String
        get() = preferences.getString(PUB_KEY, "")!!
        set(hyperTrackPubKey) {
            preferences.edit()
                .putString(PUB_KEY, hyperTrackPubKey)
                .apply()
        }

    fun removeHyperTrackPubKey() {
        preferences.edit().remove(PUB_KEY).apply()
    }

    fun setUserNameAndPhone(name: String?, phone: String?) {
        preferences.edit()
            .putString(USER_NAME_KEY, name)
            .putString(USER_PHONE_KEY, phone)
            .apply()
    }

    val deviceMetadata: Map<String, Any?>
        get() {
            val map: MutableMap<String, Any?> = HashMap(2)
            map[USER_NAME_KEY] =
                preferences.getString(USER_NAME_KEY, "")
            map[USER_PHONE_KEY] = preferences.getString(USER_PHONE_KEY, "")
            return map
        }
    val userName: String?
        get() = preferences.getString(USER_NAME_KEY, null)
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
            var recentPlaces = recentPlaces
            if (recentPlaces == null) recentPlaces = emptySet()
            val recentJson = gson.toJson(recentPlaces)
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

    var createdTripId: String?
        get() = preferences.getString(CREATED_TRIP_ID, null)
        set(tripId) {
            preferences.edit().putString(CREATED_TRIP_ID, tripId).apply()
        }
    var shareUrl: String?
        get() = preferences.getString(CREATED_TRIP_SHARE_URL, null)
        set(shareUrl) {
            preferences.edit().putString(CREATED_TRIP_SHARE_URL, shareUrl).apply()
        }

    @get:LoginType
    var loginType: String?
        get() = preferences.getString(LOGIN_TYPE, LOGIN_TYPE_COGNITO)
        set(loginType) {
            preferences.edit().putString(LOGIN_TYPE, loginType).apply()
        }

    fun logout() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PUB_KEY = "pub_key"
        private const val USER_EMAIL_KEY = "user_email"
        const val USER_NAME_KEY = "user_name"
        private const val USER_PHONE_KEY = "user_phone"
        private const val HOME_PLACE_KEY = "home_place"
        private const val USER_HOME_ADDRESS_KEY = "user_home_address"
        private const val USER_HOME_LATLON_KEY = "user_home_latlon"
        private const val RECENT = "recent"
        private const val SELECTED_TRIP_ID = "selected_trip_id"
        private const val CREATED_TRIP_ID = "created_trip_id"
        private const val CREATED_TRIP_SHARE_URL = "created_trip_share_url"
        private const val LOGIN_TYPE = "login_type"
        const val LOGIN_TYPE_DEEPLINK = "LOGIN_TYPE_DEEPLINK"
        const val LOGIN_TYPE_COGNITO = "LOGIN_TYPE_COGNITO"
        private const val INVITE_LINK_KEY = "INVITE_LINK_KEY"
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

    init {
        preferences =
            context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)
    }
}