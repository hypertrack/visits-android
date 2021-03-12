package com.hypertrack.android.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections

open class BaseViewModel : ViewModel() {

    val destination = SingleLiveEvent<NavDirections>()

}