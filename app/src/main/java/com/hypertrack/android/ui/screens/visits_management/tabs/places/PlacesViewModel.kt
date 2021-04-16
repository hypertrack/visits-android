package com.hypertrack.android.ui.screens.visits_management.tabs.places

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.api.Geofence
import com.hypertrack.android.api.GeofenceMarker
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.screens.visits_management.VisitsManagementFragmentDirections
import com.hypertrack.android.utils.OsUtilsProvider
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider
) : BaseViewModel() {

    val loadingState = MutableLiveData<Boolean>()

    val places: LiveData<List<PlaceItem>> =
        Transformations.map(placesRepository.geofences) { fences ->
            fences.sortedWith { o1, o2 ->
                -(when {
                    o1.lastVisit != null && o2.lastVisit != null -> {
                        o1.lastVisit!!.compareTo(o2.lastVisit!!)
                    }
                    o1.lastVisit != null -> 1
                    o2.lastVisit != null -> -1
                    else -> o1.created_at.compareTo(o2.created_at)
                })
            }.map { PlaceItem(it) }
        }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            loadingState.postValue(true)
            placesRepository.refreshGeofences()
            loadingState.postValue(false)
        }
    }

    fun createPlacesAdapter(): PlacesAdapter {
        return PlacesAdapter(osUtilsProvider)
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

}