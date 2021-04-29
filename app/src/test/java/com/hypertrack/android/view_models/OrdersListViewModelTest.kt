package com.hypertrack.android.view_models

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.hypertrack.android.api.MainCoroutineScopeRule
import com.hypertrack.android.createBaseOrder
import com.hypertrack.android.createBaseTrip
import com.hypertrack.android.interactors.TripInteractorTest
import com.hypertrack.android.interactors.TripsInteractor
import com.hypertrack.android.interactors.TripsInteractorImpl
import com.hypertrack.android.models.LocalOrderTest
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.ui.screens.visits_management.tabs.orders.OrdersListViewModel
import com.hypertrack.android.utils.HyperTrackService
import com.hypertrack.android.utils.Injector
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Rule
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class OrdersListViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `it should show ongoing orders first (with preserved backend order), then orders with other statuses`() {
        val backendOrders = listOf<Order>(
            createBaseOrder().copy(
                id = "1",
                _status = OrderStatus.COMPLETED.value
            ),
            createBaseOrder().copy(
                id = "2",
                _status = OrderStatus.ONGOING.value
            ),
            createBaseOrder().copy(
                id = "3",
                _status = OrderStatus.CANCELED.value
            ),
            createBaseOrder().copy(
                id = "4",
                _status = OrderStatus.ONGOING.value
            ),
        )
        val tripsInteractor: TripsInteractor = TripsInteractorImpl(
            mockk() {
                coEvery { getTrips() } returns listOf(
                    createBaseTrip().copy(orders = backendOrders)
                )
            },
            mockk() {
                coEvery { getTrips() } returns listOf()
                coEvery { saveTrips(any()) } returns Unit
            },
            mockk() {
                coEvery { isPickUpAllowed } returns false
            },
            Injector.getMoshi(),
            mockk<HyperTrackService> {
                coEvery { sendPickedUp(any(), any()) } returns Unit
            },
            TestCoroutineScope(),
            mockk(relaxed = true) {},
            mockk(relaxed = true) {},
            mockk(relaxed = true) {},
            Dispatchers.Main
        )
        runBlocking {
            tripsInteractor.refreshTrips()
            assertTrue(tripsInteractor.currentTrip.value != null)
            val vm = OrdersListViewModel(tripsInteractor, mockk())
            vm.orders.observeForever {}
            val orders = vm.orders.value!!
//            orders.forEach { println("${it.id} ${it.status}") }
            assertEquals("2", orders[0].id)
            assertEquals("4", orders[1].id)
            assertEquals("1", orders[2].id)
            assertEquals("3", orders[3].id)
        }

    }

}