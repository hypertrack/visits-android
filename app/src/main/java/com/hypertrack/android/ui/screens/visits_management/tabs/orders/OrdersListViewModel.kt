package com.hypertrack.android.ui.screens.visits_management.tabs.orders

import androidx.lifecycle.*
import com.hypertrack.android.interactors.TripsInteractor
import com.hypertrack.android.models.Order
import com.hypertrack.android.models.local.LocalOrder
import com.hypertrack.android.models.local.LocalTrip
import com.hypertrack.android.models.local.OrderStatus
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.common.KeyValueItem
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragmentDirections
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.BuildConfig
import kotlinx.coroutines.launch

@Suppress("IfThenToElvis")
class OrdersListViewModel(
    private val tripsInteractor: TripsInteractor,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val error = tripsInteractor.errorFlow.asLiveData()

    val loadingState = MutableLiveData<Boolean>()

    val trip: LiveData<LocalTrip?> = tripsInteractor.currentTrip

    val metadata: LiveData<List<KeyValueItem>> =
        Transformations.map(tripsInteractor.currentTrip) { trip ->
            if (trip != null) {
                trip.metadata.toList().map { KeyValueItem(it.first, it.second) }.toMutableList()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            add(KeyValueItem("trip_id (debug)", trip.id))
                        }
                    }
            } else {
                listOf()
            }
        }

    //todo task tests
    val orders: LiveData<List<LocalOrder>> =
        Transformations.map(tripsInteractor.currentTrip) { trip ->
            if (trip != null) {
                trip.orders.sortedWith { o1, o2 ->
                    when {
                        o1.status == o2.status -> {
                            compareByScheduledAt(o1, o2)
                        }
                        o1.status != OrderStatus.ONGOING -> 1
                        o2.status != OrderStatus.ONGOING -> -1
                        else -> {
                            compareByScheduledAt(o1, o2)
                        }
                    }
                }
            } else {
                listOf()
            }
        }

    private fun compareByScheduledAt(o1: LocalOrder, o2: LocalOrder): Int {
        return when {
            o1.scheduledAt == null && o2.scheduledAt != null -> -1
            o2.scheduledAt == null && o1.scheduledAt != null -> 1
            o2.scheduledAt == null && o1.scheduledAt == null -> 0
            else -> o1.scheduledAt!!.compareTo(o2.scheduledAt!!)
        }
    }

    init {
        onRefresh()
    }

    fun onRefresh() {
        viewModelScope.launch {
            loadingState.postValue(true)
            tripsInteractor.refreshTrips()
            loadingState.postValue(false)
        }
    }

    fun onOrderClick(orderId: String) {
        destination.postValue(
            VisitsManagementFragmentDirections.actionVisitManagementFragmentToOrderDetailsFragment(
                orderId
            )
        )
    }

    fun onCopyClick(it: String) {
        osUtilsProvider.copyToClipboard(it)
    }

}