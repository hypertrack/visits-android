package com.hypertrack.android.ui.screens.visits_management.tabs.places

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val placesRepository: PlacesRepository
) : BaseViewModel() {

    val loadingState = MutableLiveData<Boolean>()

    val places: LiveData<List<PlaceItem>> =
        Transformations.map(placesRepository.geofences) { fences ->
            fences.map { PlaceItem(it) }
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

}