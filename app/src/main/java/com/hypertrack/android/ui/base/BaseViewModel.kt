package com.hypertrack.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hypertrack.android.utils.OsUtilsProvider
import com.hypertrack.logistics.android.github.R

open class BaseViewModel : ViewModel() {

    val destination = SingleLiveEvent<NavDirections>()
    val popBackStack = SingleLiveEvent<Boolean>()

    //todo remove loadingState form children and rename to loadingState
    val loadingStateBase = SingleLiveEvent<Boolean>()
    val errorBase = SingleLiveEvent<Consumable<String>>()

}