package com.hypertrack.android.ui.screens.visits_management.tabs.summary

import android.view.View
import androidx.annotation.DrawableRes
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_summary.view.*

class SummaryItemsAdapter : BaseAdapter<SummaryItem, BaseAdapter.BaseVh<SummaryItem>>() {

    override val itemLayoutResource: Int = R.layout.item_summary

    override fun createViewHolder(
            view: View,
            baseClickListener: (Int) -> Unit
    ): BaseVh<SummaryItem> {
        return object : BaseContainerVh<SummaryItem>(view, baseClickListener) {
            override fun bind(item: SummaryItem) {
                item.drawableRes.toView(containerView.ivItemIcon)
                item.name.toView(containerView.tvItemName)
                item.value1?.toView(containerView.tvItemValue1)
                item.value2.toView(containerView.tvItemValue2)
            }
        }
    }
}

class SummaryItem(
        @DrawableRes
        val drawableRes: Int,
        val name: String,
        val value2: String,
        val value1: String? = null,
)