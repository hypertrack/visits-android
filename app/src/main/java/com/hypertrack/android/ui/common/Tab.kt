package com.hypertrack.android.ui.common

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.hypertrack.logistics.android.github.R
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Tab(@DrawableRes val iconRes: Int) : Parcelable {
    MAP(R.drawable.ic_map_tab),
    HISTORY(R.drawable.ic_history),
    ORDERS(R.drawable.ic_visits_list_tab),
    VISITS(R.drawable.ic_visits_list_tab),
    PLACES(R.drawable.ic_places),
    SUMMARY(R.drawable.ic_insights_tab),
    PROFILE(R.drawable.ic_profile_tab),
}