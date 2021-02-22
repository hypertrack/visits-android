package com.hypertrack.android.ui.screens.visit_details

import android.graphics.Bitmap
import android.view.View
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotosAdapter: BaseAdapter<VisitPhoto, BaseAdapter.BaseVh<VisitPhoto>>() {

    override val itemLayoutResource: Int = R.layout.item_photo

    override fun createViewHolder(view: View, baseClickListener: (Int) -> Unit): BaseVh<VisitPhoto> {
        return object: BaseContainerVh<VisitPhoto>(view, baseClickListener) {
            override fun bind(item: VisitPhoto) {
                item.thumbnail.toView(containerView.ivPhoto)
                containerView.progressBar.setGoneState(item.uploaded)
            }
        }
    }
}

class VisitPhoto(
        val thumbnail: Bitmap,
        val uploaded: Boolean
)