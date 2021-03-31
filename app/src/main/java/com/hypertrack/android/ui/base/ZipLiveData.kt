package com.hypertrack.android.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class ZipLiveData<T, R>(liveData1: LiveData<T>, liveData2: LiveData<R>) :
    MediatorLiveData<Pair<T, R>>() {

    private var res1: T? = null
    private var res2: R? = null

    init {

        addSource(liveData1) {
            res1 = it
            update()
        }
        addSource(liveData2) {
            res2 = it
            update()
        }
    }

    private fun update() {
        if (res1 != null && res2 != null) {
            postValue(Pair(res1!!, res2!!))
        }
    }

}