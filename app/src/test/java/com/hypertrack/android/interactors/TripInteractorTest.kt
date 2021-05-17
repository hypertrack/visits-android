package com.hypertrack.android.interactors

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hypertrack.android.api.*
import com.hypertrack.android.createBaseOrder
import com.hypertrack.android.createBaseTrip
import com.hypertrack.android.models.Metadata
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.TripCompletionSuccess
import com.hypertrack.android.models.local.LocalTrip
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.models.local.TripStatus
import com.hypertrack.android.observeAndAssertNull
import com.hypertrack.android.observeAndGetValue
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.TripsStorage
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.Injector
import io.mockk.*
import io.mockk.coVerify
import junit.framework.Assert.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@Suppress("EXPERIMENTAL_API_USAGE")
class TripInteractorTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    //todo test error emission
    @Test
    fun `it should load trips with orders`() {
        val backendTrips = listOf(
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder(),
                    createBaseOrder()
                ),
                status = TripStatus.ACTIVE.value
            ),
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder(),
                ),
                status = TripStatus.ACTIVE.value
            ),
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder(),
                ),
                status = TripStatus.COMPLETED.value
            ),
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder(),
                ),
                status = TripStatus.PROGRESSING_COMPLETION.value
            ),
        )
        val tripStorage = mockk<TripsStorage>() {
            coEvery { getTrips() } returns listOf()
            coEvery { saveTrips(any()) } returns kotlin.Unit
        }
        val tripsInteractor: TripsInteractor = createTripInteractorImpl(
            tripStorage, backendTrips
        )
        runBlocking {
            tripsInteractor.refreshTrips()
        }

        runBlocking {
            val completed = tripsInteractor.completedTrips.observeAndGetValue()
            assertEquals(2, completed.size)
            assertTrue(completed.all { it.orders.size == 1 })
            val slot = slot<List<LocalTrip>>()
            coVerify {
                tripStorage.saveTrips(capture(slot))
            }
            println(slot.captured)
            assertEquals(4, slot.captured.size)
        }
    }


    @Test
    fun `it should get orders list for current trip (first with ongoing status)`() {
        val backendOrders = listOf(
            createBaseTrip().copy(
                tripId = "tripId",
                status = TripStatus.ACTIVE.value, orders = listOf<Order>(
                    createBaseOrder().copy(
                        id = "1",
                        _status = OrderStatus.COMPLETED.value
                    ),
                    createBaseOrder().copy(
                        id = "2",
                        _status = OrderStatus.CANCELED.value
                    ),
                    createBaseOrder().copy(
                        id = "3",
                        _status = OrderStatus.ONGOING.value
                    ),
                )
            ),
            createBaseTrip().copy(
                tripId = "tripId1",
                status = TripStatus.ACTIVE.value, orders = listOf(
                    createBaseOrder().copy(
                        _status = OrderStatus.ONGOING.value
                    )
                )
            )
        )
        val tripsInteractor: TripsInteractor = createTripInteractorImpl(
            backendTrips = backendOrders
        )
        runBlocking {
            tripsInteractor.refreshTrips()
        }

        runBlocking {
            tripsInteractor.currentTrip.observeAndGetValue()!!.let {
                assertEquals("tripId", it.id)
                assertEquals(3, it.orders.size)
            }
        }
    }

    @Test
    fun `it should create trip with one order for first legacy trip (and ignore any others)`() {
        val backendTrips = listOf(
            createBaseTrip().copy(orders = null),
            createBaseTrip().copy(orders = listOf()),
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder()
                )
            ),
        )
        val tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndGetValue()!!.let { trip ->
                trip.orders.let {
                    assertEquals(1, it.size)
                    it[0].let { order ->
                        assertTrue(order.legacy)
                        assertEquals(trip.id, order.id)
                        assertEquals(OrderStatus.ONGOING, order.status)
                    }
                }
            }
        }
    }

    @Test
    fun `it should create legacy trip only for ongoing trip`() {
        val backendTrips = listOf(
            createBaseTrip().copy(status = TripStatus.COMPLETED.value, orders = null),
            createBaseTrip().copy(
                status = TripStatus.PROGRESSING_COMPLETION.value,
                orders = listOf()
            ),
            createBaseTrip().copy(
                status = TripStatus.UNKNOWN.value, orders = listOf(
                    createBaseOrder()
                )
            ),
        )
        val tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndAssertNull()
        }
    }

    @Test
    fun `it should create legacy trip with correct picked up state`() {
        val backendTrips = listOf(
            createBaseTrip().copy(status = TripStatus.ACTIVE.value, orders = null),
        )
        var tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips,
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns false }
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndGetValue().let {
                assertTrue(it!!.orders.first().isPickedUp)
            }
        }

        tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips,
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns true }
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndGetValue().let {
                assertFalse(it!!.orders.first().isPickedUp)
            }
        }
    }

    @Test
    fun `it should persist local orders state when refreshing trips`() {
        var trip =
            createBaseTrip().copy(
                orders = listOf(
                    createBaseOrder().copy(id = "1"),
                    createBaseOrder().copy(id = "2"),
                    createBaseOrder().copy(id = "3"),
                )
            )
        val tripsInteractorImpl = createTripInteractorImpl(
            tripStorage = createTripsStorage(),
            apiClient = mockk {
                coEvery { getTrips() } answers {
                    listOf(trip)
                }
                coEvery { completeOrder(any(), any()) } returns OrderCompletionSuccess
                coEvery { cancelOrder(any(), any()) } returns OrderCompletionSuccess
                coEvery { updateOrderMetadata(any(), any(), any()) } answers {
                    trip = trip.copy(orders = trip.orders!!.map {
                        if (it.id == firstArg()) {
                            val paramMd = thirdArg<Metadata>()
                            try {
                                val copyMd = it.copy(
                                    _metadata = paramMd.copy(visitsAppMetadata = paramMd.visitsAppMetadata.copy())
                                        .toMap()
                                )
                                return@map copyMd
                            } catch (e: Exception) {
                                e.printStackTrace()
                                throw e
                            }
                        } else {
                            it
                        }
                    })
                    Response.success(trip)
                }
            },
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns true }
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
            tripsInteractorImpl.setOrderPickedUp("1")
            tripsInteractorImpl.setOrderPickedUp("2")
            tripsInteractorImpl.addPhotoToOrder("2", "")
            tripsInteractorImpl.addPhotoToOrder("2", "")
            tripsInteractorImpl.addPhotoToOrder("3", "")
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndGetValue()!!.let { trip ->
                trip.orders.let { orders ->
                    orders[0].let {
                        assertEquals(true, it.isPickedUp)
                        assertEquals(0, it.photos.size)
                    }
                    orders[1].let {
                        assertEquals(true, it.isPickedUp)
                        assertEquals(2, it.photos.size)
                    }
                    orders[2].let {
                        assertEquals(false, it.isPickedUp)
                        assertEquals(1, it.photos.size)
                    }
                }
            }
        }
    }

    @Test
    fun `it should persist local orders state when refreshing trip for legacy trip`() {
        val backendTrips = listOf(
            createBaseTrip().copy(
                tripId = "tripId",
                orders = null
            ),
        )
        val tripsInteractorImpl = createTripInteractorImpl(
            tripStorage = createTripsStorage(),
            backendTrips = backendTrips,
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns true }
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
            tripsInteractorImpl.setOrderPickedUp("tripId")
            tripsInteractorImpl.refreshTrips()
        }
        runBlocking {
            tripsInteractorImpl.currentTrip.observeAndGetValue()!!.let { trip ->
                trip.orders.let { orders ->
                    orders[0].let {
                        assertEquals(true, it.isPickedUp)
                    }
                }
            }
        }
    }

    @Test
    fun `it should complete trip on order complete for legacy trips`() {
        val backendTrips = listOf(
            createBaseTrip().copy(tripId = "1", status = TripStatus.ACTIVE.value, orders = null),
        )
        val apiClient: ApiClient = mockk {
            coEvery { getTrips() } returns backendTrips
            coEvery { completeTrip(any()) } returns TripCompletionSuccess
        }
        val tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips,
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
            apiClient = apiClient
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
            tripsInteractorImpl.completeOrder("1")
            coVerifyAll {
                apiClient.getTrips()
                apiClient.completeTrip("1")
            }
        }
    }

    @Test
    fun `it should cancel trip on order cancel for legacy trips`() {
        val backendTrips = listOf(
            createBaseTrip().copy(tripId = "1", status = TripStatus.ACTIVE.value, orders = null),
        )
        val apiClient: ApiClient = mockk {
            coEvery { getTrips() } returns backendTrips
            coEvery { completeTrip(any()) } returns TripCompletionSuccess
        }
        val tripsInteractorImpl = createTripInteractorImpl(
            backendTrips = backendTrips,
            accountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
            apiClient = apiClient
        )
        runBlocking {
            tripsInteractorImpl.refreshTrips()
            tripsInteractorImpl.cancelOrder("1")
            coVerifyAll {
                apiClient.getTrips()
                apiClient.completeTrip("1")
            }
        }
    }

    companion object {
        fun createTripsStorage(): TripsStorage {
            return object : TripsStorage {
                var trips: List<LocalTrip> = listOf()

                override suspend fun saveTrips(trips: List<LocalTrip>) {
                    this.trips = trips
                }

                override suspend fun getTrips(): List<LocalTrip> {
                    return trips
                }
            }
        }

        fun createMockApiClient(backendTrips: List<Trip> = listOf()): ApiClient {
            return mockk {
                coEvery { getTrips() } returns backendTrips
                coEvery { completeOrder(any(), any()) } returns OrderCompletionSuccess
                coEvery { cancelOrder(any(), any()) } returns OrderCompletionSuccess
                coEvery { updateOrderMetadata(any(), any(), any()) } answers {
                    var trip = backendTrips.first { it.orders!!.any { it.id == firstArg() } }.copy()
                    trip = trip.copy(orders = trip.orders!!.map {
                        if (it.id == firstArg()) {
                            it.copy(
                                _metadata = thirdArg<Metadata>().copy(visitsAppMetadata = thirdArg<Metadata>().visitsAppMetadata.copy())
                                    .toMap()
                            )
                        } else {
                            it
                        }
                    })
                    Response.success(trip)
                }
            }
        }

        fun createTripInteractorImpl(
            tripStorage: TripsStorage = mockk() {
                coEvery { getTrips() } returns listOf()
                coEvery { saveTrips(any()) } returns Unit
            },
            backendTrips: List<Trip> = listOf(),
            accountRepository: AccountRepository = mockk() { coEvery { isPickUpAllowed } returns false },
            apiClient: ApiClient = createMockApiClient(backendTrips),
            hyperTrackService: HyperTrackService = mockk(relaxed = true) {
                coEvery { sendPickedUp(any(), any()) } returns Unit
            },
            queueInteractor: PhotoUploadQueueInteractor = mockk(relaxed = true) {}
        ): TripsInteractorImpl {
            return TripsInteractorImpl(
                apiClient,
                tripStorage,
                accountRepository,
                Injector.getMoshi(),
                hyperTrackService,
                TestCoroutineScope(),
                queueInteractor,
                mockk(relaxed = true) {},
                mockk(relaxed = true) {},
                Dispatchers.Main
            )
        }

        fun createBasePhotoForUpload(
            photoId: String = "1 " + Math.random(),
            filePath: String = "",
            state: PhotoUploadingState = PhotoUploadingState.NOT_UPLOADED
        ): PhotoForUpload {
            return PhotoForUpload(
                photoId,
                filePath,
                "",
                state,
            )
        }
    }


}