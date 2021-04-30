package com.hypertrack.android.complex

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hypertrack.android.api.MainCoroutineScopeRule
import com.hypertrack.android.createBaseOrder
import com.hypertrack.android.createBaseTrip
import com.hypertrack.android.interactors.TripInteractorTest
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.observeAndGetValue
import com.hypertrack.android.ui.screens.order_details.OrderDetailsViewModel
import com.hypertrack.android.ui.screens.visits_management.tabs.orders.OrdersListViewModel
import io.mockk.mockk
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ComplexTripsTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `it should update order completion state on orders list screen after changing it on order details screen`() {
        val tripsInteractor = TripInteractorTest.createTripInteractorImpl(
            backendTrips = listOf(
                createBaseTrip().copy(
                    orders = listOf(
                        createBaseOrder().copy(id = "1"),
                        createBaseOrder().copy(id = "2"),
                    )
                )
            ),
        )
        val listVm = OrdersListViewModel(tripsInteractor, mockk())
        val detailsVm1 =
            OrderDetailsViewModel(
                "1",
                tripsInteractor,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )
        val detailsVm2 =
            OrderDetailsViewModel(
                "2",
                tripsInteractor,
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true),
                mockk(relaxed = true)
            )

        runBlocking {
            tripsInteractor.refreshTrips()
            assertNotNull(tripsInteractor.getOrder("1"))

            detailsVm1.onCompleteClicked()
            listVm.orders.observeAndGetValue().first { it.id == "1" }.let {
                assertEquals(OrderStatus.COMPLETED, it.status)
            }
            listVm.orders.observeAndGetValue().first { it.id == "2" }.let {
                assertEquals(OrderStatus.ONGOING, it.status)
            }

            detailsVm2.onCancelClicked()
            listVm.orders.observeAndGetValue().first { it.id == "1" }.let {
                assertEquals(OrderStatus.COMPLETED, it.status)
            }
            listVm.orders.observeAndGetValue().first { it.id == "2" }.let {
                assertEquals(OrderStatus.CANCELED, it.status)
            }
        }
    }

}