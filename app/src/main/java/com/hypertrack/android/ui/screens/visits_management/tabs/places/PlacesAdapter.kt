package com.hypertrack.android.ui.screens.visits_management.tabs.places

import android.location.Address
import android.view.View
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.formatDateTime
import com.hypertrack.android.ui.common.toAddressString
import com.hypertrack.android.ui.common.toShortAddressString
import com.hypertrack.android.ui.common.toView
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_place.view.*
import kotlinx.android.synthetic.main.item_spinner.view.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class PlacesAdapter(val osUtilsProvider: OsUtilsProvider) :
    BaseAdapter<PlaceItem, BaseAdapter.BaseVh<PlaceItem>>() {

    override val itemLayoutResource: Int = R.layout.item_place

    override fun createViewHolder(
        view: View,
        baseClickListener: (Int) -> Unit
    ): BaseAdapter.BaseVh<PlaceItem> {
        return object : BaseContainerVh<PlaceItem>(view, baseClickListener) {
            override fun bind(item: PlaceItem) {
                (item.geofence.visitsCount).let {
                    if (it > 0) {
                        val timesString =
                            MyApplication.context.resources.getQuantityString(R.plurals.time, it)

                        "${
                            MyApplication.context.getString(
                                R.string.places_visited,
                                it.toString()
                            )
                        } $timesString"
                            .toView(containerView.tvVisited)
                    } else {
                        containerView.tvVisited.setText(R.string.places_not_visited)
                    }
                }

                var placeAddress: Address? = null

                val name = (item.geofence.name
                    ?: item.geofence.address?.street
                    ?: osUtilsProvider.getPlaceFromCoordinates(
                        item.geofence.latitude,
                        item.geofence.longitude
                    )?.let {
                        placeAddress = it
                        it.toShortAddressString()
                    }
                    ?: item.geofence.metadataAddress
                    ?: item.geofence.created_at.let {
                        ZonedDateTime.parse(it).formatDateTime()
                    })
                name.toView(containerView.tvTitle)

                val address =
                    item.geofence.metadataAddress
                        ?: item.geofence.address?.let {
                            "${it.city}, ${it.street}"
                        }
                        ?: (placeAddress ?: osUtilsProvider.getPlaceFromCoordinates(
                            item.geofence.geometry.latitude,
                            item.geofence.geometry.longitude,
                        ))?.let {
                            if (it.thoroughfare == null) {
                                it.toShortAddressString()
                            } else {
                                null
                            }
                        }
                        ?: "${item.geofence.geometry.latitude} ${item.geofence.geometry.longitude}"
                address.toView(containerView.tvAddress)
            }
        }
    }
}

class PlaceItem(
    val geofence: Geofence
)