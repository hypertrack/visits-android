package com.hypertrack.android.ui.screens.visits_management.tabs.profile

import android.view.View
import androidx.annotation.StringRes
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.toTextView
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_profile.view.*
import kotlinx.android.synthetic.main.item_summary.view.tvItemName

class ProfileItemsAdapter : BaseAdapter<ProfileItem, BaseAdapter.BaseVh<ProfileItem>>() {

    override val itemLayoutResource: Int = R.layout.item_profile

    override fun createViewHolder(
            view: View,
            baseClickListener: (Int) -> Unit
    ): BaseVh<ProfileItem> {
        return object : BaseContainerVh<ProfileItem>(view, baseClickListener) {
            override fun bind(item: ProfileItem) {
                item.nameRes.toTextView(containerView.tvItemName)
                item.value.toView(containerView.tvItemValue)
            }
        }
    }
}

class ProfileItem(
        @StringRes
        val nameRes: Int,
        val value: String,
)