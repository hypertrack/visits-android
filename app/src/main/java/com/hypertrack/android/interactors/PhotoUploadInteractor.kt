package com.hypertrack.android.interactors

import android.util.Log
import com.hypertrack.android.RetryParams
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

interface PhotoUploadInteractor {
    fun addToQueue(visitId: String, photo: VisitPhoto)
    val errorFlow: MutableSharedFlow<Exception>
}

class PhotoUploadInteractorImpl(
        private val visitsRepository: VisitsRepository,
        private val fileRepository: FileRepository,
        private val crashReportsProvider: CrashReportsProvider,
        private val imageDecoder: ImageDecoder,
        private val apiClient: ApiClient,
        private val scope: CoroutineScope,
        private val retryParams: RetryParams
) : PhotoUploadInteractor {

    override val errorFlow = MutableSharedFlow<Exception>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        val oldPhotos = mutableMapOf<String, VisitPhoto>().apply {
            visitsRepository.visits.forEach { visit ->
                visit.photos.filter { it.state != VisitPhotoState.UPLOADED }.forEach {
                    put(visit._id, it)
                }
            }
        }

        scope.launch {
            oldPhotos.forEach {
                uploadPhoto(it.key, it.value)
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
            setVisitImageState(visitId = visitId, imageId = photo.imageId, VisitPhotoState.NOT_UPLOADED)
            retryWithBackoff(retryParams) {
                uploadImage(imageId = photo.imageId, imagePath = photo.filePath)
            }
            setVisitImageState(visitId = visitId, imageId = photo.imageId, VisitPhotoState.UPLOADED)
            fileRepository.deleteIfExists(photo.filePath)
        } catch (t: Exception) {
            setVisitImageState(visitId = visitId, imageId = photo.imageId, VisitPhotoState.ERROR)
            errorFlow.emit(t)
            when (t) {
                is java.net.UnknownHostException, is java.net.ConnectException, is java.net.SocketTimeoutException -> {
                    Log.i(VisitsRepository.TAG, "Failed to upload image", t)
                }
                else -> crashReportsProvider.logException(t)
            }
        }
    }

    private suspend fun uploadImage(
            imageId: String,
            imagePath: String,
    ) {
        try {
            val uploadedImage = imageDecoder.readBitmap(imagePath, MAX_IMAGE_SIDE_LENGTH_PX)
            apiClient.uploadImage(imageId, uploadedImage)
            // Log.v(TAG, "Updated visit pic in target $target")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun setVisitImageState(visitId: String, imageId: String, state: VisitPhotoState) {
        visitsRepository.getVisit(visitId)?.let { visit ->
            visit.photos.firstOrNull { it.imageId == imageId }?.state = state
            visitsRepository.updateItem(visit._id, visit)
        }
    }
}
