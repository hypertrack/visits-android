package com.hypertrack.android.ui.screens.visit_details

import android.graphics.Bitmap
import android.view.View
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotosAdapter: BaseAdapter<CachedPhoto, BaseAdapter.BaseVh<CachedPhoto>>() {

    override val itemLayoutResource: Int = R.layout.item_photo

    override fun createViewHolder(view: View, baseClickListener: (Int) -> Unit): BaseVh<CachedPhoto> {
        return object: BaseContainerVh<CachedPhoto>(view, baseClickListener) {
            override fun bind(item: CachedPhoto) {
                item.thumbnail.toView(containerView.ivPhoto)
            }
        }
    }
}

class CachedPhoto(
        val thumbnail: Bitmap
)