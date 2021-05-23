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
import com.hypertrack.android.repository.TripsStorage
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.io.File

@Suppress("EXPERIMENTAL_API_USAGE")
class OrdersDetailsViewModelLegacyTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `it should show correct view state for ongoing legacy order`() {
        runBlocking {
            val tripsInteractor: TripsInteractor =
                OrdersDetailsViewModelTest.createTripsInteractorMock(orderSet = {
                    every { it.getOrderLiveData(any()) } returns MutableLiveData(
                        LocalOrder(
                            createBaseOrder(),
                            note = "Note",
                            metadata = null,
                            legacy = true,
                            isPickedUp = false
                        )
                    )
                })

            var isPickUpAllowed = true
            OrdersDetailsViewModelTest.createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    OrdersDetailsViewModelTest.getFromMetadata(
                        "order_status",
                        it.metadata.observeAndGetValue()
                    )
                )
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertTrue(it.showPickUpButton.observeAndGetValue())
                assertTrue(it.showPhotosGroup.observeAndGetValue())
                assertEquals("Note", it.note.observeAndGetValue())
                assertEquals(
                    "false",
                    OrdersDetailsViewModelTest.getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )?.toLowerCase()
                )
            }

            isPickUpAllowed = false
            OrdersDetailsViewModelTest.createVm("ONGOING", tripsInteractor, isPickUpAllowed).let {
                assertEquals(
                    OrderStatus.ONGOING.value,
                    OrdersDetailsViewModelTest.getFromMetadata(
                        "order_status",
                        it.metadata.observeAndGetValue()
                    )
                )
                assertTrue(it.showCompleteButtons.observeAndGetValue())
                assertFalse(it.showPickUpButton.observeAndGetValue())
                assertNull(
                    OrdersDetailsViewModelTest.getFromMetadata(
                        "order_picked_up",
                        it.metadata.observeAndGetValue()
                    )
                )
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

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor, pickUpAllowed).let {
                it.onCompleteClicked("Note")

                assertEquals("Note", it.note.observeAndGetValue())
            }
        }
    }

    @Test
    fun `it should save note on exit for legacy order`() {
        val pickUpAllowed = true
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(tripId = "1", orders = null)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns pickUpAllowed }
        )

        runBlocking {
            val vm = OrdersDetailsViewModelTest.createVm(
                "1",
                tripsInteractor,
                pickUpAllowed,
                scope = this
            )
            tripsInteractor.refreshTrips()

            vm.let {
                it.onExit("Note")
            }

            runBlocking {
                assertEquals("Note", vm.note.observeAndGetValue())
            }
        }
    }

    @Test
    fun `it should persist order note and photos for legacy order`() {
        val tripsInteractor: TripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(createBaseTrip().copy(tripId = "1", orders = null)),
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns false }
        )
        runBlocking {
            tripsInteractor.refreshTrips()

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor).let {
                it.onExit("New note")
            }

            tripsInteractor.refreshTrips()

            delay(100)

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor).let {
                assertEquals("New note", it.note.observeAndGetValue())
            }
        }
    }

    @Test
    fun `it should upload order photo for legacy order`() {
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


            OrdersDetailsViewModelTest.createVm("1", tripsInteractor, false, queueInteractor).let {
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

                tripsInteractor.getOrder("1")!!.photos.let {
                    assertEquals(1, it.size)
                }

                it.photos.observeAndGetValue().let {
                    assertEquals(PhotoUploadingState.UPLOADED, it[0].state)
                }
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
                                    metadata = null,
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

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor, false).let {
                it.onCompleteClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("VISIT_MARKED_COMPLETE", it["type"])
                assertEquals("Note", it["visit_note"])
                assertTrue(it["_visit_photos"] != null)
                assertEquals(2, (it["_visit_photos"]!! as Set<*>).size)
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
                                    metadata = null,
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

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor, false).let {
                it.onCancelClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("VISIT_MARKED_CANCELED", it["type"])
                assertEquals("Note", it["visit_note"])
                assertTrue(it["_visit_photos"] != null)
                assertEquals(2, (it["_visit_photos"]!! as Set<*>).size)
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
                                    metadata = null,
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

            OrdersDetailsViewModelTest.createVm("1", tripsInteractor, false).let {
                it.onPickUpClicked()
            }

            slot.captured.let {
                assertEquals("1", it["trip_id"])
                assertEquals("PICK_UP", it["type"])
            }
        }
    }

}