package com.hypertrack.android.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.hypertrack.android.interactors.PhotoForUpload
import com.hypertrack.android.interactors.PhotoUploadQueueStorage
import com.hypertrack.android.interactors.PhotoUploadingState
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.local.LocalTrip
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MyPreferences(context: Context, private val moshi: Moshi) :
    AccountDataStorage, VisitsStorage, TripsStorage, PhotoUploadQueueStorage {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("hyper_track_pref", Context.MODE_PRIVATE)

    override fun saveDriver(driverModel: Driver) {
        val serializedModel = moshi.adapter(Driver::class.java).toJson(driverModel)
        sharedPreferences.edit()?.putString(DRIVER_KEY, serializedModel)?.apply()
    }

    override fun getDriverValue(): Driver {
        val driverDetails = sharedPreferences.getString(DRIVER_KEY, null)
        driverDetails?.let {
            return moshi.adapter(Driver::class.java).fromJson(driverDetails) ?: Driver("")
        }
        return Driver("")

    }

    fun clearPreferences() {
        sharedPreferences.edit()?.clear()?.apply()
    }

    override fun getAccountData(): AccountData {
        return try {
            moshi.adapter(AccountData::class.java)
                    .fromJson(sharedPreferences.getString(ACCOUNT_KEY, "{}")!!) ?: AccountData()
        } catch (ignored: Throwable) {
            AccountData()
        }
    }

    override fun saveAccountData(accountData: AccountData) {
        sharedPreferences.edit()
                ?.putString(ACCOUNT_KEY, moshi.adapter(AccountData::class.java).toJson(accountData))
                ?.apply()
    }

    override fun restoreRepository(): BasicAuthAccessTokenRepository? {
        sharedPreferences.getString(ACCESS_REPO_KEY, null)?.let {
            try {
                val config = moshi.adapter(BasicAuthAccessTokenConfig::class.java).fromJson(it)
                        ?: return null
                return BasicAuthAccessTokenRepository(config)
            } catch (ignored: Throwable) {

            }
        }
        return null
    }

    override fun persistRepository(repo: AccessTokenRepository) {
        sharedPreferences.edit()
                ?.putString(
                        ACCESS_REPO_KEY,
                        moshi
                                .adapter(BasicAuthAccessTokenConfig::class.java)
                                .toJson(repo.getConfig() as BasicAuthAccessTokenConfig)
                )?.apply()
    }

    override fun saveVisits(visits: List<Visit>) {
        sharedPreferences.edit().putString(VISITS_KEY, visitsListAdapter.toJson(visits))?.apply()
    }

    override fun restoreVisits(): List<Visit> {
        try {
            return visitsListAdapter
                .fromJson(sharedPreferences.getString(VISITS_KEY, "[]")!!) ?: emptyList()
        } catch (e: Throwable) {
            Log.w(TAG, "Can't deserialize visits ${e.message}")
        }
        return emptyList()
    }

    override suspend fun saveTrips(trips: List<LocalTrip>) {
        sharedPreferences.edit().putString(TRIPS_KEY, tripsListAdapter.toJson(trips))?.apply()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getTrips(): List<LocalTrip> {
        return withContext(Dispatchers.IO) {
            try {
                tripsListAdapter
                    .fromJson(sharedPreferences.getString(TRIPS_KEY, "[]")!!) ?: emptyList()
            } catch (e: Throwable) {
                Log.w(TAG, "Can't deserialize trips ${e.message}")
                emptyList()
            }
        }
    }

    private val visitsListAdapter by lazy {
        moshi.adapter<List<Visit>>(Types.newParameterizedType(List::class.java, Visit::class.java))
    }

    private val tripsListAdapter by lazy {
        moshi.adapter<List<LocalTrip>>(
            Types.newParameterizedType(
                List::class.java,
                LocalTrip::class.java
            )
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getPhotosQueue(): Set<PhotoForUpload> {
        return withContext(Dispatchers.IO) {
            try {
                (photoListAdapter
                    .fromJson(sharedPreferences.getString(PHOTOS_KEY, "[]")!!)
                    ?: emptyList()).toSet()
            } catch (e: Throwable) {
                Log.w(TAG, "Can't deserialize photos ${e.message}")
                emptySet()
            }
        }
    }

    override suspend fun getPhotoFromQueue(photoId: String): PhotoForUpload? {
        return getPhotosQueue().firstOrNull { it.photoId == photoId }
    }

    override suspend fun addToPhotosQueue(photo: PhotoForUpload) {
        sharedPreferences.edit()
            .putString(PHOTOS_KEY, photoListAdapter.toJson(getPhotosQueue().toMutableList().apply {
                add(photo)
            }))?.apply()
    }

    override suspend fun updatePhotoState(
        photoId: String,
        state: PhotoUploadingState
    ) {
        sharedPreferences.edit()
            .putString(PHOTOS_KEY, photoListAdapter.toJson(getPhotosQueue().toMutableList().map {
                if (it.photoId == photoId) {
                    if (state == PhotoUploadingState.UPLOADED) {
                        null
                    } else {
                        it.apply { it.state = state }
                    }
                } else {
                    it
                }
            }.filterNotNull()))?.apply()
    }

    private val photoListAdapter by lazy {
        moshi.adapter<List<PhotoForUpload>>(
            Types.newParameterizedType(
                List::class.java,
                PhotoForUpload::class.java
            )
        )
    }

    companion object {
        const val DRIVER_KEY = "com.hypertrack.android.utils.driver"
        const val ACCESS_REPO_KEY = "com.hypertrack.android.utils.access_token_repo"
        const val ACCOUNT_KEY = "com.hypertrack.android.utils.accountKey"
        const val VISITS_KEY = "com.hypertrack.android.utils.deliveries"
        const val TRIPS_KEY = "com.hypertrack.android.utils.trips"
        const val PHOTOS_KEY = "com.hypertrack.android.utils.photos"
        const val UPLOADING_PHOTOS_KEY = "com.hypertrack.android.utils.uploading_photos"
        const val TAG = "MyPrefs"
    }

}

interface AccountDataStorage {

    fun getAccountData(): AccountData

    fun saveAccountData(accountData: AccountData)

    fun getDriverValue(): Driver

    fun saveDriver(driverModel: Driver)
    fun persistRepository(repo: AccessTokenRepository)
    fun restoreRepository(): BasicAuthAccessTokenRepository?
}

interface VisitsStorage {
    fun saveVisits(visits: List<Visit>)
    fun restoreVisits(): List<Visit>
}

interface TripsStorage {
    suspend fun saveTrips(trips: List<LocalTrip>)
    suspend fun getTrips(): List<LocalTrip>
}