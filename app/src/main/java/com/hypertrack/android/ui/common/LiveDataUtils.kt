package com.hypertrack.android.ui.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.toHotTransformation(): HotLiveDataTransformation<T> {
    return HotLiveDataTransformation(this)
}

class HotLiveDataTransformation<T>(val liveData: LiveData<T>) {
    init {
        liveData.observeForever {}
    }
}