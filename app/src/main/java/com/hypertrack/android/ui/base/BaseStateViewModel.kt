package com.hypertrack.android.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

abstract class BaseStateViewModel : ViewModel() {
    val obsMap = mutableMapOf<LiveData<*>, Observer<*>>()

    var state = MutableLiveData<State>()

    open fun setInitialState() {
        state.value = JustInitial
    }

    override fun onCleared() {
        super.onCleared()
        //todo
        obsMap.keys.forEach {
            it.removeObserver(obsMap[it] as Observer<in Any>)
        }
    }

    fun <T> LiveData<T>.observeManaged(obs: Observer<T>) {
        observeForever(obs)
        check(obsMap[this] == null)
        obsMap[this] = obs
    }
}


open class State
object JustInitial : State()
object JustLoading : State()
object JustSuccess : State()
object JustUnknownError : State()
class JustError(val exception: Throwable) : State()
object JustInvalidData : State()

class UndefinedStateException(state: State) : Exception(state.toString())