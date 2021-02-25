package com.hypertrack.android.interactors

import android.graphics.BitmapFactory
import android.os.Build
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.models.Visit
import com.hypertrack.android.models.VisitPhoto
import com.hypertrack.android.models.VisitPhotoState
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
        every { photos } returns mutableListOf(
            VisitPhoto("5", "path5", "", VisitPhotoState.NOT_UPLOADED)
        )
    }
    private val visit2 = mockk<Visit>(relaxed = true) {
        every { _id } returns "2"
        every { photos } returns mutableListOf(
            VisitPhoto("6", "path6", "", VisitPhotoState.ERROR)
        )
    }

    lateinit var photoUploadInteractorImpl: PhotoUploadInteractorImpl

    lateinit var imageDecoder: ImageDecoder
    lateinit var apiClient: ApiClient
    lateinit var visitsRepository: VisitsRepository
    lateinit var crashReportsProvider: CrashReportsProvider
    private val mockWebServer = MockWebServer()

    val finishChannel = Channel<Boolean>()

    var fileRepository = TestFileRepository()

    inner class TestFileRepository : FileRepository {
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
        every { visitsRepository.visits } returns listOf(visit1, visit2)
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
            mockWebServer.enqueue(MockResponse().setResponseCode(500))
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
                crashReportsProvider,
                imageDecoder,
                apiClient,
                CoroutineScope(Dispatchers.Unconfined),
            )

            VisitPhoto("1", "path", "", VisitPhotoState.NOT_UPLOADED).also {
              visit1.photos.add(it)
              photoUploadInteractorImpl.addToQueue(visit1._id, it)
            }
            VisitPhoto("2", "path1", "", VisitPhotoState.NOT_UPLOADED).also {
              visit1.photos.add(it)
              photoUploadInteractorImpl.addToQueue(visit1._id, it)
            }
            VisitPhoto("3", "path2", "", VisitPhotoState.NOT_UPLOADED).also {
              visit2.photos.add(it)
              photoUploadInteractorImpl.addToQueue(visit2._id, it)
            }
            VisitPhoto("4", "path3", "", VisitPhotoState.NOT_UPLOADED).also {
              visit2.photos.add(it)
              photoUploadInteractorImpl.addToQueue(visit2._id, it)
            }

            finishChannel.receive()

            coVerifyAll {
                imageDecoder.readBitmap("path", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path1", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path2", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path3", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path5", MAX_IMAGE_SIDE_LENGTH_PX)
                imageDecoder.readBitmap("path6", MAX_IMAGE_SIDE_LENGTH_PX)
            }

            val requests = (1..6).map { mockWebServer.takeRequest() }.map { it.body.toString() }

//            requests.forEach { System.out.println(it) }
//            visit1.photos.forEach { System.out.println("${it.imageId} ${it.state}") }
//            visit2.photos.forEach { System.out.println("${it.imageId} ${it.state}") }

            listOf(1, 2, 5).forEach { id ->
                assertTrue(requests.any {
                    it.contains("\"file_name\":\"${id}\"")
                })
                assertTrue(visit1.photos.first { it.imageId == id.toString() }.state == VisitPhotoState.UPLOADED)
            }

            listOf(3, 4, 6).forEach { id ->
                assertTrue(requests.any {
                    it.contains("\"file_name\":\"${id}\"")
                })
                assertTrue(visit2.photos.first { it.imageId == id.toString() }.state == VisitPhotoState.UPLOADED)
            }

            assertEquals(7, mockWebServer.requestCount)
            assertEquals(6, fileRepository.fileCount)

            assertTrue(visitsRepository.visits.all { visit -> visit.photos.all { it.state == VisitPhotoState.UPLOADED } })
        }

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}