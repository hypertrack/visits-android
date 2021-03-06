package com.hypertrack.android.interactors

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Looper
import com.hypertrack.android.RetryParams
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitPhoto
import com.hypertrack.android.models.VisitPhotoState
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
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
class PhotoUploadInteractorTest {

    private val visit1 = mockk<Visit>(relaxed = true) {
        every { _id } returns "1"
        every { photos } returns mutableListOf()
    }
    private val visit2 = mockk<Visit>(relaxed = true) {
        every { _id } returns "2"
        every { photos } returns mutableListOf()
    }

    private lateinit var photoUploadInteractorImpl: PhotoUploadInteractorImpl

    private lateinit var imageDecoder: ImageDecoder
    private lateinit var apiClient: ApiClient
    private lateinit var visitsRepository: VisitsRepository
    private lateinit var crashReportsProvider: CrashReportsProvider

    private val finishChannel = Channel<Boolean>()

    private var fileRepository = TestFileRepository()

    inner class TestFileRepository : FileRepository {
        var fileCount = 0

        override fun deleteIfExists(path: String) {
            fileCount++
            checkFinish()
        }
    }

    private var errors = 0

    private val retriesLeft = mutableMapOf(
            "3" to 1,
            "2" to 9999,
            "5" to 2,
            "4" to 2,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        imageDecoder = mockk(relaxed = true)
        every {
            imageDecoder.readBitmap("1", MAX_IMAGE_SIDE_LENGTH_PX)
        } returns BitmapFactory.decodeByteArray(ByteArray(1) { 0 }, 0, 1)
        visitsRepository = mockk(relaxed = true)
        every { visitsRepository.getVisit("1") } returns visit1
        every { visitsRepository.getVisit("2") } returns visit2
        every { visitsRepository.visits } returns listOf(visit1, visit2)
        crashReportsProvider = mockk(relaxed = true)

        apiClient = mockk()
        coEvery { apiClient.uploadImage(any(), any()) } answers {
            val id = args[0] as String

            retriesLeft[id]?.let {
                if (it > 0) {
                    retriesLeft[id] = it - 1
                    if(id == "4") {
                        throw HttpException(Response.error<String>(401, "".toResponseBody("application/json".toMediaTypeOrNull())))
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
            visit1.photos.add(
                    VisitPhoto("5", "path5", "", VisitPhotoState.NOT_UPLOADED)
            )

            visit2.photos.add(
                    VisitPhoto("6", "path6", "", VisitPhotoState.ERROR)
            )

            photoUploadInteractorImpl = PhotoUploadInteractorImpl(
                    visitsRepository,
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


            VisitPhoto("1", "path1", "", VisitPhotoState.NOT_UPLOADED).also {
                visit1.photos.add(it)
                photoUploadInteractorImpl.addToQueue(visit1._id, it)
            }
            VisitPhoto("2", "path2", "", VisitPhotoState.NOT_UPLOADED).also {
                visit1.photos.add(it)
                photoUploadInteractorImpl.addToQueue(visit1._id, it)
            }
            VisitPhoto("3", "path3", "", VisitPhotoState.NOT_UPLOADED).also {
                visit2.photos.add(it)
                photoUploadInteractorImpl.addToQueue(visit2._id, it)
            }

            VisitPhoto("4", "path4", "", VisitPhotoState.NOT_UPLOADED).also {
                visit2.photos.add(it)
                photoUploadInteractorImpl.addToQueue(visit2._id, it)
            }

            shadowOf(Looper.getMainLooper()).idle()

            finishChannel.receive()


            println("after finish")

            visit1.photos.forEach { println("${it.imageId} ${it.state}") }
            visit2.photos.forEach { println("${it.imageId} ${it.state}") }

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

            val photos = visit1.photos.apply { addAll(visit2.photos) }
            assertEquals(6, photos.size)
            listOf(1, 6).map { it.toString() }.forEach { id ->
                assertEquals(id, VisitPhotoState.UPLOADED, photos.firstOrNull { it.imageId == id }?.state)
            }
            listOf(3, 5).map { it.toString() }.forEach { id ->
                assertEquals(id, VisitPhotoState.UPLOADED, photos.firstOrNull { it.imageId == id }?.state)
            }

            assertEquals(2, errors)
            listOf(2, 4).map { it.toString() }.forEach { id ->
                assertEquals(id, VisitPhotoState.ERROR, photos.firstOrNull { it.imageId == id }?.state)
            }
            assertEquals(1, retriesLeft["4"])

            assertEquals(6-errors, fileRepository.fileCount)

            errorJob.cancel()
        }

    }

    private fun checkFinish() {
        if (fileRepository.fileCount + errors >= 6) {
            println("finish")
            GlobalScope.launch { finishChannel.send(true) }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}