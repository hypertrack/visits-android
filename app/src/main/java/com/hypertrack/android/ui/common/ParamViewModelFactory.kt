package com.hypertrack.android.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.screens.place_details.PlaceDetailsViewModel
import com.hypertrack.android.utils.OsUtilsProvider

@Suppress("UNCHECKED_CAST")
class ParamViewModelFactory<T>(
    private val param: T,
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            PlaceDetailsViewModel::class.java -> PlaceDetailsViewModel(
                geofenceId = param as String,
                placesRepository,
                osUtilsProvider
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}