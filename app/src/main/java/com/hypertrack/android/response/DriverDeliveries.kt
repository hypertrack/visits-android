package com.hypertrack.android.response

data class DriverDeliveries(
    val _id: String,
    val driver_id: String,
    val name: String,
    val device_id: String,
    val token: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: String,
    val active_trip: String,
    val app_name: String,
    val platform: String,
   val deliveries : ArrayList<Delivery>
)

data class Delivery(val status : String, val _id : String,
                    val delivery_id : String = "", val driver_id : String = "",
                    val label : String = "", val customerNote : String = "",
                    val createdAt : String = "", val updatedAt : String = "",
                    val items : List<Items> = emptyList(), val address : Address = Address("", "", "", ""),
                    val deliveryNote : String = "", var deliveryPicture : String = "", var enteredAt :String = "",
                    val completedAt : String = "", val exitedAt : String = "",
                    val latitude : Double? = null, val longitude: Double? = null)
// showNavigationItem =  this object only for sho navigation icon in pending category deliveries

data class Items(val _id : String,val item_id :String,val item_label : String,val item_sku : String)

data class Address (val street : String,val postalCode : String,val city : String,val country : String)

