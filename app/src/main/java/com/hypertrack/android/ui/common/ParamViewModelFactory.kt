package com.hypertrack.android.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.net.PlacesClient
import com.hypertrack.android.api.ApiClient
import com.hypertrack.android.interactors.PhotoUploadQueueInteractor
import com.hypertrack.android.interactors.PhotoUploadQueueStorage
import com.hypertrack.android.interactors.TripsInteractor
import com.hypertrack.android.repository.AccountRepository
import com.hypertrack.android.repository.PlacesRepository
import com.hypertrack.android.ui.screens.place_details.PlaceDetailsViewModel
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.android.ui.screens.order_details.OrderDetailsViewModel

@Suppress("UNCHECKED_CAST")
class ParamViewModelFactory<T>(
    private val param: T,
    private val tripsInteractor: TripsInteractor,
    private val placesRepository: PlacesRepository,
    private val osUtilsProvider: OsUtilsProvider,
    private val placesClient: PlacesClient,
    private val accountRepository: AccountRepository,
    private val photoUploadQueueInteractor: PhotoUploadQueueInteractor,
    private val apiClient: ApiClient
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            PlaceDetailsViewModel::class.java -> PlaceDetailsViewModel(
                geofenceId = param as String,
                placesRepository,
                osUtilsProvider
            ) as T
            OrderDetailsViewModel::class.java -> OrderDetailsViewModel(
                orderId = param as String,
                tripsInteractor,
                photoUploadQueueInteractor,
                osUtilsProvider,
                accountRepository,
                apiClient
            ) as T
            else -> throw IllegalArgumentException("Can't instantiate class $modelClass")
        }
    }
}