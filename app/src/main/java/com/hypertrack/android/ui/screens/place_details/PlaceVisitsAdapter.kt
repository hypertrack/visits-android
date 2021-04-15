package com.hypertrack.android.ui.screens.place_details

import android.view.View
import com.google.android.libraries.places.api.model.Place
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceMarker
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.*
import com.hypertrack.android.ui.screens.visits_management.tabs.places.PlacesViewModel
import com.hypertrack.android.utils.MyApplication
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_place_visit.view.*

class PlaceVisitsAdapter(
    private val onCopyClickListener: ((String) -> Unit)? = null
) : BaseAdapter<GeofenceMarker, BaseAdapter.BaseVh<GeofenceMarker>>() {

    override val itemLayoutResource: Int = R.layout.item_place_visit

    override fun createViewHolder(
        view: View,
        baseClickListener: (Int) -> Unit
    ): BaseVh<GeofenceMarker> {
        return object : BaseContainerVh<GeofenceMarker>(view, baseClickListener) {
            override fun bind(item: GeofenceMarker) {
                val enter = item.arrival?.recordedAt?.formatDateTime()
                val exit = item.exit?.recordedAt?.formatDateTime()
                    ?: MyApplication.context.getString(R.string.now)
                "$enter - $exit".toView(containerView.tvTitle)
                item.duration?.let { DateTimeUtils.secondsToLocalizedString(it) }
                    ?.toView(containerView.tvDescription)
                item.markerId.toView(containerView.tvVisitId)
                item.routeTo?.let {
                    if (it.distance == null) return@let null
                    if (it.duration == null) return@let null
                    MyApplication.context.getString(
                        R.string.place_route_ro,
                        DistanceUtils.metersToDistanceString(it.distance),
                        DateTimeUtils.secondsToLocalizedString(it.duration)
                    )
                }?.toView(containerView.tvRouteTo)
                listOf(containerView.ivRouteTo, containerView.tvRouteTo).forEach {
                    it.setGoneState(item.routeTo == null)
                }

                containerView.divider.setGoneState(adapterPosition == itemCount - 1)

                containerView.setOnClickListener {
                    onCopyClickListener?.invoke(item.markerId)
                }
            }
        }
    }
}