package com.hypertrack.android.repository

import com.hypertrack.android.utils.MyPreferences

class DriverRepo(myPreferences: MyPreferences) {

    val driver : Driver = myPreferences.getDriver()
    // TODO Denys
}
data class Driver(val deviceId:String, val driverId : String) {
    val isLoggedIn: Boolean
    get() = driverId.isNotEmpty()
}
