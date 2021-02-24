package com.hypertrack.android.interactors

import android.util.Log
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.retryWithBackoff
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PhotoUploadInteractor {
    fun addToQueue(visitId: String, imageId: String, filePath: String)
}

interface UploadQueueStorageRepository {
    fun getUploadingPhotos(): Set<UploadingPhoto>
    fun addUploadingPhoto(photo: UploadingPhoto)
    fun deleteUploadingPhoto(photoId: String)
}

@JsonClass(generateAdapter = true)
class UploadingPhoto(
    val visitId: String,
    val imageId: String,
    val filePath: String,
)

class PhotoUploadInteractorImpl(
        private val visitsRepository: VisitsRepository,
        private val fileRepository: FileRepository,
        private val uploadQueueStorageRepository: UploadQueueStorageRepository,
        private val crashReportsProvider: CrashReportsProvider,
        private val imageDecoder: ImageDecoder,
        private val apiClient: ApiClient,
        private val scope: CoroutineScope,
) : PhotoUploadInteractor {

    private val loadingQueue = Channel<UploadingPhoto>(5)

    init {
        scope.launch {
            val queue = uploadQueueStorageRepository.getUploadingPhotos()
            queue.forEach { loadingQueue.send(it) }

            while (true) {
                val photo = loadingQueue.receive()
                uploadPhoto(photo)
            }
        }
    }

    override fun addToQueue(visitId: String, imageId: String, filePath: String) {
        scope.launch {
            val photo = UploadingPhoto(visitId = visitId, imageId = imageId, filePath = filePath)
            uploadQueueStorageRepository.addUploadingPhoto(photo)
            loadingQueue.send(photo)
        }
    }

    private suspend fun uploadPhoto(photo: UploadingPhoto) {
        // Log.d(TAG, "Launched preview update task")
        try {
            retryWithBackoff(
                    times = 5, factor = 10.0,
                    block = { uploadImage(imageId = photo.imageId, imagePath = photo.filePath, visitId = photo.visitId) }
            )
            uploadQueueStorageRepository.deleteUploadingPhoto(photo.imageId)
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
                    visit.visitPicturesIds.add(imageId)
                    visitsRepository.updateItem(visitId, visit)
                }
            }
            // Log.v(TAG, "Updated visit pic in target $target")
        } catch (e: Exception) {
            //todo
            throw e
        }
    }
}
