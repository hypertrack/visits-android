package com.hypertrack.android.interactors

import android.graphics.BitmapFactory
import android.os.Build
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.Visit
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.android.repository.FileRepository
import com.hypertrack.android.repository.VisitsRepository
import com.hypertrack.android.utils.CrashReportsProvider
import com.hypertrack.android.utils.ImageDecoder
import com.hypertrack.android.utils.MAX_IMAGE_SIDE_LENGTH_PX
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class) //Location class in Android
@Config(sdk = [Build.VERSION_CODES.P])
class PhotoUploadInteractorTest() {

    private val visit1 = mockk<Visit>(relaxed = true) {
        every { _id } returns "1"
        every { visitPicturesIds } returns mutableListOf()
    }
    private val visit2 = mockk<Visit>(relaxed = true) {
        every { _id } returns "2"
        every { visitPicturesIds } returns mutableListOf()
    }

    lateinit var imageDecoder: ImageDecoder
    lateinit var apiClient: ApiClient
    lateinit var visitsRepository: VisitsRepository
    lateinit var crashReportsProvider: CrashReportsProvider
    lateinit var photoUploadInteractorImpl: PhotoUploadInteractorImpl
    private val mockWebServer = MockWebServer()

    val finishChannel = Channel<Boolean>()

    var fileRepository = TestFileRepository()
    inner class TestFileRepository: FileRepository {
        var fileCount = 0

        override fun deleteIfExists(path: String) {
            fileCount++
            if (fileCount >= 6) {
                runBlocking {
                    finishChannel.send(true)
                }
            }
        }
    }

    var uploadQueueStorageRepository = TestUploadQueueStorageRepository()
    inner class TestUploadQueueStorageRepository: UploadQueueStorageRepository {
        val startingQueue = setOf(
            UploadingPhoto("1", "5", "path5"),
            UploadingPhoto("2", "6", "path6"),
        )

        val addCalls = mutableListOf<UploadingPhoto>()
        val deleteCalls = mutableListOf<String>()

        override fun getUploadingPhotos(): Set<UploadingPhoto> {
            return startingQueue
        }

        override fun addUploadingPhoto(photo: UploadingPhoto) {
            addCalls.add(photo)
        }

        override fun deleteUploadingPhoto(photoId: String) {
            deleteCalls.add(photoId)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        imageDecoder = mockk<ImageDecoder>(relaxed = true)
        every {
            imageDecoder.readBitmap("1", MAX_IMAGE_SIDE_LENGTH_PX)
        } returns BitmapFactory.decodeByteArray(ByteArray(1) { 0 }, 0, 1)
        visitsRepository = mockk<VisitsRepository>(relaxed = true)
        every { visitsRepository.getVisit("1") } returns visit1
        every { visitsRepository.getVisit("2") } returns visit2
        crashReportsProvider = mockk<CrashReportsProvider>(relaxed = true)

        mockWebServer.start()
        val accessTokenRepo = mockk<AccessTokenRepository>()
        every { accessTokenRepo.getAccessToken() } returns "fake.jwt.token"
        every { accessTokenRepo.refreshToken() } returns "new.jwt.token"
        apiClient = ApiClient(accessTokenRepo, mockWebServer.url("/").toString(), "1")
    }

    @Test
    fun uploadImagesQueue() {
        runBlocking {
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )
            mockWebServer.enqueue(
                MockResponse().setBody("""{"name": "a"}""").setBodyDelay(0, TimeUnit.MILLISECONDS)
            )

            photoUploadInteractorImpl = PhotoUploadInteractorImpl(
                visitsRepository,
                fileRepository,
                uploadQueueStorageRepository,
                crashReportsProvider,
                imageDecoder,
                apiClient,
                CoroutineScope(Dispatchers.Unconfined),
            )

            photoUploadInteractorImpl.addToQueue("1", "1", "path")
            photoUploadInteractorImpl.addToQueue("1", "2", "path1")
            photoUploadInteractorImpl.addToQueue("2", "3", "path2")
            photoUploadInteractorImpl.addToQueue("2", "4", "path3")

            finishChannel.receive()

            coVerifyAll {
                imageDecoder.readBitmap("path", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path1", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path2", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path3", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path5", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path6", MAX_IMAGE_SIDE_LENGTH_PX)
            }
            coVerifyAll {
                visitsRepository.getVisit("1")
                visitsRepository.updateItem("1", visit1)
                visitsRepository.getVisit("2")
                visitsRepository.updateItem("2", visit2)
            }
            assertTrue("1" in visit1.visitPicturesIds)
            assertTrue("2" in visit1.visitPicturesIds)
            assertTrue("5" in visit1.visitPicturesIds)

            assertTrue("3" in visit2.visitPicturesIds)
            assertTrue("4" in visit2.visitPicturesIds)
            assertTrue("6" in visit2.visitPicturesIds)

            assertEquals(6, mockWebServer.requestCount)
            assertEquals(6, fileRepository.fileCount)

            assertTrue((1..4).all { it.toString() in uploadQueueStorageRepository.addCalls.map { it.imageId } })
            assertTrue((1..6).all { it.toString() in uploadQueueStorageRepository.deleteCalls })
        }

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }
}