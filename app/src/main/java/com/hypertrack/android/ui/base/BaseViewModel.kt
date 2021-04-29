package com.hypertrack.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections

open class BaseViewModel : ViewModel() {

    val destination = SingleLiveEvent<NavDirections>()
    val popBackStack = SingleLiveEvent<Boolean>()

    //todo remove loadingState form children and rename to loadingState
    val loadingStateBase = SingleLiveEvent<Boolean>()

}