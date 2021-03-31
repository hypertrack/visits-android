package com.hypertrack.android.ui.common

import android.view.View
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_key_value.view.*
import java.util.*

class KeyValueAdapter : BaseAdapter<KeyValueItem, BaseAdapter.BaseVh<KeyValueItem>>() {

    override val itemLayoutResource: Int = R.layout.item_key_value

    override fun createViewHolder(
        view: View,
        baseClickListener: (Int) -> Unit
    ): BaseVh<KeyValueItem> {
        return object : BaseContainerVh<KeyValueItem>(view, baseClickListener) {
            override fun bind(item: KeyValueItem) {
                item.key.toView(containerView.tvKey)
                item.value.toView(containerView.tvValue)
            }
        }
    }
}

class KeyValueItem(
    val _key: String,
    val value: String,
) {
    val key: String
        get() = _key.formatUnderscore()
}

fun String.formatUnderscore(): String {
    return split("_").map { it.capitalize(Locale.getDefault()) }.joinToString(" ")
}