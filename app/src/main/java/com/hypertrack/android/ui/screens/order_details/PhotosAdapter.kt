package com.hypertrack.android.ui.screens.order_details

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import com.hypertrack.android.interactors.PhotoForUpload
import com.hypertrack.android.interactors.PhotoUploadingState
import com.hypertrack.android.models.VisitPhoto
import com.hypertrack.android.models.VisitPhotoState
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.common.toView
import com.hypertrack.android.ui.screens.visit_details.VisitPhotoItem
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotosAdapter : BaseAdapter<PhotoItem, BaseAdapter.BaseVh<PhotoItem>>() {

    override val itemLayoutResource: Int = R.layout.item_photo

    override fun createViewHolder(view: View, baseClickListener: (Int) -> Unit): BaseVh<PhotoItem> {
        return object : BaseContainerVh<PhotoItem>(view, baseClickListener) {
            override fun bind(item: PhotoItem) {
                item.thumbnail?.toView(containerView.ivPhoto)
                containerView.progressBar.setGoneState(item.state != PhotoUploadingState.NOT_UPLOADED)
                containerView.tvRetry.setGoneState(item.state != PhotoUploadingState.ERROR)
            }
        }
    }
}

class PhotoItem(
    val photoId: String,
    val thumbnail: Bitmap?,
    val state: PhotoUploadingState
) {
    override fun toString(): String {
        return " $photoId $state"
    }
}