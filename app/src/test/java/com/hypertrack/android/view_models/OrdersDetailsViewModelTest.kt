package com.hypertrack.android.view_models

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.MainCoroutineScopeRule
import com.hypertrack.android.createBaseOrder
import com.hypertrack.android.createBaseTrip
import com.hypertrack.android.interactors.*
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.TripCompletionSuccess
import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.models.local.LocalTrip
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.models.local.TripStatus
import com.hypertrack.android.observeAndGetValue
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.ui.common.formatUnderscore
import com.hypertrack.android.ui.screens.order_details.OrderDetailsViewModel
import com.hypertrack.android.ui.screens.order_details.PhotoItem
import com.hypertrack.android.ui.screens.visit_details.VisitDetailsFragment
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.logistics.android.github.R
import com.hypertrack.sdk.HyperTrack
import io.mockk.*
import junit.framework.Assert.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File

@Suppress("EXPERIMENTAL_API_USAGE")
class OrdersDetailsViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `it should show correct view state for ongoing order`() {
        runBlocking {
            val tripsInteractor: TripsInteractor = mockk() {
                every { getOrder(any()) } returns LocalOrder(
                    createBaseOrder(),
                    note = null,
                    isPickedUp = false
                )
                every { errorFlow } returns MutableSharedFlow()
                every { currentTrip } returns MutableLiveData<LocalTrip>()
            }

            var isPickUpAllowed = true
            createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertTrue(it.isNoteEditable.observeAndGetValue())
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertTrue(it.showPickUpButton.observeAndGetValue())
                assertEquals(
                    "false",
                    getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )?.toLowerCase()
                )
            }

            isPickUpAllowed = false
            createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertTrue(it.isNoteEditable.observeAndGetValue())
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertFalse(it.showPickUpButton.observeAndGetValue())
                //todo enable on twmo
