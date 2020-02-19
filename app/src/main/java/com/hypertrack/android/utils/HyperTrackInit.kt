package com.hypertrack.android.utils

import android.content.Context
import com.hypertrack.android.getDeviceName
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack

// Singletone class for initialize HyperTrack SDK
object HyperTrackInit {

    init {
        println("Hyper Track Custom class Invoked")
    }

    fun getAccess(context: Context): HyperTrack {

        return HyperTrack.getInstance(
            context,
            context.getString(R.string.hyperTrackPublishKey)
        )
            .setDeviceName(getDeviceName())

    }
}