package com.hypertrack.android.ui.screens.visits_management.tabs.places

import android.view.View
import android.view.ViewGroup
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_place.view.*
import kotlinx.android.synthetic.main.item_spinner.view.*

class PlacesAdapter : BaseAdapter<PlaceItem, BaseAdapter.BaseVh<PlaceItem>>() {

    override val itemLayoutResource: Int = R.layout.item_place

    override fun createViewHolder(view: View, baseClickListener: (Int) -> Unit): BaseVh<PlaceItem> {
        return object : BaseContainerVh<PlaceItem>(view, baseClickListener) {
            override fun bind(item: PlaceItem) {
//                item.geofence.address?.street? ?: ite.toView(containerView.tvVisited)
                ((item.geofence.metadata?.get("name")
                    ?: item.geofence.geofence_id) as String).toView(containerView.tvTitle)
                (item.geofence.address?.street
                    ?: "${item.geofence.geometry.latitude} ${item.geofence.geometry.longitude}").toView(
                    containerView.tvAddress
                )
            }
        }
    }
}

class PlaceItem(
    val geofence: Geofence
)