//                assertTrue(it.showAddPhoto.observeAndGetValue())
                assertNull(
                    getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )
                )
            }
        }
    }

    @Test
    fun `it should update order state on pick up button click`() {
        val backendOrders = listOf<Order>(
            createBaseOrder().copy(
                id = "ONGOING",
                _status = OrderStatus.ONGOING.value
            ),
        )

        val pickUpAllowed = true
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(orders = backendOrders)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            createVm("ONGOING", tripsInteractor, pickUpAllowed).let {
                it.onPickUpClicked()

                assertFalse(it.showPickUpButton.observeAndGetValue())
                assertEquals(
                    "true",
                    getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )?.toLowerCase()
                )
            }
        }
    }

    @Test
    fun `it should update order state on complete button click`() {
        val backendOrders = listOf<Order>(
            createBaseOrder().copy(
                id = "ONGOING",
                _status = OrderStatus.ONGOING.value
            ),
        )

        val pickUpAllowed = true
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(orders = backendOrders)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            createVm("ONGOING", tripsInteractor, pickUpAllowed).let {
                it.onCompleteClicked()

                assertEquals(
                    OrderStatus.COMPLETED.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertFalse(it.showCompleteButtons.observeAndGetValue())
                assertFalse(it.showPickUpButton.observeAndGetValue())
                assertFalse(it.isNoteEditable.observeAndGetValue())
                assertFalse(it.showAddPhoto.observeAndGetValue())
                assertNull(getFromMetadata("order_picked_up", it.metadata.observeAndGetValue()))
            }
        }
    }

    @Test
    fun `it should save note on legacy order completion`() {
        val pickUpAllowed = true
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(tripId = "1", orders = null)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            createVm("1", tripsInteractor, pickUpAllowed).let {
                it.onCompleteClicked("Note")

                assertEquals("Note", it.note.observeAndGetValue())
            }
        }
    }

    @Test
    fun `it should update order state on cancel button click`() {
        val backendOrders = listOf<Order>(
            createBaseOrder().copy(
                id = "ONGOING",
                _status = OrderStatus.ONGOING.value
            ),
        )

        val pickUpAllowed = true
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(orders = backendOrders)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            createVm("ONGOING", tripsInteractor, pickUpAllowed).let {
                it.onCancelClicked()

                assertEquals(
                    OrderStatus.CANCELED.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertFalse(it.showCompleteButtons.observeAndGetValue())
                assertFalse(it.showPickUpButton.observeAndGetValue())
                assertFalse(it.isNoteEditable.observeAndGetValue())
                assertFalse(it.showAddPhoto.observeAndGetValue())
                assertNull(getFromMetadata("order_picked_up", it.metadata.observeAndGetValue()))
            }
        }
    }

    @Test
    fun `it should show correct view state for ongoing legacy order`() {
        runBlocking {
            val tripsInteractor: TripsInteractor = mockk() {
                every { getOrder(any()) } returns LocalOrder(
                    createBaseOrder(),
                    note = "Note",
                    legacy = true,
                    isPickedUp = false
                )
                every { errorFlow } returns MutableSharedFlow()
                every { currentTrip } returns MutableLiveData<LocalTrip>()
            }

            var isPickUpAllowed = true
            createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertTrue(it.showPickUpButton.observeAndGetValue())
                assertTrue(it.showPhotosGroup.observeAndGetValue())
                assertEquals("Note", it.note.observeAndGetValue())
                assertEquals(
                    "false",
                    getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )?.toLowerCase()
                )
            }

            isPickUpAllowed = false
            createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    getFromMetadata("order_status", it.metadata.observeAndGetValue())
                )
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertFalse(it.showPickUpButton.observeAndGetValue())
                assertNull(
                    getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )
                )
            }
        }
    }

    @Test
    fun `it should send geotag on legacy order complete`() {
        runBlocking {
            val backendTrips = listOf(
                createBaseTrip().copy(
                    tripId = "1",
                    status = TripStatus.ACTIVE.value,
                    orders = null
                ),
            )
            val apiClient: ApiClient = mockk {
                coEvery { getTrips() } returns backendTrips
                coEvery { completeTrip(any()) } returns TripCompletionSuccess
            }
            val slot = slot<Map<String, Any>>()
            val sdk: HyperTrack = mockk() {
                every { addGeotag(capture(slot), any()) } returns this
                every { isRunning } returns true
            }
            val hts = HyperTrackService(mockk(relaxed = true), sdk)
            val tripsInteractor = TripInteractorTest.createTripInteractorImpl(
                tripStorage = mockk() {
                    coEvery { getTrips() } returns listOf(
                        LocalTrip(
                            "1", TripStatus.ACTIVE, mapOf(), orders = listOf(
                                LocalOrder(
                                    createBaseOrder().copy(id = "1"),
                                    false,
                                    "Note",
                                    legacy = true,
                                    photos = mutableSetOf(
                                        TripInteractorTest.createBasePhotoForUpload(photoId = "1"),
                                        TripInteractorTest.createBasePhotoForUpload(photoId = "2"),
                                    )
                                )
                            ).toMutableList()
                        )
                    )
                    coEvery { saveTrips(any()) } returns Unit
                },
                backendTrips = backendTrips,
                accountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
                apiClient = apiClient,
                hyperTrackService = hts
            )
            runBlocking {
                tripsInteractor.refreshTrips()
                assertEquals(
                    "Note",
                    tripsInteractor.currentTrip.observeAndGetValue()!!.orders.first().note
                )
            }

            createVm("1", tripsInteractor, false).let {
                it.onCompleteClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("VISIT_MARKED_COMPLETE", it["type"])
                assertEquals("Note", it["order_note"])
                assertTrue(it["order_photos"] != null)
                assertEquals(2, (it["order_photos"]!! as Set<*>).size)
            }
        }
    }

    @Test
    fun `it should send geotag on legacy order cancel`() {
        runBlocking {
            val backendTrips = listOf(
                createBaseTrip().copy(
                    tripId = "1",
                    status = TripStatus.ACTIVE.value,
                    orders = null
                ),
            )
            val apiClient: ApiClient = mockk {
                coEvery { getTrips() } returns backendTrips
                coEvery { completeTrip(any()) } returns TripCompletionSuccess
            }
            val slot = slot<Map<String, Any>>()
            val sdk: HyperTrack = mockk() {
                every { addGeotag(capture(slot), any()) } returns this
                every { isRunning } returns true
            }
            val hts = HyperTrackService(mockk(relaxed = true), sdk)
            val tripsInteractor = TripInteractorTest.createTripInteractorImpl(
                tripStorage = mockk() {
                    coEvery { getTrips() } returns listOf(
                        LocalTrip(
                            "1", TripStatus.ACTIVE, mapOf(), orders = listOf(
                                LocalOrder(
                                    createBaseOrder().copy(id = "1"),
                                    false,
                                    "Note",
                                    legacy = true,
                                    photos = mutableSetOf(
                                        TripInteractorTest.createBasePhotoForUpload(photoId = "1"),
                                        TripInteractorTest.createBasePhotoForUpload(photoId = "2"),
                                    )
                                )
                            ).toMutableList()
                        )
                    )
                    coEvery { saveTrips(any()) } returns Unit
                },
                backendTrips = backendTrips,
                accountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
                apiClient = apiClient,
                hyperTrackService = hts
            )
            runBlocking {
                tripsInteractor.refreshTrips()
                assertEquals(
                    "Note",
                    tripsInteractor.currentTrip.observeAndGetValue()!!.orders.first().note
                )
            }

            createVm("1", tripsInteractor, false).let {
                it.onCancelClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("VISIT_MARKED_CANCELED", it["type"])
                assertEquals("Note", it["order_note"])
                assertTrue(it["order_photos"] != null)
                assertEquals(2, (it["order_photos"]!! as Set<*>).size)
            }
        }
    }

    @Test
    fun `it should persist order note on legacy trip`() {
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(tripId = "1", orders = null)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns false }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            createVm("1", tripsInteractor).let {
                it.onSaveNote("New note")
            }

            tripsInteractor.refreshTrips()

            createVm("1", tripsInteractor).let {
                assertEquals("New note", it.note.observeAndGetValue())
            }
        }
    }

    @Test
    fun `it should send geotag on legacy order pick up`() {
        runBlocking {
            val backendTrips = listOf(
                createBaseTrip().copy(
                    tripId = "1",
                    status = TripStatus.ACTIVE.value,
                    orders = null
                ),
            )
            val apiClient: ApiClient = mockk {
                coEvery { getTrips() } returns backendTrips
                coEvery { completeTrip(any()) } returns TripCompletionSuccess
            }
            val slot = slot<Map<String, String>>()
            val sdk: HyperTrack = mockk() {
                every { addGeotag(capture(slot), any()) } returns this
                every { addGeotag(capture(slot)) } returns this
                every { isRunning } returns true
            }
            val hts = HyperTrackService(mockk(relaxed = true), sdk)
            val tripsInteractor = TripInteractorTest.createTripInteractorImpl(
                tripStorage = mockk() {
                    coEvery { getTrips() } returns listOf(
                        LocalTrip(
                            "1", TripStatus.ACTIVE, mapOf(), orders = listOf(
                                LocalOrder(
                                    createBaseOrder().copy(id = "1"),
                                    false,
                                    "Note",
                                    legacy = true
                                )
                            ).toMutableList()
                        )
                    )
                    coEvery { saveTrips(any()) } returns Unit
                },
                backendTrips = backendTrips,
                accountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
                apiClient = apiClient,
                hyperTrackService = hts
            )
            runBlocking {
                tripsInteractor.refreshTrips()
                assertEquals(
                    "Note",
                    tripsInteractor.currentTrip.observeAndGetValue()!!.orders.first().note
                )
            }

            createVm("1", tripsInteractor, false).let {
                it.onPickUpClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("PICK_UP", it["type"])
            }
        }
    }

    @Test
    fun `it should upload order photo`() {
        runBlocking {
            val backendTrips = listOf(
                createBaseTrip().copy(
                    tripId = "1",
                    status = TripStatus.ACTIVE.value,
                    orders = null
                ),
            )
            val queueInteractor = object : PhotoUploadQueueInteractor {
                override fun addToQueue(photo: PhotoForUpload) {
                    queue.postValue(queue.value!!.toMutableMap().apply {
                        put(photo.photoId, photo.apply {
                            state = PhotoUploadingState.UPLOADED
                        })
                    })
                }

                override fun retry(photoId: String) {
                }

                override val errorFlow = MutableSharedFlow<Consumable<Exception>>()
                override val queue = MutableLiveData<Map<String, PhotoForUpload>>(mapOf())
            }
            assertTrue(queueInteractor.queue.value!!.isEmpty())
            val tripsInteractor = TripInteractorTest.createTripInteractorImpl(
                backendTrips = backendTrips,
                queueInteractor = queueInteractor
            )
            tripsInteractor.refreshTrips()


            createVm("1", tripsInteractor, false, queueInteractor).let {
                val activity = mockk<Activity>(relaxed = true)

                it.onAddPhotoClicked(activity, "Note")

                verify { activity.startActivityForResult(any(), any()) }
                assertEquals(
                    "Note",
                    tripsInteractor.currentTrip.observeAndGetValue()!!.orders.first().note
                )

                it.onActivityResult(
                    VisitDetailsFragment.REQUEST_IMAGE_CAPTURE,
                    AppCompatActivity.RESULT_OK,
                    null
                )

                tripsInteractor.getOrder("1").photos.let {
                    assertEquals(1, it.size)
                }

                it.photos.observeAndGetValue().let {
                    assertEquals(PhotoUploadingState.UPLOADED, it[0].state)
                }
            }
        }
    }

    @Test
    fun `it should retry upload on order photo click (if error)`() {
        runBlocking {
            val backendTrips = listOf(
                createBaseTrip().copy(
                    tripId = "1",
                    status = TripStatus.ACTIVE.value,
                    orders = null
                ),
            )
            val ld = MutableLiveData<Map<String, PhotoForUpload>>(
                mapOf(
                    "1" to TripInteractorTest.createBasePhotoForUpload(
                        "1",
                        "",
                        PhotoUploadingState.ERROR
                    ),
                    "2" to TripInteractorTest.createBasePhotoForUpload(
                        "2",
                        "",
                        PhotoUploadingState.NOT_UPLOADED
                    ),
                    "3" to TripInteractorTest.createBasePhotoForUpload(
                        "3",
                        "",
                        PhotoUploadingState.UPLOADED
                    ),
                )
            )
            val slot = slot<String>()
            val queueInteractor = mockk<PhotoUploadQueueInteractor>() {
                every { retry(capture(slot)) } returns Unit
                every { queue } returns ld
            }
            val tripsInteractor = mockk<TripsInteractor>(relaxed = true) {
                every { getOrder("1") } returns LocalOrder(
                    createBaseOrder(), true, null, false,
                    listOf("1", "2", "3").map {
                        TripInteractorTest.createBasePhotoForUpload(
                            it,
                            "",
                            state = PhotoUploadingState.NOT_UPLOADED
                        )
                    }.toMutableSet()
                )
            }
            createVm("1", tripsInteractor, false, queueInteractor).let {
                it.photos.observeAndGetValue().let {
                    assertEquals(3, it.size)
                    assertEquals(PhotoUploadingState.ERROR, it.first { it.photoId == "1" }.state)
                    assertEquals(
                        PhotoUploadingState.NOT_UPLOADED,
                        it.first { it.photoId == "2" }.state
                    )
                    assertEquals(PhotoUploadingState.UPLOADED, it.first { it.photoId == "3" }.state)
                }

                it.onPhotoClicked("1")
                it.onPhotoClicked("2")
                it.onPhotoClicked("3")

                coVerifyAll {
                    queueInteractor.queue
                    queueInteractor.queue
                    queueInteractor.retry("1")
                }
            }
        }
    }

    companion object {
        fun getFromMetadata(key: String, metadata: List<KeyValueItem>): String? {
            return metadata.firstOrNull {
                it.key == key.formatUnderscore()
            }?.value
        }

        fun createVm(
            id: String,
            tripsInteractor: TripsInteractor,
            pickUpAllowed: Boolean = false,
            photoUploadInteractor: PhotoUploadQueueInteractor = mockk(relaxed = true)
        ): OrderDetailsViewModel {
            return OrderDetailsViewModel(
                id,
                tripsInteractor,
                photoUploadInteractor,
                mockk(relaxed = true) {
                    every { stringFromResource(R.string.order_status) } returns "order_status"
                    every { stringFromResource(R.string.order_picked_up) } returns "order_picked_up"
                    every { cacheDir } returns File("nofile")
                    every { createImageFile() } returns File("nofile")
                },
                mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed },
                mockk() {}
            )
        }
    }

}