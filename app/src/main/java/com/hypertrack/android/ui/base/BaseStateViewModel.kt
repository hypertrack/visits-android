package com.hypertrack.android.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseStateViewModel: ViewModel() {
    var state = MutableLiveData<State>()

    open fun setInitialState() {
        state.value = JustInitial
    }
}

open class State
object JustInitial : State()
object JustLoading : State()
object JustSuccess : State()
object JustUnknownError : State()
class JustError(val exception: Throwable) : State()
object JustInvalidData : State()

class UndefinedStateException(state: State): Exception(state.toString())