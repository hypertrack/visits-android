package com.hypertrack.android.ui.screens.visits_management.tabs.orders

import android.view.View
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_key_value.view.*
import kotlinx.android.synthetic.main.item_order.view.*

class OrdersAdapter : BaseAdapter<LocalOrder, BaseAdapter.BaseVh<LocalOrder>>() {

    override val itemLayoutResource: Int = R.layout.item_order

    override fun createViewHolder(
        view: View,
        baseClickListener: (Int) -> Unit
    ): BaseVh<LocalOrder> {
        return object : BaseContainerVh<LocalOrder>(view, baseClickListener) {
            override fun bind(item: LocalOrder) {
                item.shortAddress.toView(containerView.tvAddress)
                item.etaString.toView(containerView.tvEta)
                item.status.name.toView(containerView.tvStatus)
            }
        }
    }
}
