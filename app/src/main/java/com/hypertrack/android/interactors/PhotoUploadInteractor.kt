package com.hypertrack.android.interactors

import android.util.Log
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.VisitPhoto
import com.hypertrack.android.models.VisitPhotoState
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.retryWithBackoff
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PhotoUploadInteractor {
    fun addToQueue(visitId: String, photo: VisitPhoto)
}

class PhotoUploadInteractorImpl(
        private val visitsRepository: VisitsRepository,
        private val fileRepository: FileRepository,
        private val crashReportsProvider: CrashReportsProvider,
        private val imageDecoder: ImageDecoder,
        private val apiClient: ApiClient,
        private val scope: CoroutineScope,
) : PhotoUploadInteractor {

    init {
        scope.launch {
            visitsRepository.visits.forEach { visit ->
                visit.photos.filter { it.state != VisitPhotoState.UPLOADED }.forEach {
                    uploadPhoto(visit._id, it)
                }
            }
        }
    }

    override fun addToQueue(visitId: String, photo: VisitPhoto) {
        scope.launch {
            uploadPhoto(visitId, photo)
        }
    }

    private suspend fun uploadPhoto(visitId: String, photo: VisitPhoto) {
        // Log.d(TAG, "Launched preview update task")
        try {
            //todo don't retry 4**
            retryWithBackoff(
                    times = 5, factor = 10.0,
                    block = { uploadImage(imageId = photo.imageId, imagePath = photo.filePath, visitId = visitId) }
            )
            visitsRepository.getVisit(visitId)?.let { visit ->
                visit.photos.firstOrNull { it.imageId == photo.imageId }?.state = VisitPhotoState.UPLOADED
                visitsRepository.updateItem(visit._id, visit)
            }
            fileRepository.deleteIfExists(photo.filePath)
        } catch (t: Throwable) {
            when (t) {
                is java.net.UnknownHostException, is java.net.ConnectException, is java.net.SocketTimeoutException ->
                    Log.i(VisitsRepository.TAG, "Failed to upload image", t)
                else -> crashReportsProvider.logException(t)
            }
        }
    }

    private suspend fun uploadImage(
            imageId: String,
            imagePath: String,
            visitId: String,
    ) {
        try {
            val uploadedImage = imageDecoder.readBitmap(imagePath, MAX_IMAGE_SIDE_LENGTH_PX)
            apiClient.uploadImage(imageId, uploadedImage)
            withContext(Dispatchers.Main) {
                visitsRepository.getVisit(visitId)?.let { visit ->
                    visit.photos.firstOrNull { it.imageId == imageId }
                        ?.let {
                            it.state = VisitPhotoState.UPLOADED
                        }
                    visitsRepository.updateItem(visitId, visit)
                }
            }
            // Log.v(TAG, "Updated visit pic in target $target")
        } catch (e: Exception) {
            throw e
        }
    }
}
