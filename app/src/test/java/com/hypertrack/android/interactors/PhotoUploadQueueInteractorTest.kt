package com.hypertrack.android.interactors

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Looper
import com.hypertrack.android.RetryParams
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class) //Location class in Android
@Config(sdk = [Build.VERSION_CODES.P])
@LooperMode(LooperMode.Mode.PAUSED)
class PhotoUploadQueueInteractorTest() {

    /*
    * Initial queue - photos 5 (NOT_UPLOADED) and 6 (ERROR)
    * Init interactor
    * Add 1-4 to queue (NOT_UPLOADED)
    * upload 2 - always return error
    * 4, 5 get uploaded after 1 retry
    * 4 falls with 401 (should not be retried)
    * */

    lateinit var photoUploadInteractorImpl: PhotoUploadQueueInteractorImpl

    lateinit var imageDecoder: ImageDecoder
    lateinit var apiClient: ApiClient
    lateinit var crashReportsProvider: CrashReportsProvider

    val finishChannel = Channel<Boolean>()

    var fileRepository = TestFileRepository()

    inner class TestFileRepository : FileRepository {
        var fileCount = 0

        override fun deleteIfExists(path: String) {
            fileCount++
            checkFinish()
        }
    }

    var errors = 0

    val retriesLeft = mutableMapOf<String, Int>(
        "3" to 1,
        "2" to 9999,
        "5" to 2,
        "4" to 2,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        imageDecoder = mockk<ImageDecoder>(relaxed = true)
        every {
            imageDecoder.readBitmap("1", MAX_IMAGE_SIDE_LENGTH_PX)
        } returns BitmapFactory.decodeByteArray(ByteArray(1) { 0 }, 0, 1)
        crashReportsProvider = mockk(relaxed = true)
        apiClient = mockk()

        coEvery { apiClient.uploadImage(any(), any()) } answers {
            val id = args[0] as String

            retriesLeft[id]?.let {
                if (it > 0) {
                    retriesLeft[id] = it - 1
                    if (id == "4") {
                        throw HttpException(
                            Response.error<String>(
                                401,
                                "".toResponseBody("application/json".toMediaTypeOrNull())
                            )
                        )
                    } else {
                        throw Exception("error")
                    }
                }
            }
        }
    }

    @Test
    fun uploadImagesQueue() {
        runBlocking {
            val initialQueue = listOf(
                TripInteractorTest.createBasePhotoForUpload(
                    photoId = "5",
                    filePath = "path5",
                    state = PhotoUploadingState.NOT_UPLOADED
                ),
                TripInteractorTest.createBasePhotoForUpload(
                    photoId = "6",
                    filePath = "path6",
                    state = PhotoUploadingState.ERROR
                )
            )

            val toUpload = listOf(
                TripInteractorTest.createBasePhotoForUpload(
                    "1",
                    "path1",
                    PhotoUploadingState.NOT_UPLOADED
                ),
                TripInteractorTest.createBasePhotoForUpload(
                    "2",
                    "path2",
                    PhotoUploadingState.NOT_UPLOADED
                ),
                TripInteractorTest.createBasePhotoForUpload(
                    "3",
                    "path3",
                    PhotoUploadingState.NOT_UPLOADED
                ),
                TripInteractorTest.createBasePhotoForUpload(
                    "4",
                    "path4",
                    PhotoUploadingState.NOT_UPLOADED
                )
            )

            val photos = mutableSetOf<PhotoForUpload>().apply {
                addAll(initialQueue)
            }

            val photosProvider: PhotoUploadQueueStorage = object : PhotoUploadQueueStorage {
                override suspend fun getPhotosQueue(): Set<PhotoForUpload> {
                    return photos
                }

                override suspend fun addToPhotosQueue(photo: PhotoForUpload) {
                    photos.add(photo)
                }

                override suspend fun updatePhotoState(photoId: String, state: PhotoUploadingState) {
                    photos.first { it.photoId == photoId }.state = state
                }

                override suspend fun getPhotoFromQueue(photoId: String): PhotoForUpload? {
                    return photos.first { it.photoId == photoId }
                }
            }

            photoUploadInteractorImpl = PhotoUploadQueueInteractorImpl(
                photosProvider,
                fileRepository,
                crashReportsProvider,
                imageDecoder,
                apiClient,
                this,
                RetryParams(
                    retryTimes = 3,
                    initialDelay = 1,
                    factor = 1.0,
                )
            )

            val errorJob = GlobalScope.launch {
                photoUploadInteractorImpl.errorFlow.collect {
                    println("err $it")
                    errors++
                    checkFinish()
                }
            }

            toUpload.forEach {
                photoUploadInteractorImpl.addToQueue(it)
            }

            shadowOf(Looper.getMainLooper()).idle()

            finishChannel.receive()

            println("after finish")

            photos.forEach { System.out.println("${it.photoId} ${it.state}") }

            coVerifyAll {
                (1..6).map { it.toString() }.forEach {
                    imageDecoder.readBitmap("path$it", MAX_IMAGE_SIDE_LENGTH_PX)
                }
            }

            coVerifyAll {
                (1..6).map { it.toString() }.forEach {
                    apiClient.uploadImage(it, any())
                }
            }

            listOf(1, 6).map { it.toString() }.forEach { id ->
                assertEquals(
                    id,
                    PhotoUploadingState.UPLOADED,
                    photos.firstOrNull { it.photoId == id }?.state
                )
            }
            listOf(3, 5).map { it.toString() }.forEach { id ->
                assertEquals(
                    id,
                    PhotoUploadingState.UPLOADED,
                    photos.firstOrNull { it.photoId == id }?.state
                )
            }

            assertEquals(2, errors)
            listOf(2, 4).map { it.toString() }.forEach { id ->
                assertEquals(
                    id,
                    PhotoUploadingState.ERROR,
                    photos.firstOrNull { it.photoId == id }?.state
                )
            }
            assertEquals(1, retriesLeft["4"])

            assertEquals(6 - errors, fileRepository.fileCount)

            errorJob.cancel()
        }

    }

    private fun checkFinish() {
//        System.out.println("fileCount + errors ${fileCount + errors}")
        if (fileRepository.fileCount + errors >= 6) {
            System.out.println("finish")
            GlobalScope.launch {
                finishChannel.send(true)
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}