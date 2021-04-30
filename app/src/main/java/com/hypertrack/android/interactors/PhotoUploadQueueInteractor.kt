package com.hypertrack.android.interactors

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.RetryParams
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.retryWithBackoff
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.common.toMap
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import com.hypertrack.logistics.android.github.BuildConfig
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

interface PhotoUploadQueueInteractor {
    fun addToQueue(photo: PhotoForUpload)
    fun retry(photoId: String)
    val errorFlow: MutableSharedFlow<Consumable<Exception>>
    val queue: LiveData<Map<String, PhotoForUpload>>
}

interface PhotoUploadQueueStorage {
    suspend fun getPhotosQueue(): Set<PhotoForUpload>
    suspend fun getPhotoFromQueue(photoId: String): PhotoForUpload?
    suspend fun addToPhotosQueue(photo: PhotoForUpload)
    suspend fun updatePhotoState(photoId: String, state: PhotoUploadingState)
}

@JsonClass(generateAdapter = true)
class PhotoForUpload(
    val photoId: String,
    val filePath: String,
    //todo test
    val base64thumbnail: String,
    var state: PhotoUploadingState
) {
    override fun toString(): String {
        return " $photoId $state"
    }
}

enum class PhotoUploadingState {
    UPLOADED, NOT_UPLOADED, ERROR
}

class PhotoUploadQueueInteractorImpl(
    private val queueStorage: PhotoUploadQueueStorage,
    private val fileRepository: FileRepository,
    private val crashReportsProvider: CrashReportsProvider,
    private val imageDecoder: ImageDecoder,
    private val apiClient: ApiClient,
    private val scope: CoroutineScope,
    private val retryParams: RetryParams
) : PhotoUploadQueueInteractor {

    override val queue = MutableLiveData<Map<String, PhotoForUpload>>(mapOf())

    override val errorFlow = MutableSharedFlow<Consumable<Exception>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch {
            queueStorage.getPhotosQueue()
                .filter { it.state != PhotoUploadingState.UPLOADED }
                .toMap { it.photoId }.forEach {
                    uploadPhotoSetStateAndUpdateLiveData(it.value)
                }
        }
    }

    override fun addToQueue(photo: PhotoForUpload) {
        scope.launch {
            queueStorage.addToPhotosQueue(photo)
            updateQueueLiveData()
            uploadPhotoSetStateAndUpdateLiveData(photo)
        }
    }

    override fun retry(photoId: String) {
        scope.launch {
            queueStorage.getPhotoFromQueue(photoId)?.let {
                uploadPhotoSetStateAndUpdateLiveData(it)
            }
        }
    }

    private suspend fun uploadPhotoSetStateAndUpdateLiveData(photo: PhotoForUpload) {
        // Log.d(TAG, "Launched preview update task")
        try {
            saveImageState(
                imageId = photo.photoId,
                PhotoUploadingState.NOT_UPLOADED
            )
            updateQueueLiveData()
            retryWithBackoff(retryParams, {
                uploadImage(imageId = photo.photoId, imagePath = photo.filePath)
            }, shouldRetry = {
                return@retryWithBackoff when (it) {
                    is HttpException -> {
                        it.code() !in 400..499
                    }
                    else -> true
                }
            })

            saveImageState(
                imageId = photo.photoId,
                PhotoUploadingState.UPLOADED
            )
            updateQueueLiveData()
            fileRepository.deleteIfExists(photo.filePath)
        } catch (t: Exception) {
            saveImageState(
                imageId = photo.photoId,
                PhotoUploadingState.ERROR
            )
            updateQueueLiveData()
            errorFlow.emit(Consumable(t))
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

    private suspend fun saveImageState(
        imageId: String,
        state: PhotoUploadingState
    ) {
        queueStorage.updatePhotoState(imageId, state)
        updateQueueLiveData()
    }

    private suspend fun updateQueueLiveData() {
        queue.postValue(queueStorage.getPhotosQueue().toMap { it.photoId })
    }


}
