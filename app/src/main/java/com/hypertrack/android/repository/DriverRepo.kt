package com.hypertrack.android.repository

import com.hypertrack.android.response.DriverModel

class DriverRepo(private val driverModel: DriverModel?) {

    // TODO Denys
    val hasDriverId: Boolean
    get() {
        if (driverModel == null) return false
        return driverModel.driver_id.isNotEmpty()
    }
}
data class Driver(val deviceId:String, val driverId : String) {
    val isLoggedIn: Boolean
    get() = driverId.isNotEmpty()
}
