package com.hypertrack.android.interactors

import android.content.res.Resources
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.utils.ImageDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.*

interface VisitsInteractor {
    suspend fun addPhotoToVisit(visitId: String, imagePath: String)
}

class VisitsInteractorImpl(
        private val visitsRepository: VisitsRepository,
        private val imageDecoder: ImageDecoder,
        private val photoUploadInteractor: PhotoUploadInteractor
): VisitsInteractor {

    override suspend fun addPhotoToVisit(visitId: String, imagePath: String) = coroutineScope {
        // Log.d(TAG, "Update image for visit $id")
        val generatedImageId = UUID.randomUUID().toString()

        val target = visitsRepository.getVisit(visitId) ?: return@coroutineScope
        val previewMaxSideLength: Int = (200 * Resources.getSystem().displayMetrics.density).toInt()
        withContext(Dispatchers.Default) {
            target.addLocalPhoto(generatedImageId, imageDecoder.readBitmap(imagePath, previewMaxSideLength))
            // Log.v(TAG, "Updated icon in target $target")
        }
        visitsRepository.updateItem(visitId, target)

        photoUploadInteractor.addToQueue(visitId, generatedImageId, imagePath)
    }

}