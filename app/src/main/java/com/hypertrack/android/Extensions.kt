package com.hypertrack.android

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Build
import android.text.TextUtils
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.hypertrack.android.response.Address
import com.hypertrack.logistics.android.github.R
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private lateinit var dialog: Dialog

const val LOCATION_REQUEST_CODE = 2

const val CAMERA_PERMISSION_REQUEST_CODE = 3

const val DELIVERY_UPDATE_RESULT_CODE = 4

const val TYPE_HEADER: Int = 0

const val TYPE_ITEM: Int = 2


const val KEY_EXTRA_DRIVER_ID = "driver_id"

const val KEY_EXTRA_DELIVERY_ID = "delivery_id"

const val BASE_URL = "https://backend-hypertrack-logistics.herokuapp.com/"

// Show Toast using this extension function
fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

// Show Progress Bar on anywhere
fun Context.showProgress() {

    dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_progress_bar)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    dialog.show()

}


// Dismiss Progress bar
fun dismissBar() {
    if (dialog != null) {
        dialog.dismiss()
    }
}

// check location permission
fun Activity.askLocationPermission(): Boolean {

    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PERMISSION_GRANTED
    ) {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )

        return false
    }

    return true
}

// check Camera permission
fun Activity.askCameraPermission(): Boolean {

    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) != PERMISSION_GRANTED
    ) {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )

        return false
    }

    return true
}

// Create address from Delivery Object
fun createAddress(address: Address): String {

    return address.street.plus("\n").plus(address.city).plus(",").plus(address.country)
        .plus("-${address.postalCode}")

}

// Encode Image and convert into base 64 String
fun encodeImage(imageFile: File): ByteArray? {
    var fis: FileInputStream? = null
    try {
        fis = FileInputStream(imageFile)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }

    val bm = BitmapFactory.decodeStream(fis)
    val baos = ByteArrayOutputStream()
    if (bm == null)
        return null
    bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)   // Compress image quality
    val b = baos.toByteArray()
    //Base64.de
    return b

}


fun convertSeverDateToTime(rawDate: String): String {

    var date: Date? = null
    var output = ""
    //2019-08-29T09:42:40.653
    //2019-08-12T05:40:00-04:00
    //24 July 2019 - 1:00 pm
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    df.timeZone = TimeZone.getTimeZone("UTC")
    val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    try {
        //Conversion of input String to date
        date = df.parse(rawDate)
        output = sdf.format(date)

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return output
}

fun getDeviceName(): String? {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else capitalize(manufacturer).toString() + " " + model
}

fun capitalize(str: String): String? {
    if (TextUtils.isEmpty(str)) {
        return str
    }
    val arr = str.toCharArray()
    var capitalizeNext = true
    val phrase = StringBuilder()
    for (c in arr) {
        if (capitalizeNext && Character.isLetter(c)) {
            phrase.append(Character.toUpperCase(c))
            capitalizeNext = false
            continue
        } else if (Character.isWhitespace(c)) {
            capitalizeNext = true
        }
        phrase.append(c)
    }
    return phrase.toString()
}

fun Context.getLocationFromAddress(strAddress: String): LatLng? {

    val coder = Geocoder(this)
    var address = listOf<android.location.Address>()
    var p1: LatLng = LatLng(0.0, 0.0)

    try {
        // May throw an IOException
        address = coder.getFromLocationName(strAddress, 5);
        if (address == null) {
            return null
        }

        val location: android.location.Address = address[0]
        p1 = LatLng(location.latitude, location.longitude)

    } catch (ex: IOException) {

        ex.printStackTrace()
    }

    return p1
}

// Show Error message when no driver list fetch
fun Context.showAlertMessage(message: String, finishActiivty: Boolean = true) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton("OK") { dialog, which ->

            dialog.dismiss()
            if (finishActiivty)
                (this as Activity).finish()

        }
        .create()
        .show()
}


fun getCurrentTime(): String {

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")

    return dateFormatter.format(Date())
}