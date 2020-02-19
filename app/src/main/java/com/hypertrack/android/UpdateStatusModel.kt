package com.hypertrack.android

class UpdateStatusModel(status : String,id : String) {

    var deliveryStatus = ""
    var deliveryId = ""

    init {
        this.deliveryStatus = status
        this.deliveryId = id
    }
}