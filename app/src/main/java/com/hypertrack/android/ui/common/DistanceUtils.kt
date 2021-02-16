package com.hypertrack.android.ui.common

import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import java.util.*
import kotlin.math.round

object DistanceUtils {

    fun shouldUseImperial(): Boolean {
        return when(Locale.getDefault().country) {
            "US", "LR", "MM" -> true
            else -> false
        }
    }

    fun metersToDistanceString(meters: Int): String {
        if(shouldUseImperial()) {
            return MyApplication.context.getString(R.string.miles, round(meters / 1000 * 0.62137).toInt())
        } else {
            return MyApplication.context.getString(R.string.kms, round(meters / 1000.0).toInt())
        }
    }
}