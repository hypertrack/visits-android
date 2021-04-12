package com.hypertrack.android.ui.screens.visits_management.tabs.livemap

import android.content.Context
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.os.Parcel
import android.os.Parcelable
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import java.io.IOException
import java.util.*

class PlaceModel : Parcelable {
    var placeId: String = ""
    @JvmField
    var primaryText: String? = ""
    @JvmField
    var secondaryText: String? = ""
    @JvmField
    var latLng: LatLng? = null
    @JvmField
    var address: String? = ""
    @JvmField
    var isRecent = false
    override fun equals(obj: Any?): Boolean {
        return if (super.equals(obj)) {
            true
        } else obj is PlaceModel && placeId == obj.placeId
    }

    override fun hashCode(): Int {
        var result = 17
        result = 31 * result + placeId.hashCode()
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(placeId)
        dest.writeString(primaryText)
        dest.writeString(secondaryText)
        dest.writeParcelable(latLng, flags)
        dest.writeString(address)
        dest.writeByte(if (isRecent) 1.toByte() else 0.toByte())
    }

    fun populateAddressFromGeocoder(context: Context?) {
        address = getAddressFromGeocoder(latLng, context)
    }

    constructor() {}
    protected constructor(`in`: Parcel) {
        placeId = `in`.readString()?:""
        primaryText = `in`.readString()
        secondaryText = `in`.readString()
        latLng = `in`.readParcelable(LatLng::class.java.classLoader)
        address = `in`.readString()
        isRecent = `in`.readByte().toInt() != 0
    }

    companion object {
        private val STYLE_NORMAL: CharacterStyle = StyleSpan(Typeface.NORMAL)
        fun from(autocompletePrediction: AutocompletePrediction): PlaceModel {
            val placeModel = PlaceModel()
            placeModel.placeId = autocompletePrediction.placeId
            placeModel.primaryText = autocompletePrediction.getPrimaryText(STYLE_NORMAL).toString()
            placeModel.secondaryText =
                autocompletePrediction.getSecondaryText(STYLE_NORMAL).toString()
            return placeModel
        }

        fun from(collection: Collection<AutocompletePrediction>): List<PlaceModel> {
            val placeModelList: MutableList<PlaceModel> = ArrayList()
            for (item in collection) {
                placeModelList.add(from(item))
            }
            return placeModelList
        }

        fun getAddressFromGeocoder(latLng: LatLng?, context: Context?): String {
            if (!Geocoder.isPresent()) return ""
            val results: List<Address>?
            results = try {
                Geocoder(context).getFromLocation(latLng!!.latitude, latLng.longitude, 1)
            } catch (ignored: IOException) {
                return ""
            }
            if (results == null || results.isEmpty()) return ""
            val thoroughfare = results[0].thoroughfare
            return thoroughfare ?: ""
        }

        @JvmField
        val CREATOR: Parcelable.Creator<PlaceModel> = object : Parcelable.Creator<PlaceModel> {
            override fun createFromParcel(source: Parcel): PlaceModel {
                return PlaceModel(source)
            }

            override fun newArray(size: Int): Array<PlaceModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}