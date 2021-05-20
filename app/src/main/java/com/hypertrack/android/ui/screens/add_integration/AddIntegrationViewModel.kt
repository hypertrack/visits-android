package com.hypertrack.android.ui.screens.add_integration

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.models.Integration
import com.hypertrack.android.repository.IntegrationsRepository
import com.hypertrack.android.ui.base.BaseViewModel
import com.hypertrack.android.ui.base.Consumable
import com.hypertrack.android.utils.MyApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlin.coroutines.CoroutineContext

@Suppress("EXPERIMENTAL_API_USAGE")
class AddIntegrationViewModel(
    private val integrationsRepository: IntegrationsRepository
) : BaseViewModel() {

    private val searchFlow = MutableSharedFlow<String>()

    val error = integrationsRepository.errorFlow.asLiveData()

    val integrations = MutableLiveData<List<Integration>>()

    val integrationSelectedEvent = MutableLiveData<Consumable<Integration>>()

    init {
        loadingStateBase.postValue(true)

        viewModelScope.launch {
            integrations.postValue(integrationsRepository.getFirstIntegrationsPage())
            loadingStateBase.postValue(false)
        }

        viewModelScope.launch {
            searchFlow.debounce(1000).collect {
                search(it)
            }
        }
    }

    fun onQueryChanged(query: String) {
        viewModelScope.launch {
            searchFlow.emit(query)
        }
    }

    private suspend fun search(query: String) {
        loadingStateBase.postValue(true)
        val integrationsRes = integrationsRepository.getIntegrations(query)
        integrations.postValue(integrationsRes)
        loadingStateBase.postValue(false)
    }

    fun onIntegrationClicked(integration: Integration) {
        integrationSelectedEvent.postValue(Consumable(integration))
    }

}
