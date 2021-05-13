package com.hypertrack.android.ui.screens.visit_details

import android.graphics.Bitmap
import android.view.View
import com.hypertrack.android.interactors.PhotoForUpload
import com.hypertrack.android.models.VisitPhoto
import com.hypertrack.android.models.VisitPhotoState
import com.hypertrack.android.ui.base.BaseAdapter
import com.hypertrack.android.ui.common.setGoneState
import com.hypertrack.android.ui.common.toView
import com.hypertrack.logistics.android.github.R
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotosAdapter: BaseAdapter<VisitPhotoItem, BaseAdapter.BaseVh<VisitPhotoItem>>() {

    override val itemLayoutResource: Int = R.layout.item_photo

    override fun createViewHolder(view: View, baseClickListener: (Int) -> Unit): BaseVh<VisitPhotoItem> {
        return object: BaseContainerVh<VisitPhotoItem>(view, baseClickListener) {
            override fun bind(item: VisitPhotoItem) {
                item.thumbnail.toView(containerView.ivPhoto)
                containerView.progressBar.setGoneState(item.visitPhoto.state != VisitPhotoState.NOT_UPLOADED)
                containerView.tvRetry.setGoneState(item.visitPhoto.state != VisitPhotoState.ERROR)
            }
        }
    }
}

class VisitPhotoItem(
        val thumbnail: Bitmap,
        val visitPhoto: VisitPhoto
)