package com.hypertrack.android.ui.screens.visits_management.tabs.places

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceMarker
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.ui.base.SingleLiveEvent
import com.hypertrack.android.ui.base.toConsumable
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragmentDirections
import com.hypertrack.android.ui.screens.visits_management.tabs.history.DeviceLocationProvider
import com.hypertrack.android.utils.FusedDeviceLocationProvider
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.utils.TimeDistanceFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val locationProvider: DeviceLocationProvider,
    private val timeDistanceFormatter: TimeDistanceFormatter,
) : BaseViewModel() {

    private var nextPageToken: String? = null
    private var updateJob: Job? = null

    val placesPage = SingleLiveEvent<Consumable<List<PlaceItem>>?>()

    fun refresh() {
        placesPage.value = null
        placesPage.postValue(null)
        nextPageToken = null
        updateJob?.cancel()
        loadingStateBase.value = false
        loadingStateBase.postValue(false)
        onLoadMore()
    }

    fun createPlacesAdapter(): PlacesAdapter {
        return PlacesAdapter(
            osUtilsProvider,
            locationProvider,
            timeDistanceFormatter
        )
    }

    fun onPlaceClick(placeItem: PlaceItem) {
        destination.postValue(
            VisitsManagementFragmentDirections.actionVisitManagementFragmentToPlaceDetailsFragment(
                placeItem.geofence._id
            )
        )
    }

    fun onAddPlaceClicked() {
        destination.postValue(VisitsManagementFragmentDirections.actionVisitManagementFragmentToAddPlaceFragment())
    }

    //todo test
    fun onLoadMore() {
        if ((loadingStateBase.value ?: false) == false) {
            //todo change to viewModelScope (cause bug when launch is not called after geofence creation)
            updateJob = GlobalScope.launch {
                try {
                    if (nextPageToken != null || placesPage.value == null) {
//                        Log.v("hypertrack-verbose", "** loading ${nextPageToken.hashCode()}")
                        loadingStateBase.postValue(true)
                        val res = placesRepository.loadPage(nextPageToken)
                        nextPageToken = res.paginationToken
//                        Log.v("hypertrack-verbose", "nextPageToken = ${nextPageToken.hashCode()}")
                        placesPage.postValue(Consumable(res.geofences.map { PlaceItem(it) }))
                        loadingStateBase.postValue(false)
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        errorBase.postValue(osUtilsProvider.getErrorMessage(e).toConsumable())
                        loadingStateBase.postValue(false)
                    }
                }
            }
        }
    }

}